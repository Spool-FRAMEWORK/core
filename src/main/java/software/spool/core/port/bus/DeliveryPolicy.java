package software.spool.core.port.bus;

/**
 * Who receives each event of a type when several subscriptions listen to it.
 */
public enum DeliveryPolicy {
    /** One subscription of the whole system handles each event. */
    SINGLE_SUBSCRIBER,
    /** Every active subscription handles each event, replicas included. */
    EVERY_SUBSCRIBER
}
