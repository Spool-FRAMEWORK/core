package software.spool.core.model;

import software.spool.core.model.event.EventAddress;

import java.time.Instant;

/**
 * Base interface for all events in the Spool framework.
 *
 * <p>
 * Every event carries a unique identifier, causation and correlation IDs
 * for distributed tracing, and a timestamp indicating when it occurred.
 * </p>
 */
public interface Event {
    String eventId();

    String causationId();

    String correlationId();

    Instant timestamp();

    default String eventType() {
        return getClass().getSimpleName();
    }

    default String address() {
        EventAddress annotation = this.getClass().getAnnotation(EventAddress.class);
        return annotation != null
                ? annotation.value()
                : eventType();
    }
}