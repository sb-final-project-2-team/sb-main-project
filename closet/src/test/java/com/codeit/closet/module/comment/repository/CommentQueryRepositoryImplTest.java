package com.codeit.closet.module.comment.repository;

import com.codeit.closet.module.comment.dto.CommentDTOCursorResponse;
import com.codeit.closet.module.comment.entity.Comment;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.user.entity.AuthProvider;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.weather.entity.*;
import com.codeit.closet.module.weather.repository.WeatherDataRepository;
import com.codeit.closet.module.weather.repository.WeatherRegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("CommentQueryRepository 통합 테스트")
class CommentQueryRepositoryImplTest {

  @Autowired
  @Qualifier("commentQueryRepositoryImpl")
  private CommentQueryRepository commentQueryRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private FeedRepository feedRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private WeatherRegionRepository weatherRegionRepository;

  @Autowired
  private WeatherDataRepository weatherDataRepository;

  @Autowired
  private jakarta.persistence.EntityManager entityManager;

  private User testUser1;
  private User testUser2;
  private WeatherData weatherData1;
  private Feed testFeed1;
  private Feed testFeed2;

  @BeforeEach
  void setUp() {
    testUser1 = User.builder()
        .name("user1")
        .email("user1@example.com")
        .password("password123")
        .provider(AuthProvider.LOCAL)
        .providerId("user1-provider")
        .role(UserRole.USER)
        .build();
    testUser1 = userRepository.save(testUser1);

    testUser2 = User.builder()
        .name("user2")
        .email("user2@example.com")
        .password("password123")
        .provider(AuthProvider.LOCAL)
        .providerId("user2-provider")
        .role(UserRole.USER)
        .build();
    testUser2 = userRepository.save(testUser2);

    WeatherRegion tempRegion1 = WeatherRegion.builder()
        .x(60)
        .y(127)
        .latitude(37.5665)
        .longitude(126.9780)
        .locationNames("서울특별시,강남구,역삼동")
        .build();
    tempRegion1 = weatherRegionRepository.save(tempRegion1);
    entityManager.flush();

    weatherData1 = WeatherData.builder()
        .weatherRegion(tempRegion1)
        .forecastKind(ForecastKind.ULTRA_NOW)
        .forecastAt(Instant.now())
        .forecastedAt(Instant.now())
        .skyStatus(SkyStatus.CLEAR)
        .temperatureCurrent(20.0)
        .temperatureMin(15.0)
        .temperatureMax(25.0)
        .precipitationType(PrecipitationType.NONE)
        .precipitationAmount(0.0)
        .precipitationProb(0.0)
        .humidityCurrent(50.0)
        .windSpeed(2.0)
        .windAsWord(WindStrength.WEAK)
        .build();
    weatherData1 = weatherDataRepository.save(weatherData1);
    entityManager.flush();

    entityManager.createNativeQuery(
            "UPDATE weather_regions SET weather_data_id = :weatherDataId WHERE id = :regionId")
        .setParameter("weatherDataId", weatherData1.getId())
        .setParameter("regionId", tempRegion1.getId())
        .executeUpdate();
    entityManager.flush();
    entityManager.clear();

    testFeed1 = Feed.builder()
        .user(testUser1)
        .weather(weatherData1)
        .content("첫 번째 피드")
        .build();
    testFeed1 = feedRepository.save(testFeed1);

    testFeed2 = Feed.builder()
        .user(testUser2)
        .weather(weatherData1)
        .content("두 번째 피드")
        .build();
    testFeed2 = feedRepository.save(testFeed2);

    entityManager.flush();
  }

