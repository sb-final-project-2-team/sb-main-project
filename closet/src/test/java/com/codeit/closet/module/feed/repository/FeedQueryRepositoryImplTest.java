package com.codeit.closet.module.feed.repository;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.mapper.FeedMapper;
import com.codeit.closet.module.like.entity.Like;
import com.codeit.closet.module.like.repository.LikeRepository;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("FeedQueryRepository 통합 테스트")
class FeedQueryRepositoryImplTest {

    @Autowired
    @Qualifier("feedQueryRepositoryImpl")
    private FeedQueryRepository feedQueryRepository;

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
    private LikeRepository likeRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User testUser1;
    private User testUser2;
    private WeatherRegion testWeather1;
    private WeatherRegion testWeather2;
    private WeatherData weatherData1;
    private WeatherData weatherData2;
    private Cloth testCloth;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 1
        testUser1 = User.builder()
                .name("user1")
                .email("user1@example.com")
                .password("password123")
                .provider(AuthProvider.LOCAL)
                .providerId("user1-provider")
                .role(UserRole.USER)
                .build();
        testUser1 = userRepository.save(testUser1);

        // 테스트용 사용자 2
        testUser2 = User.builder()
                .name("user2")
                .email("user2@example.com")
                .password("password123")
                .provider(AuthProvider.LOCAL)
                .providerId("user2-provider")
                .role(UserRole.USER)
                .build();
        testUser2 = userRepository.save(testUser2);

        // 테스트용 날씨 지역 1 먼저 저장 (weatherData 없이)
        WeatherRegion tempRegion1 = WeatherRegion.builder()
                .x(60)
                .y(127)
                .latitude(37.5665)
                .longitude(126.9780)
                .locationNames("서울특별시,강남구,역삼동")
                .build();
        tempRegion1 = weatherRegionRepository.save(tempRegion1);
        entityManager.flush();

        // 테스트용 날씨 데이터 1 저장 (weatherRegion 참조)
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

        // WeatherRegion에 weatherData 설정 (native SQL 사용)
        entityManager.createNativeQuery(
                "UPDATE weather_regions SET weather_data_id = :weatherDataId WHERE id = :regionId")
                .setParameter("weatherDataId", weatherData1.getId())
                .setParameter("regionId", tempRegion1.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        testWeather1 = weatherRegionRepository.findById(tempRegion1.getId()).orElseThrow();

        // 테스트용 날씨 지역 2 먼저 저장
        WeatherRegion tempRegion2 = WeatherRegion.builder()
                .x(61)
                .y(128)
                .latitude(37.5000)
                .longitude(127.0000)
                .locationNames("서울특별시,송파구,잠실동")
                .build();
        tempRegion2 = weatherRegionRepository.save(tempRegion2);
        entityManager.flush();

        // 테스트용 날씨 데이터 2 저장
        weatherData2 = WeatherData.builder()
                .weatherRegion(tempRegion2)
                .forecastKind(ForecastKind.ULTRA_NOW)
                .forecastAt(Instant.now())
                .forecastedAt(Instant.now())
                .skyStatus(SkyStatus.CLOUDY)
                .temperatureCurrent(15.0)
                .temperatureMin(10.0)
                .temperatureMax(20.0)
                .precipitationType(PrecipitationType.RAIN)
                .precipitationAmount(5.0)
                .precipitationProb(80.0)
                .humidityCurrent(80.0)
                .windSpeed(5.0)
                .windAsWord(WindStrength.MODERATE)
                .build();
        weatherData2 = weatherDataRepository.save(weatherData2);
        entityManager.flush();

        // WeatherRegion에 weatherData 설정 (native SQL 사용)
        entityManager.createNativeQuery(
                "UPDATE weather_regions SET weather_data_id = :weatherDataId WHERE id = :regionId")
                .setParameter("weatherDataId", weatherData2.getId())
                .setParameter("regionId", tempRegion2.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        testWeather2 = weatherRegionRepository.findById(tempRegion2.getId()).orElseThrow();

        // 테스트용 의상
        testCloth = Cloth.builder()
                .owner(testUser1)
                .name("테스트 셔츠")
                .type(ClothType.TOP)
                .build();
        testCloth = clothRepository.save(testCloth);

        entityManager.flush();
    }

    @Test
    @DisplayName("principal이 null일 때 예외 발생")
    void findFeedsByCursor_NullPrincipal() {
        // when & then
        assertThatThrownBy(() -> feedQueryRepository.findFeedsByCursor(
                null, null, 10, null, null, null, null, null, null, null
        ))
                .isInstanceOf(org.springframework.dao.InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("인증 정보가 없습니다");
    }

    @Test
    @DisplayName("기본 피드 조회 성공")
    void findFeedsByCursor_Basic() {
        // given
        Feed feed1 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("첫 번째 피드")
                .build();
        feedRepository.save(feed1);

        Feed feed2 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("두 번째 피드")
                .build();
        feedRepository.save(feed2);
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, null, null, null, null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.data()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result.sortBy()).isEqualTo("createdAt");
        assertThat(result.sortDirection()).isEqualTo("DESCENDING");
    }

    @Test
    @DisplayName("keyword 필터링 테스트")
    void findFeedsByCursor_WithKeyword() {
        // given
        Feed feed1 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("오늘 날씨가 좋아요")
                .build();
        feedRepository.save(feed1);

        Feed feed2 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("비가 와요")
                .build();
        feedRepository.save(feed2);
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, null, null, "날씨", null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.data()).isNotEmpty();
        assertThat(result.data()).allMatch(dto -> dto.content().contains("날씨"));
    }

