package software.spool.core.adapter.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import software.spool.core.exception.EventBrokerListenException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.EventSubscriber;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.bus.Subscription;

import java.util.List;
import java.util.Properties;

public class KafkaEventSubscriber implements EventSubscriber {
    private final Properties   baseProps;

    public KafkaEventSubscriber(KafkaEventBusConfig config) {
        this.baseProps = buildBaseProps(config);
    }

    @Override
    public <E extends Event> Subscription on(Class<E> event, Handler<E> handler)
            throws EventBrokerListenException {
        String topic = event.getSimpleName();
        Properties props = new Properties();
        props.putAll(baseProps);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "spool." + topic);
        try {
            KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(List.of(topic));
            KafkaSubscription<E> subscription = new KafkaSubscription<>(consumer, handler, event);
            subscription.start();
            return subscription;

        } catch (Exception e) {
            throw new EventBrokerListenException(event,  "Failed to subscribe to Kafka topic [" + topic + "]", e);
        }
    }

    private static Properties buildBaseProps(KafkaEventBusConfig config) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                  StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                  ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,   "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,  "true");
        return props;
    }
}
