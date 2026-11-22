package software.spool.core.acceptance.schedule;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import software.spool.core.adapter.schedule.CronUtilsCronCalendar;
import software.spool.core.model.schedule.CronExpression;
import software.spool.core.model.schedule.InvalidScheduleException;
import software.spool.core.model.schedule.Schedule;
import software.spool.core.port.schedule.CronCalendar;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ScheduleAModuleSteps {
    private final CronCalendar calendar = new CronUtilsCronCalendar();
    private Schedule schedule;
    private ZonedDateTime start;
    private Optional<RuntimeException> rejection = Optional.empty();

    @Given("a module that runs once")
    public void aModuleThatRunsOnce() {
        schedule = Schedule.once();
    }

    @Given("a module that runs every {string}")
    public void aModuleThatRunsEvery(String interval) {
        schedule = Schedule.every(durationOf(interval));
    }

    @Given("a module that runs on {string} in {string}")
    public void aModuleThatRunsOn(String expression, String zone) {
        schedule = Schedule.cron(CronExpression.parse(expression, calendar), ZoneId.of(zone));
    }

    @When("the module starts at {string}")
    public void theModuleStartsAt(String moment) {
        start = ZonedDateTime.parse(moment);
    }

    @When("the cron expression {string} is read")
    public void theCronExpressionIsRead(String expression) {
        rejection = rejectionOf(() -> CronExpression.parse(expression, calendar));
    }

    @When("a module is set to run every {string}")
    public void aModuleIsSetToRunEvery(String interval) {
        rejection = rejectionOf(() -> Schedule.every(durationOf(interval)));
    }

    @When("a module is set to run every interval without one")
    public void aModuleIsSetToRunEveryIntervalWithoutOne() {
        rejection = rejectionOf(() -> Schedule.every(null));
    }

    @When("a module is set to run on {string} without a time zone")
    public void aModuleIsSetToRunOnWithoutATimeZone(String expression) {
        CronExpression cron = CronExpression.parse(expression, calendar);
        rejection = rejectionOf(() -> Schedule.cron(cron, null));
    }

    @Then("the first execution is at {string}")
    public void theFirstExecutionIsAt(String moment) {
        assertThat(schedule.firstExecution(start).toInstant()).isEqualTo(ZonedDateTime.parse(moment).toInstant());
    }

    @Then("there is no execution after a round that ends at {string}")
    public void thereIsNoExecutionAfter(String roundEnd) {
        assertThat(schedule.nextExecutionAfter(ZonedDateTime.parse(roundEnd))).isEmpty();
    }

    @Then("the execution after a round that ends at {string} is at {string}")
    public void theExecutionAfterIsAt(String roundEnd, String moment) {
        assertThat(schedule.nextExecutionAfter(ZonedDateTime.parse(roundEnd)).map(ZonedDateTime::toInstant))
                .contains(ZonedDateTime.parse(moment).toInstant());
    }

    @Then("the cron expression is rejected")
    public void theCronExpressionIsRejected() {
        assertThat(rejection).containsInstanceOf(InvalidScheduleException.class);
    }

    @Then("the schedule is rejected because the interval must be positive")
    public void rejectedBecauseTheIntervalMustBePositive() {
        assertRejectedWith("Schedule interval must be positive");
    }

    @Then("the schedule is rejected because the interval is required")
    public void rejectedBecauseTheIntervalIsRequired() {
        assertRejectedWith("Schedule interval required");
    }

    @Then("the schedule is rejected because the time zone is required")
    public void rejectedBecauseTheTimeZoneIsRequired() {
        assertRejectedWith("Cron schedule without time zone");
    }

    private void assertRejectedWith(String reason) {
        assertThat(rejection).containsInstanceOf(InvalidScheduleException.class);
        assertThat(rejection.orElseThrow().getMessage()).startsWith(reason);
    }

    private static Duration durationOf(String shortForm) {
        return Duration.parse("PT" + shortForm.toUpperCase());
    }

    private static Optional<RuntimeException> rejectionOf(Supplier<?> creation) {
        try {
            creation.get();
            return Optional.empty();
        } catch (RuntimeException rejected) {
            return Optional.of(rejected);
        }
    }
}
