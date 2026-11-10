package software.spool.core.model.annotation;

import software.spool.core.port.bus.DeliveryPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares how an event type is delivered when several subscriptions listen to it.
 * Without it, an event is delivered with {@link DeliveryPolicy#SINGLE_SUBSCRIBER}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Delivery {
    DeliveryPolicy value();
}
