package com.codeit.closet.module.like.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.like.service.LikeService;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = LikeController.class,
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
@DisplayName("LikeController 테스트")
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LikeService likeService;

    private UUID testUserId;
    private UUID testFeedId;
    private ClosetUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testFeedId = UUID.randomUUID();

        UserDTO userDTO = new UserDTO(
                testUserId,
                Instant.now(),
                "test@example.com",
                "testuser",
                UserRole.USER,
                false
        );
        mockUserDetails = new ClosetUserDetails(userDTO, "password123", null, null);
    }

    @Test
    @DisplayName("좋아요 생성 성공")
    void createLike_Success() throws Exception {
        // given
        doNothing().when(likeService).createLike(testFeedId, testUserId);

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/like", testFeedId)
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk());

        verify(likeService, times(1)).createLike(testFeedId, testUserId);
    }

    @Test
    @DisplayName("좋아요 생성 실패 - 존재하지 않는 Feed")
    void createLike_FeedNotFound() throws Exception {
        // given
        doThrow(new NoSuchElementException("존재하지 않는 피드입니다."))
                .when(likeService).createLike(testFeedId, testUserId);

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/like", testFeedId)
                        .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

        verify(likeService, times(1)).createLike(testFeedId, testUserId);
    }

    @Test
    @DisplayName("좋아요 생성 실패 - 존재하지 않는 User")
    void createLike_UserNotFound() throws Exception {
        // given
        doThrow(new NoSuchElementException("존재하지 않는 유저입니다."))
                .when(likeService).createLike(testFeedId, testUserId);

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/like", testFeedId)
                        .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

        verify(likeService, times(1)).createLike(testFeedId, testUserId);
    }

    @Test
    @DisplayName("좋아요 삭제 성공")
    void deleteLike_Success() throws Exception {
        // given
        doNothing().when(likeService).deleteLike(testFeedId, testUserId);

        // when & then
        mockMvc.perform(delete("/api/feeds/{feedId}/like", testFeedId)
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk());

        verify(likeService, times(1)).deleteLike(testFeedId, testUserId);
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 존재하지 않는 Feed")
    void deleteLike_FeedNotFound() throws Exception {
        // given
        doThrow(new NoSuchElementException("존재하지 않는 피드입니다."))
                .when(likeService).deleteLike(testFeedId, testUserId);

        // when & then
        mockMvc.perform(delete("/api/feeds/{feedId}/like", testFeedId)
                        .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

        verify(likeService, times(1)).deleteLike(testFeedId, testUserId);
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 존재하지 않는 User")
    void deleteLike_UserNotFound() throws Exception {
        // given
        doThrow(new NoSuchElementException("존재하지 않는 유저입니다."))
                .when(likeService).deleteLike(testFeedId, testUserId);

        // when & then
        mockMvc.perform(delete("/api/feeds/{feedId}/like", testFeedId)
                        .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

        verify(likeService, times(1)).deleteLike(testFeedId, testUserId);
    }

    @Test
    @DisplayName("인증되지 않은 사용자 좋아요 생성 시도")
    void createLike_Unauthorized() throws Exception {
        // when & then
        // TestSecurityConfig에서 모든 요청을 permitAll로 설정했으므로
        // 인증 없이 호출하면 userDetails가 null이 되어 500 에러 발생
        mockMvc.perform(post("/api/feeds/{feedId}/like", testFeedId))
                .andExpect(status().is5xxServerError());

        verify(likeService, never()).createLike(any(), any());
    }

    @Test
    @DisplayName("인증되지 않은 사용자 좋아요 삭제 시도")
    void deleteLike_Unauthorized() throws Exception {
        // when & then
        // TestSecurityConfig에서 모든 요청을 permitAll로 설정했으므로
        // 인증 없이 호출하면 userDetails가 null이 되어 500 에러 발생
        mockMvc.perform(delete("/api/feeds/{feedId}/like", testFeedId))
                .andExpect(status().is5xxServerError());

        verify(likeService, never()).deleteLike(any(), any());
    }
}
