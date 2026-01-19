package com.codeit.closet.module.comment.service;

import com.codeit.closet.module.comment.dto.CommentCreateRequest;
import com.codeit.closet.module.comment.dto.CommentDTO;
import com.codeit.closet.module.comment.dto.CommentDTOCursorResponse;
import com.codeit.closet.module.comment.entity.Comment;
import com.codeit.closet.module.comment.mapper.CommentMapper;
import com.codeit.closet.module.comment.repository.CommentRepository;
import com.codeit.closet.module.comment.service.impl.BasicCommentService;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicCommentService 테스트")
class BasicCommentServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private FeedRepository feedRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentMapper commentMapper;

  @InjectMocks
  private BasicCommentService commentService;

  private UUID testUserId;
  private UUID testFeedId;
  private UUID testCommentId;
  private User testUser;
  private Feed testFeed;
  private Comment testComment;
  private CommentDTO testCommentDTO;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testFeedId = UUID.randomUUID();
    testCommentId = UUID.randomUUID();

    testUser = mock(User.class);
    lenient().when(testUser.getId()).thenReturn(testUserId);
    lenient().when(testUser.getName()).thenReturn("testuser");

    testFeed = mock(Feed.class);
    lenient().when(testFeed.getId()).thenReturn(testFeedId);

    testComment = mock(Comment.class);
    lenient().when(testComment.getId()).thenReturn(testCommentId);
    lenient().when(testComment.getContent()).thenReturn("테스트 댓글");
    lenient().when(testComment.getUser()).thenReturn(testUser);
    lenient().when(testComment.getFeed()).thenReturn(testFeed);

    testCommentDTO = mock(CommentDTO.class);
    lenient().when(testCommentDTO.content()).thenReturn("테스트 댓글");
  }

  @Test
  @DisplayName("댓글 생성 성공")
  void createComment_Success() {
    // given
    CommentCreateRequest request = new CommentCreateRequest(
        testFeedId,
        testUserId,
        "새로운 댓글"
    );

    when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
    when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

    // when
    CommentDTO result = commentService.createComment(testFeedId, request);

    // then
    assertThat(result).isNotNull();
    verify(feedRepository, times(1)).findById(testFeedId);
    verify(userRepository, times(1)).findById(testUserId);
    verify(commentRepository, times(1)).save(any(Comment.class));
    verify(commentMapper, times(1)).toDTO(testComment);
    verify(testFeed, times(1)).increaseCommentCount();
  }

  @Test
  @DisplayName("존재하지 않는 피드로 댓글 생성 시 예외 발생")
  void createComment_FeedNotFound() {
    // given
    CommentCreateRequest request = new CommentCreateRequest(
        UUID.randomUUID(),
        testUserId,
        "새로운 댓글"
    );

    when(feedRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.createComment(UUID.randomUUID(), request))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("존재하지 않는 피드 입니다");

    verify(feedRepository, times(1)).findById(any(UUID.class));
    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("존재하지 않는 사용자로 댓글 생성 시 예외 발생")
  void createComment_UserNotFound() {
    // given
    CommentCreateRequest request = new CommentCreateRequest(
        testFeedId,
        UUID.randomUUID(),
        "새로운 댓글"
    );

    when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
    when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.createComment(testFeedId, request))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("존재하지않는 회원입니다");

    verify(userRepository, times(1)).findById(any(UUID.class));
    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 목록 조회 성공")
  void getComments_Success() {
    // given
    String cursor = "testCursor";
    UUID idAfter = UUID.randomUUID();
    Integer limit = 20;
    CommentDTOCursorResponse expectedResponse = mock(CommentDTOCursorResponse.class);

    when(commentRepository.findCommentsByCursor(testFeedId, cursor, idAfter, limit))
        .thenReturn(expectedResponse);

    // when
    CommentDTOCursorResponse result = commentService.getComments(testFeedId, cursor, idAfter,
        limit);

    // then
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(expectedResponse);
    verify(commentRepository, times(1)).findCommentsByCursor(testFeedId, cursor, idAfter, limit);
  }

  @Test
  @DisplayName("cursor 없이 댓글 목록 조회")
  void getComments_WithoutCursor() {
    // given
    Integer limit = 20;
    CommentDTOCursorResponse expectedResponse = mock(CommentDTOCursorResponse.class);

    when(commentRepository.findCommentsByCursor(testFeedId, null, null, limit))
        .thenReturn(expectedResponse);

    // when
    CommentDTOCursorResponse result = commentService.getComments(testFeedId, null, null, limit);

    // then
    assertThat(result).isNotNull();
    verify(commentRepository, times(1)).findCommentsByCursor(testFeedId, null, null, limit);
  }

  @Test
  @DisplayName("빈 내용으로 댓글 생성 성공")
  void createComment_WithEmptyContent() {
    // given
    CommentCreateRequest request = new CommentCreateRequest(
        testFeedId,
        testUserId,
        ""
    );

    when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
    when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

    // when
    CommentDTO result = commentService.createComment(testFeedId, request);

    // then
    assertThat(result).isNotNull();
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @DisplayName("null 내용으로 댓글 생성 성공")
  void createComment_WithNullContent() {
    // given
    CommentCreateRequest request = new CommentCreateRequest(
        testFeedId,
        testUserId,
        null
    );

    when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
    when(commentMapper.toDTO(testComment)).thenReturn(testCommentDTO);

    // when
    CommentDTO result = commentService.createComment(testFeedId, request);

    // then
    assertThat(result).isNotNull();
    verify(commentRepository, times(1)).save(any(Comment.class));
  }
}
