package com.codeit.closet.module.recommendation.service;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.cloth.mapper.ClothMapper;
import com.codeit.closet.module.cloth.repository.ClothAttributeQueryRepository;
import com.codeit.closet.module.cloth.repository.ClothRepository;
import com.codeit.closet.module.recommendation.algorithm.OutfitCombinationGenerator;
import com.codeit.closet.module.recommendation.algorithm.RecommendationScorer;
import com.codeit.closet.module.recommendation.algorithm.SeasonFilter;
import com.codeit.closet.module.recommendation.algorithm.TemperatureClothMatcher;
import com.codeit.closet.module.recommendation.dto.RecommendationClothDTO;
import com.codeit.closet.module.recommendation.mapper.RecommendationMapper;
import com.codeit.closet.module.recommendation.dto.RecommendationResponse;
import com.codeit.closet.module.recommendation.exception.InsufficientClothesException;
import com.codeit.closet.module.recommendation.exception.UserWeatherNotSetException;
import com.codeit.closet.module.recommendation.exception.WeatherNotFoundException;
import com.codeit.closet.module.recommendation.service.impl.BasicRecommendationService;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import com.codeit.closet.module.weather.entity.WindStrength;
import com.codeit.closet.module.weather.repository.WeatherDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicRecommendationService 테스트")
class BasicRecommendationServiceTest {

    @Mock
    private WeatherDataRepository weatherDataRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClothRepository clothRepository;
    @Mock
    private TemperatureClothMatcher temperatureClothMatcher;
    @Mock
    private OutfitCombinationGenerator combinationGenerator;
    @Mock
    private RecommendationScorer recommendationScorer;
    @Mock
    private ClothAttributeQueryRepository clothAttributeQueryRepository;
    @Mock
    private RecommendationMapper recommendationMapper;
    @Mock
    private ClothMapper clothMapper;
    @Mock
    private SeasonFilter seasonFilter;

    @InjectMocks
    private BasicRecommendationService recommendationService;

    private UUID userId;
    private UUID weatherId;
    private User user;
    private WeatherData weatherData;
    private WeatherRegion weatherRegion;
    private Cloth topCloth;
    private Cloth bottomCloth;
    private Cloth outerCloth;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        weatherId = UUID.randomUUID();
        Instant now = Instant.now();

        // WeatherRegion 설정
        weatherRegion = WeatherRegion.builder()
                .id(UUID.randomUUID())
                .x(60)
                .y(127)
                .latitude(37.5665)
                .longitude(126.9780)
                .locationNames("서울특별시,종로구,청운동")
                .build();

        // User 설정
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("테스트유저")
                .temperatureSensitivity(3)
                .weather(weatherRegion)
                .build();

        // WeatherData 설정
        weatherData = WeatherData.builder()
                .id(weatherId)
                .weatherRegion(weatherRegion)
                .forecastAt(now)
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(15.0)
                .temperatureMin(10.0)
                .temperatureMax(20.0)
                .precipitationType(PrecipitationType.NONE)
                .precipitationAmount(0.0)
                .precipitationProb(0.0)
                .humidityCurrent(60.0)
                .humidityComparedToDayBefore(0.0)
                .windSpeed(2.5)
                .windAsWord(WindStrength.WEAK)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 테스트용 의상
        topCloth = Cloth.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .name("흰색 셔츠")
                .type(ClothType.TOP)
                .createdAt(now)
                .build();

        bottomCloth = Cloth.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .name("청바지")
                .type(ClothType.BOTTOM)
                .createdAt(now)
                .build();

