package com.codeit.closet.module.recommendation.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.recommendation.dto.RecommendationClothDTO;
import com.codeit.closet.module.recommendation.dto.RecommendationOutfitDTO;
import com.codeit.closet.module.recommendation.dto.RecommendationResponse;
import com.codeit.closet.module.recommendation.service.RecommendationService;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = RecommendationController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.codeit.closet.common.config.SecurityConfig.class
        ))
@Import({com.codeit.closet.common.config.TestSecurityConfig.class,
        com.codeit.closet.common.exception.GlobalExceptionHandler.class,
        com.codeit.closet.module.recommendation.exception.RecommendationExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("RecommendationController 테스트")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    private UUID testUserId;
    private UUID testWeatherId;
    private ClosetUserDetails mockUserDetails;
    private RecommendationResponse testResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testWeatherId = UUID.randomUUID();

        UserDTO userDTO = new UserDTO(
                testUserId,
                Instant.now(),
                "test@example.com",
                "testuser",
                UserRole.USER,
                false
        );
        mockUserDetails = new ClosetUserDetails(userDTO, "password123", null, null);

        // 테스트용 추천 응답 생성 (clothesId, name, imageUrl, type, attributes)
        RecommendationClothDTO clothDTO = new RecommendationClothDTO(
                UUID.randomUUID(),  // clothesId
                "흰색 셔츠",
                "https://example.com/image.jpg",
                "TOP",
                List.of()
        );

        RecommendationOutfitDTO outfitDTO = new RecommendationOutfitDTO(
                75,
                List.of(clothDTO)
        );

        testResponse = new RecommendationResponse(
                testWeatherId,
                testUserId,
                List.of(clothDTO),
                List.of(outfitDTO)
        );
    }

    @Nested
    @DisplayName("GET /api/recommendations - 추천 조회")
    class GetRecommendationsTest {

        @Test
        @DisplayName("기본 추천 조회 성공")
        void shouldReturnRecommendationsSuccessfully() throws Exception {
            // Given
            given(recommendationService.getRecommendations(isNull(), eq(testUserId), eq(5)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weatherId").value(testWeatherId.toString()))
                    .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                    .andExpect(jsonPath("$.clothes").isArray())
                    .andExpect(jsonPath("$.outfits").isArray());
        }

        @Test
        @DisplayName("weatherId 파라미터로 추천 조회")
        void shouldReturnRecommendationsWithWeatherId() throws Exception {
            // Given
            given(recommendationService.getRecommendations(eq(testWeatherId), eq(testUserId), eq(5)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .param("weatherId", testWeatherId.toString())
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weatherId").value(testWeatherId.toString()));
        }

        @Test
        @DisplayName("limit 파라미터로 추천 개수 조정")
        void shouldReturnRecommendationsWithCustomLimit() throws Exception {
            // Given
            int customLimit = 10;
            given(recommendationService.getRecommendations(isNull(), eq(testUserId), eq(customLimit)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .param("limit", String.valueOf(customLimit))
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("limit 최소값(1) 적용")
        void shouldApplyMinimumLimit() throws Exception {
            // Given: limit이 0 이하면 1로 조정
            given(recommendationService.getRecommendations(isNull(), eq(testUserId), eq(1)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .param("limit", "0")
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("limit 최대값(20) 적용")
        void shouldApplyMaximumLimit() throws Exception {
            // Given: limit이 20 초과면 20으로 조정
            given(recommendationService.getRecommendations(isNull(), eq(testUserId), eq(20)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .param("limit", "100")
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("includePopularFeeds=true로 피드 기반 추천 조회")
        void shouldReturnRecommendationsWithPopularFeeds() throws Exception {
            // Given
            given(recommendationService.getRecommendationsWithFeedReference(
                    isNull(), eq(testUserId), eq(true), eq(5)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .param("includePopularFeeds", "true")
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("음수 limit은 1로 조정")
        void shouldHandleNegativeLimit() throws Exception {
            // Given
            given(recommendationService.getRecommendations(isNull(), eq(testUserId), eq(1)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .param("limit", "-5")
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("모든 파라미터 조합 테스트")
        void shouldHandleAllParametersCombined() throws Exception {
            // Given
            int limit = 15;
            given(recommendationService.getRecommendationsWithFeedReference(
                    eq(testWeatherId), eq(testUserId), eq(true), eq(limit)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .param("weatherId", testWeatherId.toString())
                            .param("limit", String.valueOf(limit))
                            .param("includePopularFeeds", "true")
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("응답 구조 검증")
    class ResponseStructureTest {

        @Test
        @DisplayName("응답에 clothes 배열 포함")
        void shouldIncludeClothesArray() throws Exception {
            // Given
            given(recommendationService.getRecommendations(isNull(), eq(testUserId), eq(5)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.clothes").isArray())
                    .andExpect(jsonPath("$.clothes[0].clothesId").exists())
                    .andExpect(jsonPath("$.clothes[0].name").value("흰색 셔츠"))
                    .andExpect(jsonPath("$.clothes[0].imageUrl").exists())
                    .andExpect(jsonPath("$.clothes[0].type").value("TOP"));
        }

        @Test
        @DisplayName("응답에 outfits 배열 포함")
        void shouldIncludeOutfitsArray() throws Exception {
            // Given
            given(recommendationService.getRecommendations(isNull(), eq(testUserId), eq(5)))
                    .willReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.outfits").isArray())
                    .andExpect(jsonPath("$.outfits[0].score").value(75))
                    .andExpect(jsonPath("$.outfits[0].clothes").isArray());
        }

        @Test
        @DisplayName("빈 추천 결과 응답")
        void shouldHandleEmptyRecommendations() throws Exception {
            // Given
            RecommendationResponse emptyResponse = new RecommendationResponse(
                    testWeatherId,
                    testUserId,
                    List.of(),
                    List.of()
            );
            given(recommendationService.getRecommendations(isNull(), eq(testUserId), eq(5)))
                    .willReturn(emptyResponse);

            // When & Then
            mockMvc.perform(get("/api/recommendations")
                            .with(user(mockUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.clothes").isEmpty())
                    .andExpect(jsonPath("$.outfits").isEmpty());
        }
    }
}
