package software.spool.core.adapter.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.exception.EventBrokerEmitException;
import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Destination;
import software.spool.core.port.bus.EventPublisher;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class KafkaEventPublisher implements EventPublisher, AutoCloseable {
    private final KafkaProducer<String, byte[]> producer;

    public KafkaEventPublisher(KafkaEventBusConfig config) {
        this.producer = new KafkaProducer<>(buildProducerProps(config));
    }

    @Override
    public <E extends Event> void publish(Destination destination, BrokerMessage<E> message) throws EventBrokerEmitException {
        try {
            byte[] payload = RecordSerializerFactory.record()
                    .serialize(message.payload())
                    .getBytes(StandardCharsets.UTF_8);

            ProducerRecord<String, byte[]> record =
                    new ProducerRecord<>(destination.value(), message.key(), payload);

            producer.send(record, (metadata, ex) -> {
                if (ex != null) {
                    throw new EventBrokerEmitException(
                            message.payload(),
                            "Failed to deliver message to destination " + destination.value(),
                            ex
                    );
                }
            });
        } catch (Exception e) {
            throw new EventBrokerEmitException(
                    message.payload(),
                    "Failed to emit message to Kafka destination [" + destination.value() + "]",
                    e
            );
        }
    }

    public void flush() {
        producer.flush();
    }

    @Override
    public void close() {
        producer.close();
    }

    private static Properties buildProducerProps(KafkaEventBusConfig config) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 200);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16_384);
        return props;
    }
}