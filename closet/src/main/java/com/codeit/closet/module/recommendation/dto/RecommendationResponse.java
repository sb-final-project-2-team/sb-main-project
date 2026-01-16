package com.codeit.closet.module.recommendation.dto;

import java.util.List;
import java.util.UUID;

/**
 * 의상 추천 API 응답 DTO
 * - clothes: 최상위 코디의 의상 목록 (하위 호환성)
 * - outfits: 여러 코디 조합을 점수순으로 반환 (신규)
 */
public record RecommendationResponse(
        UUID weatherId,
        UUID userId,
        List<RecommendationClothDTO> clothes,
        List<RecommendationOutfitDTO> outfits
) {
}
