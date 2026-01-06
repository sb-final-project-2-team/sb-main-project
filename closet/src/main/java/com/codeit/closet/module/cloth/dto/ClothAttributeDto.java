package com.codeit.closet.module.cloth.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClothAttributeDto(
        UUID id,
        String name,
        List<String> selectableValues,
        Instant createdAt
) {
}