  @Test
  @DisplayName("기본 댓글 조회 성공")
  void findCommentsByCursor_Basic() {
    // given
    Comment comment1 = Comment.builder()
        .user(testUser1)
        .feed(testFeed1)
        .content("첫 번째 댓글")
        .build();
    commentRepository.save(comment1);

    Comment comment2 = Comment.builder()
        .user(testUser2)
        .feed(testFeed1)
        .content("두 번째 댓글")
        .build();
    commentRepository.save(comment2);
    entityManager.flush();
    entityManager.clear();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), null, null, 10
    );

    // then
    assertThat(result.data()).hasSize(2);
    assertThat(result.sortBy()).isEqualTo("createdAt");
    assertThat(result.sortDirection()).isEqualTo("DESCENDING");
  }

  @Test
  @DisplayName("limit 테스트")
  void findCommentsByCursor_WithLimit() {
    // given
    for (int i = 0; i < 5; i++) {
      Comment comment = Comment.builder()
          .user(testUser1)
          .feed(testFeed1)
          .content("댓글 " + i)
          .build();
      commentRepository.save(comment);
    }
    entityManager.flush();
    entityManager.clear();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), null, null, 3
    );

    // then
    assertThat(result.data()).hasSize(3);
  }

  @Test
  @DisplayName("hasNext 테스트")
  void findCommentsByCursor_HasNext() {
    // given
    for (int i = 0; i < 5; i++) {
      Comment comment = Comment.builder()
          .user(testUser1)
          .feed(testFeed1)
          .content("댓글 " + i)
          .build();
      commentRepository.save(comment);
      testFeed1.increaseCommentCount();
    }

    entityManager.flush();
    entityManager.clear();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), null, null, 3
    );

    // then
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.nextAfter()).isNotNull();
  }

  @Test
  @DisplayName("cursor 페이징 테스트")
  void findCommentsByCursor_WithCursor() {
    // given
    for (int i = 0; i < 5; i++) {
      Comment comment = Comment.builder()
          .user(testUser1)
          .feed(testFeed1)
          .content("댓글 " + i)
          .build();
      commentRepository.save(comment);
    }
    entityManager.flush();
    entityManager.clear();

    // when - 첫 페이지
    CommentDTOCursorResponse firstPage = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), null, null, 2
    );

    // when - 두 번째 페이지
    CommentDTOCursorResponse secondPage = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), firstPage.nextCursor(), firstPage.nextAfter(), 2
    );

    // then
    assertThat(firstPage.data()).hasSize(2);
    assertThat(secondPage.data()).isNotEmpty();
    assertThat(firstPage.data().get(0).id()).isNotEqualTo(secondPage.data().get(0).id());
  }

  @Test
  @DisplayName("빈 결과 조회")
  void findCommentsByCursor_EmptyResult() {
    // given
    UUID nonExistentFeedId = UUID.randomUUID();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        nonExistentFeedId, null, null, 10
    );

    // then
    assertThat(result.data()).isEmpty();
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.nextAfter()).isNull();
  }

  @Test
  @DisplayName("totalCount 확인")
  void findCommentsByCursor_TotalCount() {
    // given
    for (int i = 0; i < 3; i++) {
      Comment comment = Comment.builder()
          .user(testUser1)
          .feed(testFeed1)
          .content("댓글 " + i)
          .build();
      commentRepository.save(comment);
      testFeed1.increaseCommentCount();
    }

    entityManager.flush();
    entityManager.clear();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), null, null, 10
    );

    // then
    assertThat(result.totalCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("여러 사용자의 댓글 조회")
  void findCommentsByCursor_MultipleUsers() {
    // given
    Comment comment1 = Comment.builder()
        .user(testUser1)
        .feed(testFeed1)
        .content("사용자1의 댓글")
        .build();
    commentRepository.save(comment1);

    Comment comment2 = Comment.builder()
        .user(testUser2)
        .feed(testFeed1)
        .content("사용자2의 댓글")
        .build();
    commentRepository.save(comment2);

    Comment comment3 = Comment.builder()
        .user(testUser1)
        .feed(testFeed1)
        .content("사용자1의 두 번째 댓글")
        .build();
    commentRepository.save(comment3);

    entityManager.flush();
    entityManager.clear();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), null, null, 10
    );

    // then
    assertThat(result.data()).hasSize(3);
    assertThat(result.data()).anyMatch(dto -> dto.author().userId().equals(testUser1.getId()));
    assertThat(result.data()).anyMatch(dto -> dto.author().userId().equals(testUser2.getId()));
  }

  @Test
  @DisplayName("피드별 댓글 필터링")
  void findCommentsByCursor_FilterByFeed() {
    // given
    Comment comment1 = Comment.builder()
        .user(testUser1)
        .feed(testFeed1)
        .content("첫 번째 피드의 댓글")
        .build();
    commentRepository.save(comment1);

    Comment comment2 = Comment.builder()
        .user(testUser1)
        .feed(testFeed2)
        .content("두 번째 피드의 댓글")
        .build();
    commentRepository.save(comment2);

    entityManager.flush();
    entityManager.clear();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), null, null, 10
    );

    // then
    assertThat(result.data()).hasSize(1);
    assertThat(result.data()).allMatch(dto -> dto.feedId().equals(testFeed1.getId()));
  }

  @Test
  @DisplayName("정렬 테스트 - 최신순")
  void findCommentsByCursor_SortByCreatedAt() {
    // given
    Comment comment1 = Comment.builder()
        .user(testUser1)
        .feed(testFeed1)
        .content("첫 번째 댓글")
        .build();
    commentRepository.save(comment1);
    entityManager.flush();

    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    Comment comment2 = Comment.builder()
        .user(testUser1)
        .feed(testFeed1)
        .content("두 번째 댓글")
        .build();
    commentRepository.save(comment2);
    entityManager.flush();
    entityManager.clear();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), null, null, 10
    );

    // then
    assertThat(result.data()).hasSize(2);
    assertThat(result.data().get(0).content()).isEqualTo("두 번째 댓글");
    assertThat(result.data().get(1).content()).isEqualTo("첫 번째 댓글");
  }

  @Test
  @DisplayName("잘못된 cursor 처리")
  void findCommentsByCursor_InvalidCursor() {
    // given
    Comment comment = Comment.builder()
        .user(testUser1)
        .feed(testFeed1)
        .content("댓글")
        .build();
    commentRepository.save(comment);
    entityManager.flush();
    entityManager.clear();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        testFeed1.getId(), "invalidCursor", null, 10
    );

    // then
    assertThat(result.data()).isNotEmpty();
  }

  @Test
  @DisplayName("null feedId 처리")
  void findCommentsByCursor_NullFeedId() {
    // given
    Comment comment1 = Comment.builder()
        .user(testUser1)
        .feed(testFeed1)
        .content("첫 번째 피드 댓글")
        .build();
    commentRepository.save(comment1);

    Comment comment2 = Comment.builder()
        .user(testUser1)
        .feed(testFeed2)
        .content("두 번째 피드 댓글")
        .build();
    commentRepository.save(comment2);

    entityManager.flush();
    entityManager.clear();

    // when
    CommentDTOCursorResponse result = commentQueryRepository.findCommentsByCursor(
        null, null, null, 10
    );

    // then
    assertThat(result.data()).hasSizeGreaterThanOrEqualTo(2);
  }
}
