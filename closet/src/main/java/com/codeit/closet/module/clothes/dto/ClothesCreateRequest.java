package com.codeit.closet.module.clothes.dto;

import java.util.UUID;

public record ClothesCreateRequest(
        UUID ownerId,
        String name,
        String type // 의상 속성은 나중에

) {
}
