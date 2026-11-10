package software.spool.core.utils.routing;

import software.spool.core.model.Event;
import software.spool.core.model.SpoolEvent;
import software.spool.core.model.annotation.Delivery;
import software.spool.core.model.annotation.EventAddress;
import software.spool.core.port.bus.DeliveryPolicy;
import software.spool.core.port.bus.Destination;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the route of each event type from the type itself.
 *
 * <p>
 * The channel is the address declared with {@link EventAddress} or, when there is none, the class name
 * split with the configured {@link ChannelNamingConvention}. Events of Spool ({@link SpoolEvent}) are
 * prefixed with {@code spool.}: {@code EnvelopeStored} travels on {@code spool.envelope.stored}.
 * The delivery policy is the one declared with {@link Delivery}, or
 * {@link DeliveryPolicy#SINGLE_SUBSCRIBER} when there is none.
 * </p>
 */
public final class AnnotationEventRouter implements EventRouter {
    private static final String INTERNAL_CHANNEL_PREFIX = "spool.";
    private static final DeliveryPolicy DEFAULT_DELIVERY_POLICY = DeliveryPolicy.SINGLE_SUBSCRIBER;

    private final ChannelNamingConvention namingConvention;
    private final Map<Class<? extends Event>, EventRoute> routes = new ConcurrentHashMap<>();

    public AnnotationEventRouter() {
        this(ChannelNamingConvention.DOT_CASE);
    }

    public AnnotationEventRouter(ChannelNamingConvention namingConvention) {
        this.namingConvention = namingConvention;
    }

    @Override
    public EventRoute routeOf(Class<? extends Event> eventType) {
        return routes.computeIfAbsent(eventType, type -> new EventRoute(channelOf(type), deliveryPolicyOf(type)));
    }

    private Destination channelOf(Class<? extends Event> eventType) {
        String address = Optional.ofNullable(eventType.getAnnotation(EventAddress.class))
                .map(EventAddress::value)
                .orElseGet(() -> namingConvention.apply(eventType));
        return new Destination(isInternal(eventType) ? INTERNAL_CHANNEL_PREFIX + address : address);
    }

    private static DeliveryPolicy deliveryPolicyOf(Class<? extends Event> eventType) {
        return Optional.ofNullable(eventType.getAnnotation(Delivery.class))
                .map(Delivery::value)
                .orElse(DEFAULT_DELIVERY_POLICY);
    }

    private static boolean isInternal(Class<? extends Event> eventType) {
        return SpoolEvent.class.isAssignableFrom(eventType);
    }
}
