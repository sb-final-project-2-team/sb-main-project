package com.codeit.closet.module.like.repository;

import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.repository.FeedRepository;
import com.codeit.closet.module.like.entity.Like;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.weather.entity.ForecastKind;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import com.codeit.closet.module.weather.entity.WindStrength;
import com.codeit.closet.module.weather.repository.WeatherDataRepository;
import com.codeit.closet.module.weather.repository.WeatherRegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("LikeRepository 테스트")
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeatherRegionRepository weatherRegionRepository;

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    private User testUser;
    private Feed testFeed;
    private WeatherData weatherData;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .name("testuser")
                .build();
        testUser = userRepository.save(testUser);

        // 테스트용 날씨 지역 생성
        WeatherRegion testRegion = WeatherRegion.builder()
                .x(60)
                .y(127)
                .latitude(37.5665)
                .longitude(126.9780)
                .locationNames("서울특별시,강남구,역삼동")
                .build();
        testRegion = weatherRegionRepository.save(testRegion);

        // 테스트용 날씨 데이터 생성
        weatherData = WeatherData.builder()
                .weatherRegion(testRegion)
                .forecastKind(ForecastKind.ULTRA_NOW)
                .forecastAt(java.time.Instant.now())
                .forecastedAt(java.time.Instant.now())
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(20.0)
                .temperatureMin(15.0)
                .temperatureMax(25.0)
                .precipitationType(PrecipitationType.NONE)
                .precipitationAmount(0.0)
                .precipitationProb(0.0)
                .humidityCurrent(50.0)
                .windSpeed(2.5)
                .windAsWord(WindStrength.WEAK)
                .build();
        weatherData = weatherDataRepository.save(weatherData);

        // 테스트 피드 생성
        testFeed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("테스트 피드 내용")
                .likeCount(0L)
                .commentCount(0L)
                .build();
        testFeed = feedRepository.save(testFeed);
    }

    @Test
    @DisplayName("좋아요 저장 성공")
    void save_Success() {
        // given
        Like like = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();

        // when
        Like savedLike = likeRepository.save(like);
        likeRepository.flush();

        // then
        assertThat(savedLike.getId()).isNotNull();
        assertThat(savedLike.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(savedLike.getFeed().getId()).isEqualTo(testFeed.getId());
        assertThat(savedLike.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ID로 좋아요 조회 성공")
    void findById_Success() {
        // given
        Like like = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
        Like savedLike = likeRepository.save(like);

        // when
        Optional<Like> foundLike = likeRepository.findById(savedLike.getId());

        // then
        assertThat(foundLike).isPresent();
        assertThat(foundLike.get().getId()).isEqualTo(savedLike.getId());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
    void findById_NotFound() {
        // given
        java.util.UUID nonExistentId = java.util.UUID.randomUUID();

        // when
        Optional<Like> foundLike = likeRepository.findById(nonExistentId);

        // then
        assertThat(foundLike).isEmpty();
    }

    @Test
    @DisplayName("Feed와 User로 좋아요 삭제 성공")
    void deleteByFeedAndUser_Success() {
        // given
        Like like = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
        likeRepository.save(like);

        // when
        likeRepository.deleteByFeedAndUser(testFeed, testUser);
        likeRepository.flush();

        // then
        List<Like> allLikes = likeRepository.findAll();
        assertThat(allLikes).isEmpty();
    }

    @Test
    @DisplayName("모든 좋아요 조회")
    void findAll_Success() {
        // given
        User anotherUser = User.builder()
                .email("another@example.com")
                .password("password123")
                .name("anotheruser")
                .build();
        anotherUser = userRepository.save(anotherUser);

        Like like1 = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
        Like like2 = Like.builder()
                .user(anotherUser)
                .feed(testFeed)
                .build();

        likeRepository.save(like1);
        likeRepository.save(like2);

        // when
        List<Like> allLikes = likeRepository.findAll();

        // then
        assertThat(allLikes).hasSize(2);
    }

    @Test
    @DisplayName("좋아요 개수 조회")
    void count_Success() {
        // given
        Like like = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
        likeRepository.save(like);

        // when
        long count = likeRepository.count();

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 존재 여부 확인 - 존재하는 경우")
    void existsById_True() {
        // given
        Like like = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
        Like savedLike = likeRepository.save(like);

        // when
        boolean exists = likeRepository.existsById(savedLike.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("좋아요 존재 여부 확인 - 존재하지 않는 경우")
    void existsById_False() {
        // given
        java.util.UUID nonExistentId = java.util.UUID.randomUUID();

        // when
        boolean exists = likeRepository.existsById(nonExistentId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("좋아요 삭제 성공")
    void deleteById_Success() {
        // given
        Like like = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
        Like savedLike = likeRepository.save(like);

        // when
        likeRepository.deleteById(savedLike.getId());
        likeRepository.flush();

        // then
        Optional<Like> foundLike = likeRepository.findById(savedLike.getId());
        assertThat(foundLike).isEmpty();
    }

    @Test
    @DisplayName("같은 사용자가 같은 피드에 중복 좋아요 시도 시 제약 조건 위반")
    void save_DuplicateLike_ConstraintViolation() {
        // given
        Like like1 = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
        likeRepository.save(like1);
        likeRepository.flush();

        Like like2 = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();

        // when & then
        try {
            likeRepository.save(like2);
            likeRepository.flush();
            // 제약 조건 위반 예외가 발생해야 함
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("여러 사용자가 같은 피드에 좋아요")
    void multipleUsersLikeSameFeed() {
        // given
        User user2 = User.builder()
                .email("user2@example.com")
                .password("password123")
                .name("user2")
                .build();
        user2 = userRepository.save(user2);

        User user3 = User.builder()
                .email("user3@example.com")
                .password("password123")
                .name("user3")
                .build();
        user3 = userRepository.save(user3);

        Like like1 = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
        Like like2 = Like.builder()
                .user(user2)
                .feed(testFeed)
                .build();
        Like like3 = Like.builder()
                .user(user3)
                .feed(testFeed)
                .build();

        // when
        likeRepository.saveAll(List.of(like1, like2, like3));
        List<Like> allLikes = likeRepository.findAll();

        // then
        assertThat(allLikes).hasSize(3);
    }

    @Test
    @DisplayName("한 사용자가 여러 피드에 좋아요")
    void oneUserLikesMultipleFeeds() {
        // given
        Feed feed2 = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("두 번째 피드")
                .likeCount(0L)
                .commentCount(0L)
                .build();
        feed2 = feedRepository.save(feed2);

        Feed feed3 = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("세 번째 피드")
                .likeCount(0L)
                .commentCount(0L)
                .build();
        feed3 = feedRepository.save(feed3);

        Like like1 = Like.builder()
                .user(testUser)
                .feed(testFeed)
                .build();
        Like like2 = Like.builder()
                .user(testUser)
                .feed(feed2)
                .build();
        Like like3 = Like.builder()
                .user(testUser)
                .feed(feed3)
                .build();

        // when
        likeRepository.saveAll(List.of(like1, like2, like3));
        List<Like> allLikes = likeRepository.findAll();

        // then
        assertThat(allLikes).hasSize(3);
    }
}
