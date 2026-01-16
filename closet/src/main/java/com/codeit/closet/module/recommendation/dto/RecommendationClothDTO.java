package com.codeit.closet.module.recommendation.dto;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import java.util.List;
import java.util.UUID;

/**
 * 추천 의상 DTO (프론트엔드 명세 맞춤)
 */
public record RecommendationClothDTO(
        UUID clothesId,
        String name,
        String imageUrl,
        String type,
        List<ClothAttributeValueDTO> attributes
) {
}
