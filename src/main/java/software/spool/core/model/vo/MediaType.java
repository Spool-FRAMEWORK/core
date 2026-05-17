package software.spool.core.model.vo;

public record MediaType(
        String value
) {
    public MediaType {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("MediaType value cannot be blank");
    }

    public static MediaType of(String value) {
        return new MediaType(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
