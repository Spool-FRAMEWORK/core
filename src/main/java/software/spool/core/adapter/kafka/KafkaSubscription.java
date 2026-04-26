package software.spool.core.adapter.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.model.Event;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.bus.Subscription;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class KafkaSubscription<E extends Event> implements Subscription {
    private final KafkaConsumer<String, byte[]> consumer;
    private final Handler<BrokerMessage<E>> handler;
    private final Class<E> eventType;
    private final Thread thread;
    private volatile boolean running = true;

    KafkaSubscription(KafkaConsumer<String, byte[]> consumer,
                      Handler<BrokerMessage<E>> handler,
                      Class<E> eventType) {
        this.consumer = consumer;
        this.handler = handler;
        this.eventType = eventType;
        this.thread = new Thread(this::pollLoop, "spool-kafka-" + eventType.getSimpleName());
        this.thread.setDaemon(true);
    }

    void start() {
        thread.start();
    }

    private void pollLoop() {
        try {
            while (running) {
                ConsumerRecords<String, byte[]> records =
                        consumer.poll(Duration.ofMillis(200));

                for (ConsumerRecord<String, byte[]> record : records) {
                    try {
                        E payload = PayloadDeserializerFactory.json()
                                .as(eventType)
                                .deserialize(new String(record.value(), StandardCharsets.UTF_8));

                        Map<String, String> headers = new LinkedHashMap<>();
                        record.headers().forEach(header -> headers.put(
                                header.key(),
                                header.value() == null
                                        ? null
                                        : new String(header.value(), StandardCharsets.UTF_8)
                        ));

                        BrokerMessage<E> message = new BrokerMessage<>(
                                payload,
                                record.key(),
                                headers
                        );

                        handler.handle(message);
                    } catch (Exception e) {
                        System.err.println(
                                "Error while handling event " + eventType.getSimpleName() +
                                        " from Kafka: " + e.getMessage()
                        );
                    }
                }
            }
        } catch (WakeupException ignored) {
        } finally {
            consumer.close();
        }
    }

    @Override
    public void cancel() {
        running = false;
        consumer.wakeup();
    }

    @Override
    public boolean isActive() {
        return running;
    }
}