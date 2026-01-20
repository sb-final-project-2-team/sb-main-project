package com.codeit.closet.module.like.entity;

import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Like Entity 테스트")
class LikeEntityTest {

    @Test
    @DisplayName("Like 엔티티 빌더로 생성 성공")
    void buildLike_Success() {
        // given
        User user = mock(User.class);
        Feed feed = mock(Feed.class);

        // when
        Like like = Like.builder()
                .user(user)
                .feed(feed)
                .build();

        // then
        assertThat(like).isNotNull();
        assertThat(like.getUser()).isEqualTo(user);
        assertThat(like.getFeed()).isEqualTo(feed);
    }

    @Test
    @DisplayName("Like 엔티티 Getter 테스트")
    void likeGetters_Success() {
        // given
        User user = mock(User.class);
        Feed feed = mock(Feed.class);

        // when
        Like like = Like.builder()
                .user(user)
                .feed(feed)
                .build();

        // then
        assertThat(like.getUser()).isNotNull();
        assertThat(like.getFeed()).isNotNull();
        assertThat(like.getId()).isNull(); // ID는 영속화 전이므로 null
        assertThat(like.getCreatedAt()).isNull(); // CreationTimestamp는 영속화 시 설정됨
    }

    @Test
    @DisplayName("Like 엔티티 생성 시 필수 필드 확인")
    void likeRequiredFields_Success() {
        // given
        User user = mock(User.class);
        Feed feed = mock(Feed.class);

        // when
        Like like = Like.builder()
                .user(user)
                .feed(feed)
                .build();

        // then
        assertThat(like.getUser()).isNotNull();
        assertThat(like.getFeed()).isNotNull();
    }
}
