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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AttributeScoreCalculator 테스트")
class AttributeScoreCalculatorTest {

    private AttributeScoreCalculator calculator;
    private Cloth testCloth;
    private WeatherData testWeather;
    private UUID clothId;

    @BeforeEach
    void setUp() {
        calculator = new AttributeScoreCalculator();
        Instant now = Instant.now();
        clothId = UUID.randomUUID();

        testCloth = Cloth.builder()
                .id(clothId)
                .name("테스트 의상")
                .type(ClothType.TOP)
                .createdAt(now)
                .build();

        testWeather = WeatherData.builder()
                .id(UUID.randomUUID())
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
    }

    @Nested
    @DisplayName("calculateAttributeScore - 속성 점수 계산")
    class CalculateAttributeScoreTest {

        @Test
        @DisplayName("null attributeMaps 처리 시 기본 점수 반환")
        void shouldReturnDefaultScoreWhenAttributeMapsIsNull() {
            // Given
            double adjustedTemp = 15.0;

            // When
            int score = calculator.calculateAttributeScore(testCloth, adjustedTemp, testWeather, null);

            // Then
            assertThat(score).isEqualTo(15); // DEFAULT_ATTRIBUTE_SCORE
        }

        @Test
        @DisplayName("빈 attributes 처리 시 기본 점수 반환")
        void shouldReturnDefaultScoreWhenAttributesEmpty() {
            // Given
            double adjustedTemp = 15.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();

            // When
            int score = calculator.calculateAttributeScore(testCloth, adjustedTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(15); // DEFAULT_ATTRIBUTE_SCORE
        }

        @Test
        @DisplayName("clothId에 대한 속성이 없을 때 기본 점수 반환")
        void shouldReturnDefaultScoreWhenClothIdNotInMap() {
            // Given
            double adjustedTemp = 15.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(UUID.randomUUID(), Map.of("SEASON", "WINTER")); // 다른 clothId

            // When
            int score = calculator.calculateAttributeScore(testCloth, adjustedTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(15); // DEFAULT_ATTRIBUTE_SCORE
        }

        @Test
        @DisplayName("점수는 0-30 범위로 클램핑")
        void shouldClampScoreBetweenZeroAndThirty() {
            // Given
            double adjustedTemp = 15.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "SUMMER"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, adjustedTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isBetween(0, 30);
        }
    }

    @Nested
    @DisplayName("계절 점수 계산 - 온도별 계절 매핑")
    class SeasonScoreByTemperatureTest {

        @Test
        @DisplayName("겨울 온도(0도)에서 WINTER 의상 최고 점수")
        void shouldReturnMaxScoreForWinterClothInWinterTemp() {
            // Given: 0도 = WINTER
            double winterTemp = 0.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "WINTER"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, winterTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // SEASON_EXACT_MATCH_SCORE
        }

        @Test
        @DisplayName("겨울 온도(9도)에서 WINTER 의상 최고 점수")
        void shouldReturnMaxScoreForWinterClothAtNineDegrees() {
            // Given: 9도 = WINTER 경계
            double borderTemp = 9.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "WINTER"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, borderTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // SEASON_EXACT_MATCH_SCORE
        }

        @Test
        @DisplayName("가을 온도(15도)에서 FALL 의상 최고 점수")
        void shouldReturnMaxScoreForFallClothInFallTemp() {
            // Given: 10-16도 = FALL
            double fallTemp = 15.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "FALL"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, fallTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // SEASON_EXACT_MATCH_SCORE
        }

        @Test
        @DisplayName("봄 온도(20도)에서 SPRING 의상 최고 점수")
        void shouldReturnMaxScoreForSpringClothInSpringTemp() {
            // Given: 17-22도 = SPRING
            double springTemp = 20.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "SPRING"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, springTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // SEASON_EXACT_MATCH_SCORE
        }

        @Test
        @DisplayName("여름 온도(30도)에서 SUMMER 의상 최고 점수")
        void shouldReturnMaxScoreForSummerClothInSummerTemp() {
            // Given: 23도 이상 = SUMMER
            double summerTemp = 30.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "SUMMER"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, summerTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // SEASON_EXACT_MATCH_SCORE
        }
    }

    @Nested
    @DisplayName("계절 점수 계산 - 인접 계절")
    class AdjacentSeasonScoreTest {

        @Test
        @DisplayName("봄 온도에서 FALL 의상 인접 점수")
        void shouldReturnAdjacentScoreForFallClothInSpring() {
            // Given: SPRING ↔ FALL 은 서로 인접 계절
            double springTemp = 20.0; // SPRING 온도
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "FALL"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, springTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(15); // SEASON_ADJACENT_SCORE
        }

        @Test
        @DisplayName("가을 온도에서 SPRING 의상 인접 점수")
        void shouldReturnAdjacentScoreForSpringClothInFall() {
            // Given: FALL ↔ SPRING 은 서로 인접 계절
            double fallTemp = 15.0; // FALL 온도
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "SPRING"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, fallTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(15); // SEASON_ADJACENT_SCORE
        }
    }

    @Nested
    @DisplayName("계절 점수 계산 - 계절 불일치")
    class SeasonMismatchScoreTest {

        @Test
        @DisplayName("겨울 온도에서 SUMMER 의상 불일치 점수")
        void shouldReturnMismatchScoreForSummerClothInWinter() {
            // Given
            double winterTemp = 0.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "SUMMER"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, winterTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(5); // SEASON_MISMATCH_SCORE
        }

        @Test
        @DisplayName("여름 온도에서 WINTER 의상 불일치 점수")
        void shouldReturnMismatchScoreForWinterClothInSummer() {
            // Given
            double summerTemp = 30.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "WINTER"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, summerTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(5); // SEASON_MISMATCH_SCORE
        }

        @Test
        @DisplayName("봄 온도에서 WINTER 의상 불일치 점수")
        void shouldReturnMismatchScoreForWinterClothInSpring() {
            // Given
            double springTemp = 20.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "WINTER"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, springTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(5); // SEASON_MISMATCH_SCORE
        }
    }

    @Nested
    @DisplayName("계절 점수 계산 - null/빈 계절 처리")
    class NullEmptySeasonScoreTest {

        @Test
        @DisplayName("SEASON 속성이 null일 때 기본 점수")
        void shouldReturnDefaultScoreWhenSeasonIsNull() {
            // Given
            double adjustedTemp = 15.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            Map<String, String> attributes = new HashMap<>();
            attributes.put("SEASON", null);
            attributeMaps.put(clothId, attributes);

            // When
            int score = calculator.calculateAttributeScore(testCloth, adjustedTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(15); // SEASON_DEFAULT_SCORE
        }

        @Test
        @DisplayName("SEASON 속성이 빈 문자열일 때 기본 점수")
        void shouldReturnDefaultScoreWhenSeasonIsEmpty() {
            // Given
            double adjustedTemp = 15.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", ""));

            // When
            int score = calculator.calculateAttributeScore(testCloth, adjustedTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(15); // SEASON_DEFAULT_SCORE
        }

        @Test
        @DisplayName("SEASON 속성이 없을 때 기본 점수")
        void shouldReturnDefaultScoreWhenSeasonAttributeNotPresent() {
            // Given
            double adjustedTemp = 15.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("COLOR", "RED")); // SEASON 없음

            // When
            int score = calculator.calculateAttributeScore(testCloth, adjustedTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(15); // SEASON_DEFAULT_SCORE
        }
    }

    @Nested
    @DisplayName("온도 경계값 테스트")
    class TemperatureBoundaryTest {

        @Test
        @DisplayName("10도 경계에서 FALL로 판단")
        void shouldDetermineFallAtTenDegrees() {
            // Given: 10도 = FALL 시작
            double borderTemp = 10.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "FALL"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, borderTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // FALL 매칭
        }

        @Test
        @DisplayName("16도 경계에서 FALL로 판단")
        void shouldDetermineFallAtSixteenDegrees() {
            // Given: 16도 = FALL 끝
            double borderTemp = 16.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "FALL"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, borderTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // FALL 매칭
        }

        @Test
        @DisplayName("17도 경계에서 SPRING으로 판단")
        void shouldDetermineSpringAtSeventeenDegrees() {
            // Given: 17도 = SPRING 시작
            double borderTemp = 17.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "SPRING"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, borderTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // SPRING 매칭
        }

        @Test
        @DisplayName("22도 경계에서 SPRING으로 판단")
        void shouldDetermineSpringAtTwentyTwoDegrees() {
            // Given: 22도 = SPRING 끝
            double borderTemp = 22.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "SPRING"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, borderTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // SPRING 매칭
        }

        @Test
        @DisplayName("23도 경계에서 SUMMER로 판단")
        void shouldDetermineSummerAtTwentyThreeDegrees() {
            // Given: 23도 = SUMMER 시작
            double borderTemp = 23.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "SUMMER"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, borderTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // SUMMER 매칭
        }
    }

    @Nested
    @DisplayName("대소문자 처리 테스트")
    class CaseInsensitiveTest {

        @Test
        @DisplayName("소문자 계절 값도 매칭")
        void shouldMatchLowercaseSeason() {
            // Given
            double winterTemp = 0.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "winter"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, winterTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // 대소문자 무관 매칭
        }

        @Test
        @DisplayName("혼합 대소문자 계절 값도 매칭")
        void shouldMatchMixedCaseSeason() {
            // Given
            double summerTemp = 30.0;
            Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
            attributeMaps.put(clothId, Map.of("SEASON", "SuMmEr"));

            // When
            int score = calculator.calculateAttributeScore(testCloth, summerTemp, testWeather, attributeMaps);

            // Then
            assertThat(score).isEqualTo(30); // 대소문자 무관 매칭
        }
    }
}
