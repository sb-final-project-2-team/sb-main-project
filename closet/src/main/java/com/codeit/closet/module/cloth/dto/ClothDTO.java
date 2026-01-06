package com.codeit.closet.module.cloth.dto;

import java.util.List;
import java.util.UUID;

public record ClothDTO(
        UUID id,
        UUID ownerId,
        String name,
        String imageUrl,
        String type,
        List<ClothAttributeValueDto> attributes  // 속성 값 목록
) {
}
