package com.codeit.closet.module.recommendation.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.recommendation.dto.RecommendationResponse;
import com.codeit.closet.module.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 의상 추천 API 컨트롤러
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 날씨 기반 의상 추천 조회
     *
     * @param weatherId 날씨 데이터 ID (선택적, 미제공 시 사용자 설정 지역의 최신 데이터 사용)
     * @param limit 추천 결과 개수 (기본값: 5, 최대: 20)
     * @return 추천 코디 목록
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @RequestParam(required = false) UUID weatherId,
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal ClosetUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserDTO().id();

        // limit 범위 제한 (1-20)
        int validLimit = Math.max(1, Math.min(20, limit));

        RecommendationResponse response = recommendationService.getRecommendations(
                weatherId, userId, validLimit);

        return ResponseEntity.ok(response);
    }
}
