package software.spool.core.adapter.kafka;

public record KafkaEventBusConfig(String bootstrapServers) {
    public static KafkaEventBusConfig local() {
        return new KafkaEventBusConfig("localhost:9092");
    }
}
