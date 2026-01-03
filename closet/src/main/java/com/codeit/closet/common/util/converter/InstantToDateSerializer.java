package com.codeit.closet.common.util.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class InstantToDateSerializer extends JsonSerializer<Instant> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    @Override
    public void serialize(
            Instant value,
            JsonGenerator gen,
            SerializerProvider serializers
    ) throws IOException {

        if (value == null) {
            gen.writeNull();
            return;
        }

        String formatted =
                value.atZone(ZONE_ID)
                     .toLocalDate()
                     .format(FORMATTER);

        gen.writeString(formatted);
    }
}
