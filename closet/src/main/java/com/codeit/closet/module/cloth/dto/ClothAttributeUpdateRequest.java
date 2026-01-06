package com.codeit.closet.module.cloth.dto;

import java.util.List;

public record ClothAttributeUpdateRequest(
        String name,
        List<String> selectableValues
) {
}
