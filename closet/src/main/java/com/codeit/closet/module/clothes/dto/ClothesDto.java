package com.codeit.closet.module.clothes.dto;

import java.util.UUID;

public record ClothesDto(
        UUID id,
        UUID ownerId,
        String name,
        String type // 의상 속성, image url 은 나중에
) {
}