    @Test
    @DisplayName("author 필터링 테스트")
    void findFeedsByCursor_WithAuthor() {
        // given
        Feed feed1 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("사용자1의 피드")
                .build();
        feedRepository.save(feed1);

        Feed feed2 = Feed.builder()
                .user(testUser2)
                .weather(weatherData1)
                .content("사용자2의 피드")
                .build();
        feedRepository.save(feed2);
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, null, null, null, null, null, testUser1.getId(), testUser1.getId()
        );

        // then
        assertThat(result.data()).isNotEmpty();
        assertThat(result.data()).allMatch(dto -> dto.author().userId().equals(testUser1.getId()));
    }


    @Test
    @DisplayName("limit 테스트")
    void findFeedsByCursor_WithLimit() {
        // given
        for (int i = 0; i < 5; i++) {
            Feed feed = Feed.builder()
                    .user(testUser1)
                    .weather(weatherData1)
                    .content("피드 " + i)
                    .build();
            feedRepository.save(feed);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 3, null, null, null, null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.data()).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("hasNext 테스트")
    void findFeedsByCursor_HasNext() {
        // given
        for (int i = 0; i < 5; i++) {
            Feed feed = Feed.builder()
                    .user(testUser1)
                    .weather(weatherData1)
                    .content("피드 " + i)
                    .build();
            feedRepository.save(feed);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 3, null, null, null, null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isNotNull();
        assertThat(result.nextAfter()).isNotNull();
    }

    @Test
    @DisplayName("cursor 페이징 테스트")
    void findFeedsByCursor_WithCursor() {
        // given
        for (int i = 0; i < 5; i++) {
            Feed feed = Feed.builder()
                    .user(testUser1)
                    .weather(weatherData1)
                    .content("피드 " + i)
                    .build();
            feedRepository.save(feed);
        }
        entityManager.flush();
        entityManager.clear();

        // when - 첫 페이지
        FeedDTOCursorResponse firstPage = feedQueryRepository.findFeedsByCursor(
                null, null, 2, null, null, null, null, null, null, testUser1.getId()
        );

        // when - 두 번째 페이지
        FeedDTOCursorResponse secondPage = feedQueryRepository.findFeedsByCursor(
                firstPage.nextCursor(), firstPage.nextAfter(), 2, null, null, null, null, null, null, testUser1.getId()
        );

        // then
        assertThat(firstPage.data()).hasSize(2);
        assertThat(secondPage.data()).isNotEmpty();
        assertThat(firstPage.data().get(0).id()).isNotEqualTo(secondPage.data().get(0).id());
    }

    @Test
    @DisplayName("정렬 테스트 - createdAt ASCENDING")
    void findFeedsByCursor_SortByCreatedAtAsc() {
        // given
        Feed feed1 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("첫 번째")
                .build();
        feedRepository.save(feed1);
        entityManager.flush();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Feed feed2 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("두 번째")
                .build();
        feedRepository.save(feed2);
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, "createdAt", "ASCENDING", null, null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.data()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result.sortBy()).isEqualTo("createdAt");
        assertThat(result.sortDirection()).isEqualTo("ASCENDING");
    }

    @Test
    @DisplayName("정렬 테스트 - likeCount DESCENDING")
    void findFeedsByCursor_SortByLikeCount() {
        // given
        Feed feed1 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("좋아요 많은 피드")
                .build();
        feedRepository.save(feed1);
        feed1.increaseLikeCount();
        feed1.increaseLikeCount();
        feed1.increaseLikeCount();

        Feed feed2 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("좋아요 적은 피드")
                .build();
        feedRepository.save(feed2);
        feed2.increaseLikeCount();
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, "likeCount", "DESCENDING", null, null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.data()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result.sortBy()).isEqualTo("likeCount");
        assertThat(result.sortDirection()).isEqualTo("DESCENDING");
    }

    @Test
    @DisplayName("좋아요 여부 확인 테스트")
    void findFeedsByCursor_WithLikedByMe() {
        // given
        Feed feed1 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("좋아요 누른 피드")
                .build();
        feedRepository.save(feed1);

        Feed feed2 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("좋아요 안 누른 피드")
                .build();
        feedRepository.save(feed2);

        Like like = Like.builder()
                .user(testUser1)
                .feed(feed1)
                .build();
        likeRepository.save(like);
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, null, null, null, null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.data()).isNotEmpty();
        assertThat(result.data().stream().anyMatch(dto -> dto.likedByMe())).isTrue();
        assertThat(result.data().stream().anyMatch(dto -> !dto.likedByMe())).isTrue();
    }

    @Test
    @DisplayName("OOTD 포함된 피드 조회")
    void findFeedsByCursor_WithOotd() {
        // given
        Feed feed = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("OOTD 피드")
                .build();
        feed.addOotd(testCloth);
        feedRepository.save(feed);
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, null, null, null, null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.data()).isNotEmpty();
        assertThat(result.data().stream()
                .anyMatch(dto -> !dto.ootds().isEmpty())).isTrue();
    }

    @Test
    @DisplayName("빈 결과 조회")
    void findFeedsByCursor_EmptyResult() {
        // given - 아무 피드도 없는 상태에서

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, null, null, "존재하지않는키워드", null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.data()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.nextAfter()).isNull();
    }

    @Test
    @DisplayName("totalCount 확인")
    void findFeedsByCursor_TotalCount() {
        // given
        for (int i = 0; i < 3; i++) {
            Feed feed = Feed.builder()
                    .user(testUser1)
                    .weather(weatherData1)
                    .content("피드 " + i)
                    .build();
            feedRepository.save(feed);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, null, null, null, null, null, null, testUser1.getId()
        );

        // then
        assertThat(result.totalCount()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("여러 필터 조합 테스트")
    void findFeedsByCursor_MultipleFilters() {
        // given
        Feed feed1 = Feed.builder()
                .user(testUser1)
                .weather(weatherData1)
                .content("좋은 날씨 피드")
                .build();
        feedRepository.save(feed1);

        Feed feed2 = Feed.builder()
                .user(testUser2)
                .weather(weatherData1)
                .content("좋은 날씨 피드")
                .build();
        feedRepository.save(feed2);

        Feed feed3 = Feed.builder()
                .user(testUser1)
                .weather(weatherData2)
                .content("다른 피드")
                .build();
        feedRepository.save(feed3);
        entityManager.flush();
        entityManager.clear();

        // when
        FeedDTOCursorResponse result = feedQueryRepository.findFeedsByCursor(
                null, null, 10, null, null, "좋은", null, null, testUser1.getId(), testUser1.getId()
        );

        // then
        assertThat(result.data()).isNotEmpty();
        assertThat(result.data()).allMatch(dto ->
                dto.content().contains("좋은") &&
                dto.author().userId().equals(testUser1.getId())
        );
    }
}
