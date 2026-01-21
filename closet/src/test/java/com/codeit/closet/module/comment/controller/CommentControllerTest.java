package com.codeit.closet.module.comment.controller;

import com.codeit.closet.module.comment.dto.CommentCreateRequest;
import com.codeit.closet.module.comment.dto.CommentDTO;
import com.codeit.closet.module.comment.dto.CommentDTOCursorResponse;
import com.codeit.closet.module.comment.service.CommentService;
import com.codeit.closet.module.user.dto.user.UserSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CommentController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.codeit.closet.common.config.SecurityConfig.class
        ))
@org.springframework.context.annotation.Import(com.codeit.closet.common.config.TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CommentController 테스트")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    private UUID testFeedId;
    private UUID testUserId;
    private UUID testCommentId;
    private CommentDTO testCommentDTO;
    private UserSummary testUserSummary;

    @BeforeEach
    void setUp() {
        testFeedId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testCommentId = UUID.randomUUID();

        testUserSummary = new UserSummary(
                testUserId,
                "testuser",
                null
        );

        testCommentDTO = new CommentDTO(
                testCommentId,
                Instant.now(),
                testFeedId,
                testUserSummary,
                "테스트 댓글 내용"
        );
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_Success() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest(
                testFeedId,
                testUserId,
                "새로운 댓글입니다"
        );

        when(commentService.createComment(eq(testFeedId), any(CommentCreateRequest.class)))
                .thenReturn(testCommentDTO);

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/comments", testFeedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testCommentId.toString()))
                .andExpect(jsonPath("$.feedId").value(testFeedId.toString()))
                .andExpect(jsonPath("$.content").value("테스트 댓글 내용"))
                .andExpect(jsonPath("$.author.userId").value(testUserId.toString()));

        verify(commentService, times(1)).createComment(eq(testFeedId), any(CommentCreateRequest.class));
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getComments_Success() throws Exception {
        // given
        List<CommentDTO> commentList = List.of(testCommentDTO);
        CommentDTOCursorResponse response = new CommentDTOCursorResponse(
                commentList,
                null,
                null,
                false,
                1L,
                "createdAt",
                "DESCENDING"
        );

        when(commentService.getComments(eq(testFeedId), isNull(), isNull(), eq(20)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/feeds/{feedId}/comments", testFeedId)
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testCommentId.toString()))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.sortBy").value("createdAt"))
                .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

        verify(commentService, times(1)).getComments(eq(testFeedId), isNull(), isNull(), eq(20));
    }

    @Test
    @DisplayName("댓글 목록 조회 - cursor와 함께")
    void getComments_WithCursor() throws Exception {
        // given
        String cursor = "eyJpZCI6IjNmYTg1ZjY0In0=";
        UUID idAfter = UUID.randomUUID();

        List<CommentDTO> commentList = List.of(testCommentDTO);
        CommentDTOCursorResponse response = new CommentDTOCursorResponse(
                commentList,
                cursor,
                idAfter,
                true,
                10L,
                "createdAt",
                "DESCENDING"
        );

        when(commentService.getComments(eq(testFeedId), eq(cursor), eq(idAfter), eq(10)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/feeds/{feedId}/comments", testFeedId)
                        .param("cursor", cursor)
                        .param("idAfter", idAfter.toString())
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(cursor))
                .andExpect(jsonPath("$.nextAfter").value(idAfter.toString()));

        verify(commentService, times(1)).getComments(eq(testFeedId), eq(cursor), eq(idAfter), eq(10));
    }

    @Test
    @DisplayName("빈 댓글 목록 조회")
    void getComments_EmptyList() throws Exception {
        // given
        CommentDTOCursorResponse emptyResponse = new CommentDTOCursorResponse(
                List.of(),
                null,
                null,
                false,
                0L,
                "createdAt",
                "DESCENDING"
        );

        when(commentService.getComments(eq(testFeedId), isNull(), isNull(), eq(20)))
                .thenReturn(emptyResponse);

        // when & then
        mockMvc.perform(get("/api/feeds/{feedId}/comments", testFeedId)
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(commentService, times(1)).getComments(eq(testFeedId), isNull(), isNull(), eq(20));
    }

    @Test
    @DisplayName("여러 댓글 목록 조회")
    void getComments_MultipleComments() throws Exception {
        // given
        UUID userId2 = UUID.randomUUID();
        UUID commentId2 = UUID.randomUUID();

        UserSummary userSummary2 = new UserSummary(
                userId2,
                "testuser2",
                null
        );

        CommentDTO commentDTO2 = new CommentDTO(
                commentId2,
                Instant.now(),
                testFeedId,
                userSummary2,
                "두 번째 댓글"
        );

        List<CommentDTO> commentList = List.of(testCommentDTO, commentDTO2);
        CommentDTOCursorResponse response = new CommentDTOCursorResponse(
                commentList,
                null,
                null,
                false,
                2L,
                "createdAt",
                "DESCENDING"
        );

        when(commentService.getComments(eq(testFeedId), isNull(), isNull(), eq(20)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/feeds/{feedId}/comments", testFeedId)
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.totalCount").value(2));

        verify(commentService, times(1)).getComments(eq(testFeedId), isNull(), isNull(), eq(20));
    }

    @Test
    @DisplayName("댓글 생성 - 빈 내용")
    void createComment_WithEmptyContent() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest(
                testFeedId,
                testUserId,
                ""
        );

        CommentDTO emptyContentDTO = new CommentDTO(
                testCommentId,
                Instant.now(),
                testFeedId,
                testUserSummary,
                ""
        );

        when(commentService.createComment(eq(testFeedId), any(CommentCreateRequest.class)))
                .thenReturn(emptyContentDTO);

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/comments", testFeedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(commentService, times(1)).createComment(eq(testFeedId), any(CommentCreateRequest.class));
    }

    @Test
    @DisplayName("댓글 생성 - 긴 내용")
    void createComment_WithLongContent() throws Exception {
        // given
        String longContent = "a".repeat(500);
        CommentCreateRequest request = new CommentCreateRequest(
                testFeedId,
                testUserId,
                longContent
        );

        CommentDTO longContentDTO = new CommentDTO(
                testCommentId,
                Instant.now(),
                testFeedId,
                testUserSummary,
                longContent
        );

        when(commentService.createComment(eq(testFeedId), any(CommentCreateRequest.class)))
                .thenReturn(longContentDTO);

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/comments", testFeedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(longContent));

        verify(commentService, times(1)).createComment(eq(testFeedId), any(CommentCreateRequest.class));
    }

    @Test
    @DisplayName("댓글 목록 조회 - 다양한 limit 값")
    void getComments_WithVariousLimits() throws Exception {
        // given
        List<CommentDTO> commentList = List.of(testCommentDTO);
        CommentDTOCursorResponse response = new CommentDTOCursorResponse(
                commentList,
                null,
                null,
                false,
                1L,
                "createdAt",
                "DESCENDING"
        );

        when(commentService.getComments(eq(testFeedId), isNull(), isNull(), eq(50)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/feeds/{feedId}/comments", testFeedId)
                        .param("limit", "50"))
                .andExpect(status().isOk());

        verify(commentService, times(1)).getComments(eq(testFeedId), isNull(), isNull(), eq(50));
    }

    @Test
    @DisplayName("존재하지 않는 피드로 댓글 생성 시 예외")
    void createComment_FeedNotFound() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest(
                testFeedId,
                testUserId,
                "댓글 내용"
        );

        when(commentService.createComment(eq(testFeedId), any(CommentCreateRequest.class)))
                .thenThrow(new java.util.NoSuchElementException("존재하지 않는 피드 입니다."));

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/comments", testFeedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        verify(commentService, times(1)).createComment(eq(testFeedId), any(CommentCreateRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 댓글 생성 시 예외")
    void createComment_UserNotFound() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest(
                testFeedId,
                testUserId,
                "댓글 내용"
        );

        when(commentService.createComment(eq(testFeedId), any(CommentCreateRequest.class)))
                .thenThrow(new java.util.NoSuchElementException("존재하지않는 회원입니다."));

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/comments", testFeedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        verify(commentService, times(1)).createComment(eq(testFeedId), any(CommentCreateRequest.class));
    }
}
