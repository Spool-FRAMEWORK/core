package software.spool.core.port.bus;

import software.spool.core.adapter.memory.InMemoryEventBroker;

/**
 * Combines {@link EventPublisher} and {@link EventSubscriber} into a
 * single bidirectional event bus.
 *
 * <p>
 * Implementations can both emit events and register handlers for them.
 * The in-memory implementation is provided by
 * {@link InMemoryEventBroker}.
 * </p>
 */
public interface EventBroker extends EventPublisher, EventSubscriber {
}
