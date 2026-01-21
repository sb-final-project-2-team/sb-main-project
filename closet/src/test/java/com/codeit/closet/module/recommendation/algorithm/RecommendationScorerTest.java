package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.entity.WindStrength;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationScorer 테스트")
class RecommendationScorerTest {

    @InjectMocks
    private RecommendationScorer scorer;

    @Spy
    @SuppressWarnings("unused") // @InjectMocks에 의해 주입됨
    private TemperatureClothMatcher temperatureClothMatcher;

    @Spy
    @SuppressWarnings("unused") // @InjectMocks에 의해 주입됨
    private PrecipitationClothMatcher precipitationClothMatcher;

    @Spy
    @SuppressWarnings("unused") // @InjectMocks에 의해 주입됨
    private AttributeScoreCalculator attributeScoreCalculator;

    private WeatherData coldWeather;
    private WeatherData hotWeather;
    private WeatherData rainyWeather;
    private Cloth outerCloth;
    private Cloth topCloth;
    private Cloth bottomCloth;
    private Cloth dressCloth;
    private Map<UUID, Map<String, String>> emptyAttributeMaps;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        // 추운 날씨 (0도)
        coldWeather = WeatherData.builder()
                .id(UUID.randomUUID())
                .forecastAt(now)
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(0.0)
                .temperatureMin(-5.0)
                .temperatureMax(5.0)
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

