package com.codeit.closet.module.cloth.dto;

import java.util.UUID;

public record ClothAttributeValueDto(
        UUID attributeId,  // cloth_attributes의 id
        String value       // 선택한 값 (예: "블랙")
) {
}
