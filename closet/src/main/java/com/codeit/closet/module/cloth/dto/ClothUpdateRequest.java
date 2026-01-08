package com.codeit.closet.module.cloth.dto;

import java.util.List;

public record ClothUpdateRequest(
        String name,
        String type,
        List<ClothAttributeValueDTO> attributes  // 속성 값 목록 (optional)
) {
}
