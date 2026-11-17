package software.spool.core.acceptance.http;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import software.spool.core.adapter.http.HealthRoutes;
import software.spool.core.adapter.http.JdkNodeHttpServer;
import software.spool.core.port.authentication.Authentication;
import software.spool.core.port.authentication.Identity;
import software.spool.core.port.authentication.RequestAuthenticator;
import software.spool.core.port.health.NodeHealthPayload;
import software.spool.core.port.http.Caller;
import software.spool.core.port.http.HttpMethod;
import software.spool.core.port.http.HttpResponse;
import software.spool.core.port.http.HttpRoute;
import software.spool.core.port.http.RouteAccess;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtectHttpEntriesSteps {
    private static final String BEARER = "Bearer ";

    private final Map<String, Identity> identityByToken = new HashMap<>();
    private final Map<String, String> rejectionByToken = new HashMap<>();
    private final List<String> callersOfTheEntry = new ArrayList<>();
    private final RecordingLogger log = new RecordingLogger();
    private final HttpClient client = HttpClient.newHttpClient();
    private JdkNodeHttpServer server;
    private int port;
    private java.net.http.HttpResponse<String> response;

    private final RequestAuthenticator testProvider = request -> {
        Optional<String> token = request.header("Authorization").filter(value -> value.startsWith(BEARER))
                .map(value -> value.substring(BEARER.length()));
        if (token.isEmpty()) return Authentication.rejected("the request has no bearer token");
        String rejection = rejectionByToken.get(token.get());
        if (rejection != null) return Authentication.rejected(rejection);
        Identity identity = identityByToken.get(token.get());
        return identity == null ? Authentication.rejected("the token is not known") : Authentication.accepted(identity);
    };

    @Given("the node has an entry {string} that requires the role {string}")
    public void theNodeHasAnEntry(String path, String role) throws IOException {
        port = freePort();
        server = new JdkNodeHttpServer(port, log);
        HealthRoutes.of(() -> NodeHealthPayload.of("node-test", List.of())).forEach(server::register);
        server.register(new HttpRoute(HttpMethod.GET, path, new RouteAccess.Protected(testProvider, role),
                (request, caller) -> {
                    callersOfTheEntry.add(((Caller.Identified) caller).identity().subject());
                    return HttpResponse.json(200, "{}");
                }));
        server.start();
    }

    @Given("{string} is identified with the role {string}")
    public void isIdentifiedWithTheRole(String name, String role) {
        identityByToken.put(tokenOf(name), new Identity(name, Set.of(role), "test"));
    }

    @Given("the provider rejects the token {string} because {string}")
    public void theProviderRejectsTheToken(String token, String reason) {
        rejectionByToken.put(token, reason);
    }

    @When("a request without credentials reaches {string}")
    public void aRequestWithoutCredentialsReaches(String path) throws Exception {
        response = client.send(get(path).build(), java.net.http.HttpResponse.BodyHandlers.ofString());
    }

    @When("{string} calls {string}")
    public void calls(String name, String path) throws Exception {
        aRequestWithTheTokenReaches(tokenOf(name), path);
    }

    @When("a request with the token {string} reaches {string}")
    public void aRequestWithTheTokenReaches(String token, String path) throws Exception {
        response = client.send(get(path).header("Authorization", BEARER + token).build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
    }

    @Then("the request is rejected because nobody was identified")
    public void theRequestIsRejectedBecauseNobodyWasIdentified() {
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Then("the request is rejected for lack of permission")
    public void theRequestIsRejectedForLackOfPermission() {
        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Then("the entry does not run")
    public void theEntryDoesNotRun() {
        assertThat(callersOfTheEntry).isEmpty();
    }

    @Then("the entry runs for {string}")
    public void theEntryRunsFor(String name) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(callersOfTheEntry).containsExactly(name);
    }

    @Then("the node answers with its health")
    public void theNodeAnswersWithItsHealth() {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("node-test");
    }

    @Then("the log says {string}")
    public void theLogSays(String text) {
        assertThat(log.lines()).anySatisfy(line -> assertThat(line).contains(text));
    }

    @Then("the log does not contain {string}")
    public void theLogDoesNotContain(String text) {
        assertThat(log.lines()).noneSatisfy(line -> assertThat(line).contains(text));
    }

    @After
    public void stopTheNode() {
        if (server != null) server.stop();
    }

    private HttpRequest.Builder get(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
    }

    private static String tokenOf(String name) {
        return name + "-token";
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
