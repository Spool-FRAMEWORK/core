package software.spool.core;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.spi.SerdeRegistry;

public class Main {
    public static void main(String[] args) {
        var deserializer = SerdeRegistry.structured("JSON")
                .builder()
                .asMap();

        System.out.println(PayloadDeserializerFactory.json().asNode().deserialize("{\"name\":\"javi\"}"));

        System.out.println(deserializer.deserialize("{\"name\":\"javi\"}"));
    }
}
