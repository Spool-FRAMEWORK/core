package software.spool.core.utils.media;

import software.spool.core.model.vo.MediaType;

import java.util.Map;

public final class MediaTypeResolver {

    private static final Map<String, MediaType> ALIASES = Map.of(
            "JSON", MediaTypes.JSON,
            "TEXT", MediaTypes.TEXT,
            "PDF", MediaTypes.PDF,
            "PNG", MediaTypes.PNG,
            "JPEG", MediaTypes.JPEG,
            "JPG", MediaTypes.JPEG,
            "MP4", MediaTypes.MP4
    );

    private MediaTypeResolver() {}

    public static MediaType resolve(String value) {
        return ALIASES.getOrDefault(
                value.toUpperCase(),
                MediaType.of(value)
        );
    }
}