        outerCloth = Cloth.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .name("패딩")
                .type(ClothType.OUTER)
                .createdAt(now)
                .build();
    }

    @Nested
    @DisplayName("getRecommendations - 추천 조회")
    class GetRecommendationsTest {

        @Test
        @DisplayName("weatherId 제공 시 해당 날씨 데이터로 추천")
        void shouldReturnRecommendationsWithWeatherId() {
            // Given
            List<Cloth> clothes = List.of(topCloth, bottomCloth, outerCloth);
            List<List<Cloth>> combinations = List.of(List.of(topCloth, bottomCloth));

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findById(weatherId)).willReturn(Optional.of(weatherData));
            given(clothRepository.findAllByOwner_Id(userId)).willReturn(clothes);
            given(clothAttributeQueryRepository.findAttributeMapsByClothIds(anyList())).willReturn(new HashMap<>());
            given(clothAttributeQueryRepository.findAttributeValuesByClothIds(anyList())).willReturn(new HashMap<>());
            given(recommendationScorer.getAdjustedTemperature(anyDouble(), anyInt())).willReturn(15.0);
            given(seasonFilter.filterByAllowedSeasons(anyList(), anyDouble(), anyInt(), any()))
                    .willReturn(clothes);
            given(combinationGenerator.canGenerateCombinations(clothes)).willReturn(true);
            given(temperatureClothMatcher.isOuterRequired(15.0)).willReturn(false);
            given(combinationGenerator.generateCombinations(eq(clothes), eq(false), anyInt()))
                    .willReturn(combinations);
            given(recommendationScorer.calculateOutfitScore(anyList(), any(), anyInt(), any()))
                    .willReturn(75);
            given(recommendationMapper.toRecommendationClothDTO(any(Cloth.class), anyList()))
                    .willReturn(new RecommendationClothDTO(UUID.randomUUID(), "테스트", null, "TOP", List.of()));

            // When
            RecommendationResponse response = recommendationService.getRecommendations(weatherId, userId, 5);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.weatherId()).isEqualTo(weatherId);
            assertThat(response.clothes()).isNotEmpty();
        }

        @Test
        @DisplayName("weatherId 미제공 시 사용자 설정 지역의 최신 날씨 사용")
        void shouldReturnRecommendationsWithUserWeatherRegion() {
            // Given
            List<Cloth> clothes = List.of(topCloth, bottomCloth);
            List<List<Cloth>> combinations = List.of(List.of(topCloth, bottomCloth));

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findLatestByWeatherRegionId(weatherRegion.getId()))
                    .willReturn(Optional.of(weatherData));
            given(clothRepository.findAllByOwner_Id(userId)).willReturn(clothes);
            given(clothAttributeQueryRepository.findAttributeMapsByClothIds(anyList())).willReturn(new HashMap<>());
            given(clothAttributeQueryRepository.findAttributeValuesByClothIds(anyList())).willReturn(new HashMap<>());
            given(recommendationScorer.getAdjustedTemperature(anyDouble(), anyInt())).willReturn(15.0);
            given(seasonFilter.filterByAllowedSeasons(anyList(), anyDouble(), anyInt(), any()))
                    .willReturn(clothes);
            given(combinationGenerator.canGenerateCombinations(clothes)).willReturn(true);
            given(temperatureClothMatcher.isOuterRequired(15.0)).willReturn(false);
            given(combinationGenerator.generateCombinations(eq(clothes), eq(false), anyInt()))
                    .willReturn(combinations);
            given(recommendationScorer.calculateOutfitScore(anyList(), any(), anyInt(), any()))
                    .willReturn(70);
            given(recommendationMapper.toRecommendationClothDTO(any(Cloth.class), anyList()))
                    .willReturn(new RecommendationClothDTO(UUID.randomUUID(), "테스트", null, "TOP", List.of()));

            // When
            RecommendationResponse response = recommendationService.getRecommendations(null, userId, 5);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.clothes()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("존재하지 않는 사용자 요청 시 예외")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> recommendationService.getRecommendations(weatherId, userId, 5))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("존재하지 않는 회원");
        }

        @Test
        @DisplayName("존재하지 않는 weatherId 요청 시 예외")
        void shouldThrowExceptionWhenWeatherNotFound() {
            // Given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findById(weatherId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> recommendationService.getRecommendations(weatherId, userId, 5))
                    .isInstanceOf(WeatherNotFoundException.class);
        }

        @Test
        @DisplayName("사용자 날씨 지역 미설정 시 예외")
        void shouldThrowExceptionWhenUserWeatherNotSet() {
            // Given
            User userWithoutWeather = User.builder()
                    .id(userId)
                    .email("test@example.com")
                    .name("테스트유저")
                    .temperatureSensitivity(3)
                    .weather(null)  // 날씨 지역 미설정
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(userWithoutWeather));

            // When & Then
            assertThatThrownBy(() -> recommendationService.getRecommendations(null, userId, 5))
                    .isInstanceOf(UserWeatherNotSetException.class)
                    .hasMessageContaining("날씨 지역이 설정되지 않았습니다");
        }

        @Test
        @DisplayName("옷장이 비어있을 때 예외")
        void shouldThrowExceptionWhenClosetEmpty() {
            // Given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findById(weatherId)).willReturn(Optional.of(weatherData));
            given(clothRepository.findAllByOwner_Id(userId)).willReturn(List.of());

            // When & Then
            assertThatThrownBy(() -> recommendationService.getRecommendations(weatherId, userId, 5))
                    .isInstanceOf(InsufficientClothesException.class)
                    .hasMessageContaining("옷장에 등록된 의상이 없습니다");
        }

        @Test
        @DisplayName("코디 조합 생성 불가 시 예외")
        void shouldThrowExceptionWhenCannotGenerateCombinations() {
            // Given
            List<Cloth> clothes = List.of(topCloth);  // TOP만 있음

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findById(weatherId)).willReturn(Optional.of(weatherData));
            given(clothRepository.findAllByOwner_Id(userId)).willReturn(clothes);
            given(clothAttributeQueryRepository.findAttributeMapsByClothIds(anyList())).willReturn(new HashMap<>());
            given(clothAttributeQueryRepository.findAttributeValuesByClothIds(anyList())).willReturn(new HashMap<>());
            given(recommendationScorer.getAdjustedTemperature(anyDouble(), anyInt())).willReturn(15.0);
            given(seasonFilter.filterByAllowedSeasons(anyList(), anyDouble(), anyInt(), any()))
                    .willReturn(clothes);
            given(combinationGenerator.canGenerateCombinations(clothes)).willReturn(false);
            given(seasonFilter.filterWithExpandedSeasons(anyList(), anyDouble(), any()))
                    .willReturn(clothes);

            // When & Then
            assertThatThrownBy(() -> recommendationService.getRecommendations(weatherId, userId, 5))
                    .isInstanceOf(InsufficientClothesException.class)
                    .hasMessageContaining("추천 가능한 코디 조합을 만들 수 없습니다");
        }

        @Test
        @DisplayName("limit이 0 이하일 때 예외")
        void shouldThrowExceptionWhenLimitIsZeroOrNegative() {
            // When & Then
            assertThatThrownBy(() -> recommendationService.getRecommendations(weatherId, userId, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("추천 개수(limit)는 1 이상이어야 합니다");

            assertThatThrownBy(() -> recommendationService.getRecommendations(weatherId, userId, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("추천 개수(limit)는 1 이상이어야 합니다");
        }

        @Test
        @DisplayName("사용자 지역의 최신 날씨가 없을 때 예외")
        void shouldThrowExceptionWhenLatestWeatherNotFound() {
            // Given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findLatestByWeatherRegionId(weatherRegion.getId()))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> recommendationService.getRecommendations(null, userId, 5))
                    .isInstanceOf(WeatherNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("아우터 필수 케이스")
    class OuterRequiredTest {

        @Test
        @DisplayName("아우터 필수 온도에서 아우터 포함 조합 생성")
        void shouldIncludeOuterWhenRequired() {
            // Given
            List<Cloth> clothes = List.of(topCloth, bottomCloth, outerCloth);
            List<List<Cloth>> combinationsWithOuter = List.of(List.of(topCloth, bottomCloth, outerCloth));

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findById(weatherId)).willReturn(Optional.of(weatherData));
            given(clothRepository.findAllByOwner_Id(userId)).willReturn(clothes);
            given(clothAttributeQueryRepository.findAttributeMapsByClothIds(anyList())).willReturn(new HashMap<>());
            given(clothAttributeQueryRepository.findAttributeValuesByClothIds(anyList())).willReturn(new HashMap<>());
            given(recommendationScorer.getAdjustedTemperature(anyDouble(), anyInt())).willReturn(5.0); // 추운 온도
            given(seasonFilter.filterByAllowedSeasons(anyList(), anyDouble(), anyInt(), any()))
                    .willReturn(clothes);
            given(combinationGenerator.canGenerateCombinations(clothes)).willReturn(true);
            given(temperatureClothMatcher.isOuterRequired(5.0)).willReturn(true); // 아우터 필수
            given(combinationGenerator.generateCombinations(eq(clothes), eq(true), anyInt()))
                    .willReturn(combinationsWithOuter);
            given(recommendationScorer.calculateOutfitScore(anyList(), any(), anyInt(), any()))
                    .willReturn(80);
            given(recommendationMapper.toRecommendationClothDTO(any(Cloth.class), anyList()))
                    .willReturn(new RecommendationClothDTO(UUID.randomUUID(), "테스트", null, "OUTER", List.of()));

            // When
            RecommendationResponse response = recommendationService.getRecommendations(weatherId, userId, 5);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.outfits()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("확장 필터링 케이스")
    class ExpandedFilteringTest {

        @Test
        @DisplayName("첫 번째 필터링 후 조합 불가 시 확장 필터링으로 재시도")
        void shouldRetryWithExpandedFilteringWhenInitialFilterFails() {
            // Given
            List<Cloth> clothes = List.of(topCloth, bottomCloth);
            List<Cloth> filteredClothes = List.of(); // 초기 필터링 결과 빈 목록
            List<Cloth> expandedClothes = List.of(topCloth, bottomCloth); // 확장 필터링 결과
            List<List<Cloth>> combinations = List.of(List.of(topCloth, bottomCloth));

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findById(weatherId)).willReturn(Optional.of(weatherData));
            given(clothRepository.findAllByOwner_Id(userId)).willReturn(clothes);
            given(clothAttributeQueryRepository.findAttributeMapsByClothIds(anyList())).willReturn(new HashMap<>());
            given(clothAttributeQueryRepository.findAttributeValuesByClothIds(anyList())).willReturn(new HashMap<>());
            given(recommendationScorer.getAdjustedTemperature(anyDouble(), anyInt())).willReturn(15.0);
            given(seasonFilter.filterByAllowedSeasons(anyList(), anyDouble(), anyInt(), any()))
                    .willReturn(filteredClothes);
            // 빈 목록은 조합 생성 불가 -> 확장 필터링 시도
            given(combinationGenerator.canGenerateCombinations(filteredClothes)).willReturn(false);
            given(seasonFilter.filterWithExpandedSeasons(anyList(), anyDouble(), any()))
                    .willReturn(expandedClothes);
            given(combinationGenerator.canGenerateCombinations(expandedClothes)).willReturn(true);
            given(temperatureClothMatcher.isOuterRequired(15.0)).willReturn(false);
            given(combinationGenerator.generateCombinations(eq(expandedClothes), eq(false), anyInt()))
                    .willReturn(combinations);
            given(recommendationScorer.calculateOutfitScore(anyList(), any(), anyInt(), any()))
                    .willReturn(70);
            given(recommendationMapper.toRecommendationClothDTO(any(Cloth.class), anyList()))
                    .willReturn(new RecommendationClothDTO(UUID.randomUUID(), "테스트", null, "TOP", List.of()));

            // When
            RecommendationResponse response = recommendationService.getRecommendations(weatherId, userId, 5);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.outfits()).isNotEmpty();
        }

        @Test
        @DisplayName("조합 수가 limit보다 적을 때 확장 필터링으로 추가 생성")
        void shouldExpandFilteringWhenCombinationsLessThanLimit() {
            // Given
            List<Cloth> clothes = List.of(topCloth, bottomCloth, outerCloth);
            List<List<Cloth>> initialCombinations = List.of(List.of(topCloth, bottomCloth)); // 1개 조합

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findById(weatherId)).willReturn(Optional.of(weatherData));
            given(clothRepository.findAllByOwner_Id(userId)).willReturn(clothes);
            given(clothAttributeQueryRepository.findAttributeMapsByClothIds(anyList())).willReturn(new HashMap<>());
            given(clothAttributeQueryRepository.findAttributeValuesByClothIds(anyList())).willReturn(new HashMap<>());
            given(recommendationScorer.getAdjustedTemperature(anyDouble(), anyInt())).willReturn(15.0);
            given(seasonFilter.filterByAllowedSeasons(anyList(), anyDouble(), anyInt(), any()))
                    .willReturn(clothes);
            given(combinationGenerator.canGenerateCombinations(clothes)).willReturn(true);
            given(temperatureClothMatcher.isOuterRequired(15.0)).willReturn(false);
            // 첫 번째 생성: 1개 조합 (limit 5보다 적음)
            given(combinationGenerator.generateCombinations(eq(clothes), eq(false), anyInt()))
                    .willReturn(initialCombinations);
            // 확장 필터링 후 재생성
            given(seasonFilter.filterWithExpandedSeasons(anyList(), anyDouble(), any()))
                    .willReturn(clothes);
            given(recommendationScorer.calculateOutfitScore(anyList(), any(), anyInt(), any()))
                    .willReturn(70);
            given(recommendationMapper.toRecommendationClothDTO(any(Cloth.class), anyList()))
                    .willReturn(new RecommendationClothDTO(UUID.randomUUID(), "테스트", null, "TOP", List.of()));

            // When
            RecommendationResponse response = recommendationService.getRecommendations(weatherId, userId, 5);

            // Then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("getRecommendationsWithFeedReference")
    class GetRecommendationsWithFeedReferenceTest {

        @Test
        @DisplayName("피드 기반 추천 조회 (현재는 기본 추천과 동일)")
        void shouldReturnRecommendationsWithFeedReference() {
            // Given
            List<Cloth> clothes = List.of(topCloth, bottomCloth);
            List<List<Cloth>> combinations = List.of(List.of(topCloth, bottomCloth));

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(weatherDataRepository.findById(weatherId)).willReturn(Optional.of(weatherData));
            given(clothRepository.findAllByOwner_Id(userId)).willReturn(clothes);
            given(clothAttributeQueryRepository.findAttributeMapsByClothIds(anyList())).willReturn(new HashMap<>());
            given(clothAttributeQueryRepository.findAttributeValuesByClothIds(anyList())).willReturn(new HashMap<>());
            given(recommendationScorer.getAdjustedTemperature(anyDouble(), anyInt())).willReturn(15.0);
            given(seasonFilter.filterByAllowedSeasons(anyList(), anyDouble(), anyInt(), any()))
                    .willReturn(clothes);
            given(combinationGenerator.canGenerateCombinations(clothes)).willReturn(true);
            given(temperatureClothMatcher.isOuterRequired(15.0)).willReturn(false);
            given(combinationGenerator.generateCombinations(eq(clothes), eq(false), anyInt()))
                    .willReturn(combinations);
            given(recommendationScorer.calculateOutfitScore(anyList(), any(), anyInt(), any()))
                    .willReturn(75);
            given(recommendationMapper.toRecommendationClothDTO(any(Cloth.class), anyList()))
                    .willReturn(new RecommendationClothDTO(UUID.randomUUID(), "테스트", null, "TOP", List.of()));

            // When
            RecommendationResponse response = recommendationService.getRecommendationsWithFeedReference(
                    weatherId, userId, true, 5);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.weatherId()).isEqualTo(weatherId);
        }
    }
}
