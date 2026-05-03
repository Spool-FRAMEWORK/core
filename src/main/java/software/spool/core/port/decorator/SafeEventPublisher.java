package software.spool.core.port.decorator;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Destination;
import software.spool.core.port.bus.EventPublisher;

/**
 * Decorator that wraps an {@link EventPublisher} and normalises any
 * unchecked exception into an {@link EventBusEmitException}.
 *
 * <p>
 * Exceptions that are already a {@link SpoolException} are rethrown as-is.
 * </p>
 */
public class SafeEventPublisher implements EventPublisher {
    private final EventPublisher publisher;

    public SafeEventPublisher(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public <E extends Event> void publish(E event) throws EventBusEmitException {
        try {
            publisher.publish(event);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new EventBusEmitException(event, e.getMessage(), e);
        }
    }

    public static SafeEventPublisher of(EventPublisher emitter) {
        return new SafeEventPublisher(emitter);
    }
}
