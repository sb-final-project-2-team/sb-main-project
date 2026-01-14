package com.codeit.closet.module.recommendation.service;

import com.codeit.closet.module.recommendation.dto.RecommendationResponse;

import java.util.UUID;

/**
 * 의상 추천 서비스 인터페이스
 */
public interface RecommendationService {

    /**
     * 날씨 기반 의상 추천
     * @param weatherId 날씨 데이터 ID (null이면 사용자 설정 지역의 최신 데이터 사용)
     * @param userId 사용자 ID
     * @param limit 추천 결과 개수
     * @return 추천 응답 DTO
     */
    RecommendationResponse getRecommendations(UUID weatherId, UUID userId, int limit);

    /**
     * 피드 참고 추천 (2차 구현)
     */
    RecommendationResponse getRecommendationsWithFeedReference(
            UUID weatherId, UUID userId, boolean includePopularFeeds, int limit
    );
}
