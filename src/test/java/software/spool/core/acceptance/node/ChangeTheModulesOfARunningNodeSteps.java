package software.spool.core.acceptance.node;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import software.spool.core.model.spool.SpoolModule;
import software.spool.core.model.spool.SpoolNode;
import software.spool.core.port.http.HttpMethod;
import software.spool.core.port.http.HttpResponse;
import software.spool.core.port.http.HttpRoute;
import software.spool.core.port.http.RouteAccess;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeTheModulesOfARunningNodeSteps {
    private static final long QUIET_MILLIS = 150;

    private final Map<String, BeatingModule> modules = new HashMap<>();
    private final Map<String, BeatingModule> routedModuleOf = new HashMap<>();
    private final HttpClient client = HttpClient.newHttpClient();
    private SpoolNode node;
    private int port;
    private Optional<RuntimeException> failure = Optional.empty();
    private boolean stopped;

    @After
    public void stopTheNode() {
        if (node != null && !stopped) node.stop();
    }

    @Given("a running node that added the deployment {string} with the modules {string}")
    public void aRunningNodeThatAdded(String deployment, String module) throws IOException {
        port = freePort();
        node = SpoolNode.create(port);
        node.start();
        node.add(deployment, List.of(module(module)));
    }

    @Given("the node added the deployment {string} with a module that answers {string}")
    public void theNodeAddedARoutedDeployment(String deployment, String path) {
        theNodeAddsARoutedDeployment(deployment, path);
        assertThat(failure).isEmpty();
    }

    @Given("the node added the deployment {string} with the modules {string}")
    public void theNodeAddedTheDeployment(String deployment, String module) {
        node.add(deployment, List.of(module(module)));
    }

    @When("the node adds the deployment {string} with the modules {string} and {string}")
    public void theNodeAddsTwoModules(String deployment, String first, String second) {
        add(deployment, List.of(module(first), module(second)));
    }

    @When("the node adds the deployment {string} with the modules {string}")
    public void theNodeAddsOneModule(String deployment, String module) {
        add(deployment, List.of(module(module)));
    }

    @When("the node adds the deployment {string} with a module that answers {string}")
    public void theNodeAddsARoutedDeployment(String deployment, String path) {
        BeatingModule module = new BeatingModule(deployment + "-module", List.of(new HttpRoute(HttpMethod.GET, path,
                RouteAccess.open(), (request, caller) -> HttpResponse.json(200, "{\"deployment\":\"" + deployment + "\"}"))));
        modules.put(deployment + "-module", module);
        routedModuleOf.put(deployment, module);
        add(deployment, List.of(module));
    }

    @When("the node removes the deployment {string}")
    public void theNodeRemoves(String deployment) {
        node.remove(deployment);
    }

    @When("the node stops")
    public void theNodeStops() {
        node.stop();
        stopped = true;
    }

    @Then("the modules {string} and {string} are running")
    public void theModulesAreRunning(String first, String second) {
        assertThat(failure).isEmpty();
        assertThat(modules.get(first).running()).isTrue();
        assertThat(modules.get(second).running()).isTrue();
    }

    @Then("the modules {string} and {string} are stopped")
    public void theModulesAreStopped(String first, String second) {
        assertThat(modules.get(first).running()).isFalse();
        assertThat(modules.get(second).running()).isFalse();
    }

    @Then("the module {string} was not restarted")
    public void theModuleWasNotRestarted(String module) {
        assertThat(modules.get(module).starts()).isEqualTo(1);
    }

    @Then("the module {string} is still running")
    public void theModuleIsStillRunning(String module) {
        assertThat(modules.get(module).running()).isTrue();
        assertThat(modules.get(module).starts()).isEqualTo(1);
    }

    @Then("the health of the node lists {string}, {string} and {string}")
    public void theHealthLists(String first, String second, String third) throws Exception {
        assertThat(health()).contains(quoted(first), quoted(second), quoted(third));
    }

    @Then("the health of the node lists only {string}")
    public void theHealthListsOnly(String module) throws Exception {
        String health = health();
        assertThat(health).contains(quoted(module));
        modules.keySet().stream().filter(other -> !other.equals(module))
                .forEach(other -> assertThat(health).doesNotContain(quoted(other)));
    }

    @Then("a request to {string} reaches that module")
    public void aRequestReachesThatModule(String path) throws Exception {
        assertThat(failure).isEmpty();
        assertThat(get(path).statusCode()).isEqualTo(200);
    }

    @Then("a request to {string} reaches the module of {string}")
    public void aRequestReachesTheModuleOf(String path, String deployment) throws Exception {
        java.net.http.HttpResponse<String> response = get(path);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(quoted(deployment));
    }

    @Then("a request to {string} finds no route")
    public void aRequestFindsNoRoute(String path) throws Exception {
        assertThat(get(path).statusCode()).isEqualTo(404);
    }

    @Then("that module is stopped and sends no more heartbeats")
    public void thatModuleIsStopped() throws InterruptedException {
        BeatingModule module = routedModuleOf.values().iterator().next();
        assertThat(module.running()).isFalse();
        int beats = module.beats();
        Thread.sleep(QUIET_MILLIS);
        assertThat(module.beats()).isEqualTo(beats);
    }

    @Then("adding it fails with an error that names {string}")
    public void addingFailsNaming(String text) {
        assertThat(failure).hasValueSatisfying(error -> assertThat(error).hasMessageContaining(text));
    }

    @Then("adding it fails with an error that says {string} is already in the node")
    public void addingFailsAlreadyInTheNode(String deployment) {
        assertThat(failure).hasValueSatisfying(error -> assertThat(error)
                .hasMessageContaining(deployment).hasMessageContaining("already"));
    }

    @Then("no module of {string} is running")
    public void noModuleOfIsRunning(String deployment) {
        BeatingModule module = routedModuleOf.get(deployment);
        assertThat(module.running()).isFalse();
        assertThat(module.starts()).isZero();
    }

    private void add(String deployment, List<SpoolModule> added) {
        try {
            node.add(deployment, added);
        } catch (RuntimeException e) {
            failure = Optional.of(e);
        }
    }

    private BeatingModule module(String id) {
        BeatingModule module = new BeatingModule(id, List.of());
        modules.put(id, module);
        return module;
    }

    private String health() throws Exception {
        return get("/spool/health").body();
    }

    private java.net.http.HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
    }

    private static String quoted(String text) {
        return "\"" + text + "\"";
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
