package software.spool.core.utils.media;

import software.spool.core.model.vo.MediaType;

public final class MediaTypes {

    private MediaTypes() {}

    public static final MediaType JSON =
            MediaType.of("application/json");

    public static final MediaType TEXT =
            MediaType.of("text/plain");

    public static final MediaType PDF =
            MediaType.of("application/pdf");

    public static final MediaType PNG =
            MediaType.of("image/png");

    public static final MediaType JPEG =
            MediaType.of("image/jpeg");

    public static final MediaType MP4 =
            MediaType.of("video/mp4");
}