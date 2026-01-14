package com.codeit.closet.module.recommendation.dto;

import java.util.List;
import java.util.UUID;

/**
 * 의상 추천 API 응답 DTO (프론트엔드 명세 맞춤)
 */
public record RecommendationResponse(
        UUID weatherId,
        UUID userId,
        List<RecommendationClothDTO> clothes
) {
}
