package api.model.user.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class InstantSerializer extends StdSerializer<Instant> {
    public InstantSerializer() {
        this(Instant.class);
    }

    private final static String DATE_TIME_FORMAT = "yyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    protected InstantSerializer(Class<Instant> t) {
        super(t);
    }

    protected InstantSerializer(JavaType type) {
        super(type);
    }

    protected InstantSerializer(Class<?> t, boolean dummy) {
        super(t, dummy);
    }

    protected InstantSerializer(StdSerializer<?> src) {
        super(src);
    }

    @Override
    public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(DateTimeZone.UTC.toTimeZone().toZoneId());
        jsonGenerator.writeString(formatter.format(instant));
    }
}
