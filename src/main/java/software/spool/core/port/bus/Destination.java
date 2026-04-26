package software.spool.core.port.bus;

public record Destination(String value) {
    public Destination {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Destination cannot be blank");
        }
    }
}