        // 더운 날씨 (30도)
        hotWeather = WeatherData.builder()
                .id(UUID.randomUUID())
                .forecastAt(now)
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(30.0)
                .temperatureMin(25.0)
                .temperatureMax(35.0)
                .precipitationType(PrecipitationType.NONE)
                .precipitationAmount(0.0)
                .precipitationProb(0.0)
                .humidityCurrent(70.0)
                .humidityComparedToDayBefore(0.0)
                .windSpeed(1.5)
                .windAsWord(WindStrength.WEAK)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 비오는 날씨 (15도)
        rainyWeather = WeatherData.builder()
                .id(UUID.randomUUID())
                .forecastAt(now)
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLOUDY)
                .temperatureCurrent(15.0)
                .temperatureMin(12.0)
                .temperatureMax(18.0)
                .precipitationType(PrecipitationType.RAIN)
                .precipitationAmount(10.0)
                .precipitationProb(80.0)
                .humidityCurrent(85.0)
                .humidityComparedToDayBefore(0.0)
                .windSpeed(3.0)
                .windAsWord(WindStrength.MODERATE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 테스트용 의상
        outerCloth = Cloth.builder()
                .id(UUID.randomUUID())
                .name("패딩 점퍼")
                .type(ClothType.OUTER)
                .createdAt(now)
                .build();

        topCloth = Cloth.builder()
                .id(UUID.randomUUID())
                .name("니트 스웨터")
                .type(ClothType.TOP)
                .createdAt(now)
                .build();

        bottomCloth = Cloth.builder()
                .id(UUID.randomUUID())
                .name("청바지")
                .type(ClothType.BOTTOM)
                .createdAt(now)
                .build();

        dressCloth = Cloth.builder()
                .id(UUID.randomUUID())
                .name("원피스")
                .type(ClothType.DRESS)
                .createdAt(now)
                .build();

        emptyAttributeMaps = new HashMap<>();
    }

    @Nested
    @DisplayName("calculateClothScore - 단일 의상 점수 계산")
    class CalculateClothScoreTest {

        @Test
        @DisplayName("추운 날씨에 아우터 높은 점수")
        void shouldReturnHighScoreForOuterInColdWeather() {
            // Given
            int sensitivity = 3;  // 보통

            // When
            int score = scorer.calculateClothScore(outerCloth, coldWeather, sensitivity, emptyAttributeMaps);

            // Then
            assertThat(score).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("더운 날씨에 아우터 낮은 점수")
        void shouldReturnLowScoreForOuterInHotWeather() {
            // Given
            int sensitivity = 3;

            // When
            int score = scorer.calculateClothScore(outerCloth, hotWeather, sensitivity, emptyAttributeMaps);

            // Then
            assertThat(score).isLessThanOrEqualTo(30);
        }

        @Test
        @DisplayName("더운 날씨에 원피스 높은 점수")
        void shouldReturnHighScoreForDressInHotWeather() {
            // Given
            int sensitivity = 3;

            // When
            int score = scorer.calculateClothScore(dressCloth, hotWeather, sensitivity, emptyAttributeMaps);

            // Then
            assertThat(score).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("비오는 날 아우터 보너스 점수")
        void shouldReturnBonusScoreForOuterInRainyWeather() {
            // Given
            int sensitivity = 3;

            // When
            int score = scorer.calculateClothScore(outerCloth, rainyWeather, sensitivity, emptyAttributeMaps);

            // Then
            // 비 올 때 아우터 추천이므로 보너스 있음
            assertThat(score).isGreaterThan(30);
        }

        @Test
        @DisplayName("비오는 날 원피스 페널티 점수")
        void shouldReturnPenaltyScoreForDressInRainyWeather() {
            // Given
            int sensitivity = 3;

            // When
            int scoreRainy = scorer.calculateClothScore(dressCloth, rainyWeather, sensitivity, emptyAttributeMaps);

            // Then
            // 비 올 때 원피스 비추천이므로 페널티 (점수가 낮음)
            assertThat(scoreRainy).isLessThanOrEqualTo(50);
        }

        @Test
        @DisplayName("점수는 0-100 범위로 클램핑")
        void shouldClampScoreBetweenZeroAndHundred() {
            // Given
            int sensitivity = 3;

            // When
            int score = scorer.calculateClothScore(outerCloth, coldWeather, sensitivity, emptyAttributeMaps);

            // Then
            assertThat(score).isBetween(0, 100);
        }
    }

    @Nested
    @DisplayName("calculateOutfitScore - 코디 조합 점수 계산")
    class CalculateOutfitScoreTest {

        @Test
        @DisplayName("추운 날씨에 아우터+상의+하의 조합 높은 점수")
        void shouldReturnHighScoreForWarmOutfitInCold() {
            // Given
            List<Cloth> outfit = List.of(outerCloth, topCloth, bottomCloth);
            int sensitivity = 3;

            // When
            int score = scorer.calculateOutfitScore(outfit, coldWeather, sensitivity, emptyAttributeMaps);

            // Then
            assertThat(score).isGreaterThanOrEqualTo(30);
        }

        @Test
        @DisplayName("빈 조합은 0점")
        void shouldReturnZeroForEmptyOutfit() {
            // Given
            List<Cloth> outfit = List.of();
            int sensitivity = 3;

            // When
            int score = scorer.calculateOutfitScore(outfit, coldWeather, sensitivity, emptyAttributeMaps);

            // Then
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("null 조합은 0점")
        void shouldReturnZeroForNullOutfit() {
            // Given
            int sensitivity = 3;

            // When
            int score = scorer.calculateOutfitScore(null, coldWeather, sensitivity, emptyAttributeMaps);

            // Then
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("조합 점수는 개별 의상 점수의 평균")
        void shouldReturnAverageScore() {
            // Given
            List<Cloth> outfit = List.of(topCloth, bottomCloth);
            int sensitivity = 3;

            // When
            int outfitScore = scorer.calculateOutfitScore(outfit, coldWeather, sensitivity, emptyAttributeMaps);
            int topScore = scorer.calculateClothScore(topCloth, coldWeather, sensitivity, emptyAttributeMaps);
            int bottomScore = scorer.calculateClothScore(bottomCloth, coldWeather, sensitivity, emptyAttributeMaps);
            int expectedAverage = (topScore + bottomScore) / 2;

            // Then
            assertThat(outfitScore).isEqualTo(expectedAverage);
        }
    }

    @Nested
    @DisplayName("getAdjustedTemperature - 체감온도 계산")
    class GetAdjustedTemperatureTest {

        @Test
        @DisplayName("sensitivity에 따라 체감온도 보정")
        void shouldAdjustTemperatureBasedOnSensitivity() {
            // Given
            double actualTemp = 15.0;

            // When
            double adjusted1 = scorer.getAdjustedTemperature(actualTemp, 1);  // 추위 많이 탐
            double adjusted3 = scorer.getAdjustedTemperature(actualTemp, 3);  // 보통
            double adjusted5 = scorer.getAdjustedTemperature(actualTemp, 5);  // 더위 많이 탐

            // Then
            assertThat(adjusted1).isEqualTo(17.0);  // +2
            assertThat(adjusted3).isEqualTo(15.0);  // +0
            assertThat(adjusted5).isEqualTo(13.0);  // -2
        }
    }
}
