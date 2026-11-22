package software.spool.core.adapter.health;

import org.junit.jupiter.api.Test;
import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.EventBus;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.bus.Subscription;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.PieceCheckPolicy;
import software.spool.core.port.health.PieceMonitor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonitoredEventBusTest {
    private static final PaymentReceived EVENT = new PaymentReceived("event-1", "cause-1", "correlation-1", Instant.parse("2026-11-22T10:00:00Z"));

    private final PieceMonitor monitor = PieceMonitor.of("bus", "KAFKA", Optional.empty(), PieceCheckPolicy.defaults());

    @Test
    void publish_passesTheEventToTheBus() {
        List<Event> published = new ArrayList<>();
        EventBus bus = new MonitoredEventBus(busThat(published::add), monitor);

        bus.publish(EVENT);

        assertThat(published).containsExactly(EVENT);
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void publish_whenTheBusFails_marksThePieceUnhealthy() {
        EventBus bus = new MonitoredEventBus(busThat(event -> {
            throw new EventBusEmitException(event, "the broker did not answer");
        }), monitor);

        assertThatThrownBy(() -> bus.publish(EVENT)).isInstanceOf(EventBusEmitException.class);
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    private static EventBus busThat(java.util.function.Consumer<Event> onPublish) {
        return new EventBus() {
            @Override
            public <E extends Event> void publish(E event) {
                onPublish.accept(event);
            }

            @Override
            public <E extends Event> Subscription subscribe(Class<E> eventType, Handler<E> handler) {
                return Subscription.NULL;
            }
        };
    }

    record PaymentReceived(String eventId, String causationId, String correlationId, Instant timestamp) implements Event {
    }
}
