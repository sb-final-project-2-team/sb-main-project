package com.codeit.closet.module.cloth.dto;

import java.util.UUID;

public record ClothAttributeValueDTO(
        UUID definitionId,  // cloth_attributes의 id (실수로 스웨거 스펙과 다르게 맞춰서 수정)
        String value        // 선택한 값 (예: "블랙")
) {
}
