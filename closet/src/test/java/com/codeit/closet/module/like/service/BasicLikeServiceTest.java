package com.codeit.closet.module.like.service;

import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.like.entity.Like;
import com.codeit.closet.module.like.repository.LikeRepository;
import com.codeit.closet.module.like.service.impl.BasicLikeService;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicLikeService 테스트")
class BasicLikeServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BasicLikeService likeService;

    private UUID testUserId;
    private UUID testFeedId;
    private User testUser;
    private Feed testFeed;
    private Like testLike;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testFeedId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .password("password123")
                .name("testuser")
                .build();

        testFeed = Feed.builder()
                .user(testUser)
                .content("테스트 피드 내용")
                .likeCount(0L)
                .commentCount(0L)
                .build();

        testLike = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
    }

    @Test
    @DisplayName("좋아요 생성 성공")
    void createLike_Success() {
        // given
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(likeRepository.save(any(Like.class))).thenReturn(testLike);

        // when
        likeService.createLike(testFeedId, testUserId);

        // then
        verify(feedRepository, times(1)).findById(testFeedId);
        verify(userRepository, times(1)).findById(testUserId);
        verify(likeRepository, times(1)).save(any(Like.class));

        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("좋아요 생성 시 Feed 좋아요 수 증가")
    void createLike_IncreaseLikeCount() {
        // given
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(likeRepository.save(any(Like.class))).thenReturn(testLike);

        long initialLikeCount = testFeed.getLikeCount();

        // when
        likeService.createLike(testFeedId, testUserId);

        // then
        assertThat(testFeed.getLikeCount()).isEqualTo(initialLikeCount + 1);
    }

    @Test
    @DisplayName("좋아요 생성 실패 - 존재하지 않는 Feed")
    void createLike_FeedNotFound() {
        // given
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.createLike(testFeedId, testUserId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 피드입니다.");

        verify(feedRepository, times(1)).findById(testFeedId);
        verify(userRepository, never()).findById(any());
        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요 생성 실패 - 존재하지 않는 User")
    void createLike_UserNotFound() {
        // given
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.createLike(testFeedId, testUserId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 유저입니다.");

        verify(feedRepository, times(1)).findById(testFeedId);
        verify(userRepository, times(1)).findById(testUserId);
        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요 삭제 성공")
    void deleteLike_Success() {
        // given
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(likeRepository).deleteByFeedAndUser(testFeed, testUser);

        // when
        likeService.deleteLike(testFeedId, testUserId);

        // then
        verify(feedRepository, times(1)).findById(testFeedId);
        verify(userRepository, times(1)).findById(testUserId);
        verify(likeRepository, times(1)).deleteByFeedAndUser(testFeed, testUser);
    }

    @Test
    @DisplayName("좋아요 삭제 시 Feed 좋아요 수 감소")
    void deleteLike_DecreaseLikeCount() {
        // given
        testFeed.increaseLikeCount(); // 좋아요 수를 1로 설정
        long initialLikeCount = testFeed.getLikeCount();

        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(likeRepository).deleteByFeedAndUser(testFeed, testUser);

        // when
        likeService.deleteLike(testFeedId, testUserId);

        // then
        assertThat(testFeed.getLikeCount()).isEqualTo(initialLikeCount - 1);
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 존재하지 않는 Feed")
    void deleteLike_FeedNotFound() {
        // given
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.deleteLike(testFeedId, testUserId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 피드입니다.");

        verify(feedRepository, times(1)).findById(testFeedId);
        verify(userRepository, never()).findById(any());
        verify(likeRepository, never()).deleteByFeedAndUser(any(), any());
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 존재하지 않는 User")
    void deleteLike_UserNotFound() {
        // given
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.deleteLike(testFeedId, testUserId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("존재하지 않는 유저입니다.");

        verify(feedRepository, times(1)).findById(testFeedId);
        verify(userRepository, times(1)).findById(testUserId);
        verify(likeRepository, never()).deleteByFeedAndUser(any(), any());
    }

    @Test
    @DisplayName("좋아요 생성 시 정확한 엔티티 저장 확인")
    void createLike_SaveCorrectEntity() {
        // given
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(likeRepository.save(any(Like.class))).thenAnswer(invocation -> {
            Like savedLike = invocation.getArgument(0);
            assertThat(savedLike.getUser()).isEqualTo(testUser);
            assertThat(savedLike.getFeed()).isEqualTo(testFeed);
            return savedLike;
        });

        // when
        likeService.createLike(testFeedId, testUserId);

        // then
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    @DisplayName("좋아요 삭제 시 정확한 파라미터로 삭제 확인")
    void deleteLike_DeleteWithCorrectParameters() {
        // given
        testFeed.increaseLikeCount();
        when(feedRepository.findById(testFeedId)).thenReturn(Optional.of(testFeed));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        doAnswer(invocation -> {
            Feed feed = invocation.getArgument(0);
            User user = invocation.getArgument(1);
            assertThat(feed).isEqualTo(testFeed);
            assertThat(user).isEqualTo(testUser);
            return null;
        }).when(likeRepository).deleteByFeedAndUser(any(Feed.class), any(User.class));

        // when
        likeService.deleteLike(testFeedId, testUserId);

        // then
        verify(likeRepository, times(1)).deleteByFeedAndUser(testFeed, testUser);
    }
}
