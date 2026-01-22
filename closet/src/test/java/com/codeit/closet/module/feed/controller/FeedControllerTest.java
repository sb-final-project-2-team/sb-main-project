package com.codeit.closet.module.feed.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.feed.dto.*;
import com.codeit.closet.module.feed.service.FeedService;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.dto.user.UserSummary;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.weather.dto.weather.PrecipitationDTO;
import com.codeit.closet.module.weather.dto.weather.TemperatureDTO;
import com.codeit.closet.module.weather.dto.weather.WeatherSummaryDTO;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = FeedController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.codeit.closet.common.config.SecurityConfig.class
        ))
@Import({com.codeit.closet.common.config.TestSecurityConfig.class, com.codeit.closet.common.exception.GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("FeedController 테스트")
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeedService feedService;

    private UUID testUserId;
    private UUID testFeedId;
    private UUID testWeatherId;
    private FeedDTO testFeedDTO;
    private ClosetUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testFeedId = UUID.randomUUID();
        testWeatherId = UUID.randomUUID();

        UserDTO userDTO = new UserDTO(
                testUserId,
                Instant.now(),
                "test@example.com",
                "testuser",
                UserRole.USER,
                false
        );

        mockUserDetails = new ClosetUserDetails(
                userDTO,
                "password",
                null,
                null
        );

        UserSummary author = new UserSummary(
                testUserId,
                "testuser",
                "https://example.com/profile.jpg"
        );

        TemperatureDTO temperature = new TemperatureDTO(
                20.0,
                5.0,
                15.0,
                25.0
        );

        PrecipitationDTO precipitation = new PrecipitationDTO(
                PrecipitationType.NONE,
                0.0,
                0.0
        );

        WeatherSummaryDTO weather = new WeatherSummaryDTO(
                testWeatherId,
                SkyStatus.CLEAR,
                precipitation,
                temperature
        );

        testFeedDTO = new FeedDTO(
                testFeedId,
                Instant.now(),
                Instant.now(),
                author,
                weather,
                List.of(),
                "Test feed content",
                10L,
                5,
                false
        );
    }

    @Test
    @DisplayName("GET /api/feeds - 피드 목록 조회 성공")
    void getFeeds_Success() throws Exception {
        // given
        FeedDTOCursorResponse response = new FeedDTOCursorResponse(
                List.of(testFeedDTO),
                "nextCursor123",
                UUID.randomUUID(),
                true,
                1L,
                "createdAt",
                "DESCENDING"
        );

        when(feedService.findFeeds(
                isNull(),
                isNull(),
                eq(20),
                eq("createdAt"),
                eq("DESCENDING"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(testUserId)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("limit", "20")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESCENDING")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testFeedId.toString()))
                .andExpect(jsonPath("$.data[0].content").value("Test feed content"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.sortBy").value("createdAt"))
                .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

        verify(feedService, times(1)).findFeeds(
                isNull(),
                isNull(),
                eq(20),
                eq("createdAt"),
                eq("DESCENDING"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(testUserId)
        );
    }

    @Test
    @DisplayName("GET /api/feeds - 필터 적용 피드 목록 조회")
    void getFeeds_WithFilters() throws Exception {
        // given
        UUID authorId = UUID.randomUUID();
        FeedDTOCursorResponse response = new FeedDTOCursorResponse(
                List.of(testFeedDTO),
                null,
                null,
                false,
                1L,
                "createdAt",
                "ASCENDING"
        );

        when(feedService.findFeeds(
                isNull(),
                isNull(),
                eq(10),
                eq("createdAt"),
                eq("ASCENDING"),
                eq("keyword"),
                eq(SkyStatus.CLEAR),
                eq(PrecipitationType.RAIN),
                eq(authorId),
                eq(testUserId)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("limit", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "ASCENDING")
                        .param("keywordLike", "keyword")
                        .param("skyStatusEqual", "CLEAR")
                        .param("precipitationTypeEqual", "RAIN")
                        .param("authorIdEqual", authorId.toString())
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(feedService, times(1)).findFeeds(
                isNull(),
                isNull(),
                eq(10),
                eq("createdAt"),
                eq("ASCENDING"),
                eq("keyword"),
                eq(SkyStatus.CLEAR),
                eq(PrecipitationType.RAIN),
                eq(authorId),
                eq(testUserId)
        );
    }

    @Test
    @DisplayName("GET /api/feeds - 커서 기반 페이징")
    void getFeeds_WithCursor() throws Exception {
        // given
        String cursor = "cursorValue123";
        UUID idAfter = UUID.randomUUID();

        FeedDTOCursorResponse response = new FeedDTOCursorResponse(
                List.of(testFeedDTO),
                "nextCursor456",
                UUID.randomUUID(),
                true,
                5L,
                "createdAt",
                "DESCENDING"
        );

        when(feedService.findFeeds(
                eq(cursor),
                eq(idAfter),
                eq(15),
                eq("createdAt"),
                eq("DESCENDING"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(testUserId)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("cursor", cursor)
                        .param("idAfter", idAfter.toString())
                        .param("limit", "15")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESCENDING")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextCursor").value("nextCursor456"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalCount").value(5));
    }

    @Test
    @DisplayName("POST /api/feeds - 피드 생성 성공")
    void createFeed_Success() throws Exception {
        // given
        List<UUID> clothesIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        FeedCreateRequest request = new FeedCreateRequest(
                testUserId,
                testWeatherId,
                clothesIds,
                "New feed content"
        );

        when(feedService.createFeed(any(FeedCreateRequest.class)))
                .thenReturn(testFeedDTO);

        // when & then
        mockMvc.perform(post("/api/feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testFeedId.toString()))
                .andExpect(jsonPath("$.content").value("Test feed content"))
                .andExpect(jsonPath("$.author.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.likeCount").value(10))
                .andExpect(jsonPath("$.commentCount").value(5));

        verify(feedService, times(1)).createFeed(any(FeedCreateRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/feeds/{feedId} - 피드 삭제 성공")
    void deleteFeed_Success() throws Exception {
        // given
        doNothing().when(feedService).deleteFeed(testFeedId);

        // when & then
        mockMvc.perform(delete("/api/feeds/{feedId}", testFeedId))
                .andExpect(status().isNoContent());

        verify(feedService, times(1)).deleteFeed(testFeedId);
    }

    @Test
    @DisplayName("PATCH /api/feeds/{feedId} - 피드 수정 성공")
    void updateFeed_Success() throws Exception {
        // given
        FeedUpdateRequest request = new FeedUpdateRequest("Updated feed content");

        FeedDTO updatedFeed = new FeedDTO(
                testFeedId,
                testFeedDTO.createdAt(),
                Instant.now(),
                testFeedDTO.author(),
                testFeedDTO.weather(),
                testFeedDTO.ootds(),
                "Updated feed content",
                testFeedDTO.likeCount(),
                testFeedDTO.commentCount(),
                testFeedDTO.likedByMe()
        );

        when(feedService.updateFeed(eq(testFeedId), any(FeedUpdateRequest.class)))
                .thenReturn(updatedFeed);

        // when & then
        mockMvc.perform(patch("/api/feeds/{feedId}", testFeedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFeedId.toString()))
                .andExpect(jsonPath("$.content").value("Updated feed content"));

        verify(feedService, times(1)).updateFeed(eq(testFeedId), any(FeedUpdateRequest.class));
    }

    @Test
    @DisplayName("GET /api/feeds - 빈 결과 반환")
    void getFeeds_EmptyResult() throws Exception {
        // given
        FeedDTOCursorResponse emptyResponse = FeedDTOCursorResponse.empty("createdAt", "DESCENDING");

        when(feedService.findFeeds(
                isNull(),
                isNull(),
                eq(20),
                eq("createdAt"),
                eq("DESCENDING"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(testUserId)
        )).thenReturn(emptyResponse);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("limit", "20")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESCENDING")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalCount").value(0));
    }
}
