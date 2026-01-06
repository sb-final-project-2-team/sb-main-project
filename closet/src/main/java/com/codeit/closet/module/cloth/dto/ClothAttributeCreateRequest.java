package com.codeit.closet.module.cloth.dto;

import java.util.List;

public record ClothAttributeCreateRequest(
        String name,
        List<String> selectableValues
) {
}
