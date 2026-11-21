package software.spool.core.utils.routing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.spool.core.model.Event;
import software.spool.core.model.annotation.Delivery;
import software.spool.core.model.annotation.EventAddress;
import software.spool.core.model.event.EnvelopePersisted;
import software.spool.core.model.event.EnvelopePurged;
import software.spool.core.model.event.EnvelopeRepublished;
import software.spool.core.model.event.EnvelopeStored;
import software.spool.core.model.event.EnvelopesMounted;
import software.spool.core.model.event.SourcePayloadCaptured;
import software.spool.core.model.failure.DataLakePersistFailed;
import software.spool.core.model.failure.EnvelopeQuarantined;
import software.spool.core.port.bus.DeliveryPolicy;
import software.spool.core.port.bus.Destination;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationEventRouterTest {

    private final EventRouter router = new AnnotationEventRouter();

    static Stream<Arguments> spoolEvents() {
        return Stream.of(
                Arguments.of(EnvelopePersisted.class, "spool.envelope.persisted"),
                Arguments.of(EnvelopeStored.class, "spool.envelope.stored"),
                Arguments.of(EnvelopeRepublished.class, "spool.envelope.republished"),
                Arguments.of(EnvelopePurged.class, "spool.envelope.purged"),
                Arguments.of(EnvelopesMounted.class, "spool.envelopes.mounted"),
                Arguments.of(SourcePayloadCaptured.class, "spool.source.payload.captured"),
                Arguments.of(DataLakePersistFailed.class, "spool.data.lake.persist.failed"),
                Arguments.of(EnvelopeQuarantined.class, "spool.envelope.quarantined"));
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
                Arguments.of(EnvelopeRepublished.class, DeliveryPolicy.EVERY_SUBSCRIBER),
                Arguments.of(EnvelopePurged.class, DeliveryPolicy.EVERY_SUBSCRIBER),
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
