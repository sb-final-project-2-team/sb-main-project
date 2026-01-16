package com.codeit.closet.module.recommendation.dto;

import java.util.List;

/**
 * 추천 코디(아웃핏) DTO
 * - 하나의 코디 조합(TOP+BOTTOM 또는 DRESS, 선택적 OUTER)과 점수를 포함
 */
public record RecommendationOutfitDTO(
        int score,
        List<RecommendationClothDTO> clothes
) {
}
