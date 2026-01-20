package com.codeit.closet.module.feed.repository;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.entity.Ootd;
import com.codeit.closet.module.user.entity.AuthProvider;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("FeedRepository 테스트")
class FeedRepositoryTest {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeatherRegionRepository weatherRegionRepository;

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Autowired
    private ClothRepository clothRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User testUser;
    private WeatherData weatherData;
    private Cloth testCloth;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .name("feeduser")
                .email("feed@example.com")
                .password("password123")
                .provider(AuthProvider.LOCAL)
                .providerId("feed-provider-id")
                .role(UserRole.USER)
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
                .windSpeed(2.0)
                .windAsWord(WindStrength.WEAK)
                .build();
        weatherData = weatherDataRepository.save(weatherData);

        // 테스트용 의상 생성
        testCloth = Cloth.builder()
                .owner(testUser)
                .name("테스트 셔츠")
                .type(ClothType.TOP)
                .build();
        testCloth = clothRepository.save(testCloth);
    }

    @Test
    @DisplayName("피드 저장 성공")
    void saveFeed_Success() {
        // given
        Feed feed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("오늘 날씨 좋네요!")
                .build();

        Ootd ootd = Ootd.builder().feed(feed).cloth(testCloth).build();
        feed.addOotd(ootd);

        // when
        Feed saved = feedRepository.save(feed);
        entityManager.flush();

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getContent()).isEqualTo("오늘 날씨 좋네요!");
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getWeather().getId()).isEqualTo(weatherData.getId());
        assertThat(saved.getOotds()).hasSize(1);
        assertThat(saved.getLikeCount()).isEqualTo(0L);
        assertThat(saved.getCommentCount()).isEqualTo(0L);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ID로 피드 조회 성공")
    void findById_Success() {
        // given
        Feed feed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("테스트 피드")
                .build();
        Feed saved = feedRepository.save(feed);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Feed> found = feedRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getContent()).isEqualTo("테스트 피드");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
    void findById_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<Feed> found = feedRepository.findById(nonExistentId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("피드 삭제 성공")
    void deleteFeed_Success() {
        // given
        Feed feed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("삭제할 피드")
                .build();
        Feed saved = feedRepository.save(feed);
        entityManager.flush();

        // when
        feedRepository.deleteById(saved.getId());
        entityManager.flush();

        // then
        Optional<Feed> found = feedRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("피드 업데이트 성공")
    void updateFeed_Success() {
        // given
        Feed feed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("원본 내용")
                .build();
        Feed saved = feedRepository.save(feed);
        entityManager.flush();
        entityManager.clear();

        // when
        Feed found = feedRepository.findById(saved.getId()).orElseThrow();
        found.updateFeed("수정된 내용");
        entityManager.flush();
        entityManager.clear();

        // then
        Feed updated = feedRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("모든 피드 조회")
    void findAll_Success() {
        // given
        Feed feed1 = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("첫 번째 피드")
                .build();
        Feed feed2 = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("두 번째 피드")
                .build();
        feedRepository.save(feed1);
        feedRepository.save(feed2);
        entityManager.flush();

        // when
        List<Feed> all = feedRepository.findAll();

        // then
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        assertThat(all).extracting("content")
                .contains("첫 번째 피드", "두 번째 피드");
    }

    @Test
    @DisplayName("피드 존재 여부 확인 - 존재하는 경우")
    void existsById_True() {
        // given
        Feed feed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("존재하는 피드")
                .build();
        Feed saved = feedRepository.save(feed);
        entityManager.flush();

        // when
        boolean exists = feedRepository.existsById(saved.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("피드 존재 여부 확인 - 존재하지 않는 경우")
    void existsById_False() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        boolean exists = feedRepository.existsById(nonExistentId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("여러 OOTD가 있는 피드 저장")
    void saveFeedWithMultipleOotds_Success() {
        // given
        Cloth cloth2 = Cloth.builder()
                .owner(testUser)
                .name("테스트 바지")
                .type(ClothType.BOTTOM)
                .build();
        clothRepository.save(cloth2);

        Feed feed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("오늘의 코디")
                .build();

        Ootd ootd1 = Ootd.builder()
            .feed(feed)
            .cloth(cloth2)
            .build();

        Ootd ootd2 = Ootd.builder()
            .feed(feed)
            .cloth(testCloth)
            .build();

        feed.addOotd(ootd1);
        feed.addOotd(ootd2);

        // when
        Feed saved = feedRepository.save(feed);
        entityManager.flush();
        entityManager.clear();

        // then
        Feed found = feedRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getOotds()).hasSize(2);
    }

    @Test
    @DisplayName("좋아요 수 증가 테스트")
    void increaseLikeCount_Success() {
        // given
        Feed feed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("좋아요 테스트")
                .build();
        Feed saved = feedRepository.save(feed);
        entityManager.flush();
        entityManager.clear();

        // when
        Feed found = feedRepository.findById(saved.getId()).orElseThrow();
        found.increaseLikeCount();
        found.increaseLikeCount();
        entityManager.flush();
        entityManager.clear();

        // then
        Feed updated = feedRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("좋아요 수 감소 테스트")
    void decreaseLikeCount_Success() {
        // given
        Feed feed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("좋아요 감소 테스트")
                .build();
        Feed saved = feedRepository.save(feed);
        entityManager.flush();
        entityManager.clear();

        Feed found = feedRepository.findById(saved.getId()).orElseThrow();
        found.increaseLikeCount();
        found.increaseLikeCount();
        found.increaseLikeCount();
        entityManager.flush();
        entityManager.clear();

        // when
        Feed updated = feedRepository.findById(saved.getId()).orElseThrow();
        updated.decreaseLikeCount();
        entityManager.flush();
        entityManager.clear();

        // then
        Feed result = feedRepository.findById(saved.getId()).orElseThrow();
        assertThat(result.getLikeCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("댓글 수 증가 테스트")
    void increaseCommentCount_Success() {
        // given
        Feed feed = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("댓글 테스트")
                .build();
        Feed saved = feedRepository.save(feed);
        entityManager.flush();
        entityManager.clear();

        // when
        Feed found = feedRepository.findById(saved.getId()).orElseThrow();
        found.increaseCommentCount();
        found.increaseCommentCount();
        entityManager.flush();
        entityManager.clear();

        // then
        Feed updated = feedRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getCommentCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("피드 개수 조회")
    void count_Success() {
        // given
        long initialCount = feedRepository.count();

        Feed feed1 = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("카운트 테스트 1")
                .build();
        Feed feed2 = Feed.builder()
                .user(testUser)
                .weather(weatherData)
                .content("카운트 테스트 2")
                .build();
        feedRepository.save(feed1);
        feedRepository.save(feed2);
        entityManager.flush();

        // when
        long count = feedRepository.count();

        // then
        assertThat(count).isEqualTo(initialCount + 2);
    }
}
