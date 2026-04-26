package software.spool.core.adapter.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.EventPublisher;

import java.util.Properties;

public class KafkaEventPublisher implements EventPublisher, AutoCloseable {
    private final KafkaProducer<String, byte[]> producer;

    public KafkaEventPublisher(KafkaEventBusConfig config) {
        this.producer = new KafkaProducer<>(buildProducerProps(config));
    }

    @Override
    public void emit(Event event) throws EventBusEmitException {
        String topic = event.getClass().getSimpleName();

        try {
            byte[] payload = RecordSerializerFactory.record().serialize(event).getBytes();
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, payload);
            producer.send(record, (metadata, ex) -> {
                if (ex != null)
                    throw new EventBusEmitException(event, "Failed to deliver event to topic " + topic, ex);
            });
        } catch (Exception e) {
            throw new EventBusEmitException(event, "Failed to emit event [" + topic + "] to Kafka", e);
        }
    }

    /**
     * Flush + espera a que todos los mensajes en buffer lleguen al broker.
     * Útil antes de un shutdown controlado.
     */
    public void flush() {
        producer.flush();
    }

    @Override
    public void close() {
        producer.close();
    }

    private static Properties buildProducerProps(KafkaEventBusConfig config) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,      config.bootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        // Garantía de entrega: espera ACK del líder + réplicas in-sync
        props.put(ProducerConfig.ACKS_CONFIG,                   "all");

        // Reintentos automáticos ante fallos transitorios (red, líder nuevo, etc.)
        props.put(ProducerConfig.RETRIES_CONFIG,                3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG,       200);

        // Idempotencia: evita duplicados si un retry llega dos veces al broker
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,     true);

        // Batching para mayor throughput (ajusta según latencia aceptable)
        props.put(ProducerConfig.LINGER_MS_CONFIG,              5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG,             16_384);

        return props;
    }
}
