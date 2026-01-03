package com.codeit.closet.common.util.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class FlexibleInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        String value = p.getText();

        if (value == null || value.isBlank()) {
            return null;
        }

        // Instant (Z, +09:00)
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {}

        // LocalDateTime
        try {
            return LocalDateTime
                    .parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toInstant(ZoneOffset.UTC);
        } catch (Exception ignored) {}

        // LocalDate
        try {
            return LocalDate
                    .parse(value)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant();
        } catch (Exception ignored) {}

        throw new InvalidFormatException(
                p,
                "Invalid date format for birthDate. " +
                "Supported: yyyy-MM-dd, yyyy-MM-ddTHH:mm:ss, yyyy-MM-ddTHH:mm:ssZ",
                value,
                Instant.class
        );
    }
}
