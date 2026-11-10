package software.spool.core.utils.routing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.spool.core.model.Event;
import software.spool.core.model.annotation.Delivery;
import software.spool.core.model.annotation.EventAddress;
import software.spool.core.model.event.EnvelopeConsumed;
import software.spool.core.model.event.EnvelopePersisted;
import software.spool.core.model.event.EnvelopeStored;
import software.spool.core.model.event.EnvelopesMounted;
import software.spool.core.model.event.ItemPublished;
import software.spool.core.model.event.ModuleHeartbeat;
import software.spool.core.model.event.SourcePayloadCaptured;
import software.spool.core.model.failure.DataLakePersistFailed;
import software.spool.core.model.failure.EnvelopeQuarantined;
import software.spool.core.model.failure.InboxItemConsumptionFailed;
import software.spool.core.model.failure.InboxItemStoreFailed;
import software.spool.core.model.failure.ItemPublishFailed;
import software.spool.core.model.failure.SourceFetchFailed;
import software.spool.core.model.failure.SourceItemCaptureFailed;
import software.spool.core.port.bus.DeliveryPolicy;
import software.spool.core.port.bus.Destination;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationEventRouterTest {

    private final EventRouter router = new AnnotationEventRouter();

    static Stream<Arguments> spoolEvents() {
        return Stream.of(
                Arguments.of(EnvelopeConsumed.class, "spool.envelope.consumed"),
                Arguments.of(EnvelopePersisted.class, "spool.envelope.persisted"),
                Arguments.of(EnvelopeStored.class, "spool.envelope.stored"),
                Arguments.of(EnvelopesMounted.class, "spool.envelopes.mounted"),
                Arguments.of(ItemPublished.class, "spool.item.published"),
                Arguments.of(ModuleHeartbeat.class, "spool.module.heartbeat"),
                Arguments.of(SourcePayloadCaptured.class, "spool.source.payload.captured"),
                Arguments.of(DataLakePersistFailed.class, "spool.data.lake.persist.failed"),
                Arguments.of(EnvelopeQuarantined.class, "spool.envelope.quarantined"),
                Arguments.of(InboxItemConsumptionFailed.class, "spool.inbox.item.consumption.failed"),
                Arguments.of(InboxItemStoreFailed.class, "spool.inbox.item.store.failed"),
                Arguments.of(ItemPublishFailed.class, "spool.item.publish.failed"),
                Arguments.of(SourceFetchFailed.class, "spool.source.fetch.failed"),
                Arguments.of(SourceItemCaptureFailed.class, "spool.source.item.capture.failed"));
    }

    @ParameterizedTest
    @MethodSource("spoolEvents")
    void routeOf_spoolEvent_isPrefixedWithSpoolAndSplitByDots(Class<? extends Event> eventType, String expectedChannel) {
        assertThat(router.routeOf(eventType).channel()).isEqualTo(new Destination(expectedChannel));
    }

    static Stream<Arguments> deliveryPolicies() {
        return Stream.of(
                Arguments.of(EnvelopeStored.class, DeliveryPolicy.SINGLE_SUBSCRIBER),
                Arguments.of(EnvelopePersisted.class, DeliveryPolicy.EVERY_SUBSCRIBER),
                Arguments.of(EnvelopeQuarantined.class, DeliveryPolicy.EVERY_SUBSCRIBER),
                Arguments.of(DataLakePersistFailed.class, DeliveryPolicy.EVERY_SUBSCRIBER),
                Arguments.of(SourcePayloadCaptured.class, DeliveryPolicy.EVERY_SUBSCRIBER),
                Arguments.of(PatientAdmitted.class, DeliveryPolicy.SINGLE_SUBSCRIBER),
                Arguments.of(PatientTransferred.class, DeliveryPolicy.EVERY_SUBSCRIBER));
    }

    @ParameterizedTest
    @MethodSource("deliveryPolicies")
    void routeOf_eventType_hasTheDeclaredDeliveryPolicyOrSingleSubscriberByDefault(
            Class<? extends Event> eventType, DeliveryPolicy expectedPolicy) {
        assertThat(router.routeOf(eventType).deliveryPolicy()).isEqualTo(expectedPolicy);
    }

    @Test
    void routeOf_domainEventWithoutAddress_isItsNameSplitByDotsWithoutPrefix() {
        assertThat(router.routeOf(PatientAdmitted.class).channel()).isEqualTo(new Destination("patient.admitted"));
    }

    @Test
    void routeOf_domainEventWithAddress_isTheDeclaredAddress() {
        assertThat(router.routeOf(PatientDischarged.class).channel()).isEqualTo(new Destination("hospital.discharges"));
    }

    @Test
    void routeOf_sameEventTypeTwice_returnsTheSameRoute() {
        assertThat(router.routeOf(EnvelopeStored.class)).isEqualTo(router.routeOf(EnvelopeStored.class));
    }

    record PatientAdmitted(String eventId, String causationId, String correlationId, Instant timestamp) implements Event { }

    @Delivery(DeliveryPolicy.EVERY_SUBSCRIBER)
    record PatientTransferred(String eventId, String causationId, String correlationId, Instant timestamp) implements Event { }

    @EventAddress("hospital.discharges")
    record PatientDischarged(String eventId, String causationId, String correlationId, Instant timestamp) implements Event { }
}
