package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.ClothType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TemperatureClothMatcher 테스트")
class TemperatureClothMatcherTest {

    private TemperatureClothMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new TemperatureClothMatcher();
    }

    @Nested
    @DisplayName("adjustTemperature - 체감온도 보정")
    class AdjustTemperatureTest {

        @Test
        @DisplayName("sensitivity 1 (추위 많이 탐)이면 +2도 보정")
        void shouldAddTwoDegreesForSensitivityOne() {
            // Given
            double actualTemp = 15.0;
            int sensitivity = 1;

            // When
            double adjusted = matcher.adjustTemperature(actualTemp, sensitivity);

            // Then
            assertThat(adjusted).isEqualTo(17.0);
        }

        @Test
        @DisplayName("sensitivity 3 (보통)이면 보정 없음")
        void shouldNotAdjustForSensitivityThree() {
            // Given
            double actualTemp = 15.0;
            int sensitivity = 3;

            // When
            double adjusted = matcher.adjustTemperature(actualTemp, sensitivity);

            // Then
            assertThat(adjusted).isEqualTo(15.0);
        }

        @Test
        @DisplayName("sensitivity 5 (더위 많이 탐)이면 -2도 보정")
        void shouldSubtractTwoDegreesForSensitivityFive() {
            // Given
            double actualTemp = 15.0;
            int sensitivity = 5;

            // When
            double adjusted = matcher.adjustTemperature(actualTemp, sensitivity);

            // Then
            assertThat(adjusted).isEqualTo(13.0);
        }

        @Test
        @DisplayName("sensitivity 범위 초과 시 최대값(5)으로 제한")
        void shouldClampSensitivityToMax() {
            // Given
            double actualTemp = 20.0;
            int sensitivity = 10;  // 범위 초과

            // When
            double adjusted = matcher.adjustTemperature(actualTemp, sensitivity);

            // Then
            // sensitivity 5로 제한되어 -2도 보정
            assertThat(adjusted).isEqualTo(18.0);
        }

        @Test
        @DisplayName("sensitivity 범위 미달 시 최소값(1)으로 제한")
        void shouldClampSensitivityToMin() {
            // Given
            double actualTemp = 20.0;
            int sensitivity = 0;  // 범위 미달

            // When
            double adjusted = matcher.adjustTemperature(actualTemp, sensitivity);

            // Then
            // sensitivity 1로 제한되어 +2도 보정
            assertThat(adjusted).isEqualTo(22.0);
        }
    }

    @Nested
    @DisplayName("calculateTemperatureScore - 온도 적합도 점수")
    class CalculateTemperatureScoreTest {

        @Test
        @DisplayName("한파(0도)에서 아우터 최고 점수")
        void shouldReturnHighScoreForOuterInFreezing() {
            // Given
            ClothType outer = ClothType.OUTER;
            double temperature = 0;

            // When
            int score = matcher.calculateTemperatureScore(outer, temperature);

            // Then
            assertThat(score).isEqualTo(40);  // 최고 점수
        }

        @Test
        @DisplayName("더운 날(30도)에서 아우터 낮은 점수")
        void shouldReturnLowScoreForOuterInHot() {
            // Given
            ClothType outer = ClothType.OUTER;
            double temperature = 30;

            // When
            int score = matcher.calculateTemperatureScore(outer, temperature);

            // Then
            assertThat(score).isEqualTo(5);  // 낮은 점수
        }

        @Test
        @DisplayName("더운 날(30도)에서 원피스 높은 점수")
        void shouldReturnHighScoreForDressInHot() {
            // Given
            ClothType dress = ClothType.DRESS;
            double temperature = 30;

            // When
            int score = matcher.calculateTemperatureScore(dress, temperature);

            // Then
            assertThat(score).isEqualTo(40);  // 높은 점수
        }

        @Test
        @DisplayName("한파(0도)에서 원피스 낮은 점수")
        void shouldReturnLowScoreForDressInFreezing() {
            // Given
            ClothType dress = ClothType.DRESS;
            double temperature = 0;

            // When
            int score = matcher.calculateTemperatureScore(dress, temperature);

            // Then
            assertThat(score).isEqualTo(10);  // 낮은 점수
        }
    }

    @Nested
    @DisplayName("isOuterRequired - 아우터 필요 여부")
    class IsOuterRequiredTest {

        @Test
        @DisplayName("한파(0도)에서 아우터 필수")
        void shouldRequireOuterInFreezing() {
            // When
            boolean required = matcher.isOuterRequired(0);

            // Then
            assertThat(required).isTrue();
        }

        @Test
        @DisplayName("추운 날(7도)에서 아우터 필수")
        void shouldRequireOuterInCold() {
            // When
            boolean required = matcher.isOuterRequired(7);

            // Then
            assertThat(required).isTrue();
        }

        @Test
        @DisplayName("선선한 날(15도)에서 아우터 선택적")
        void shouldNotRequireOuterInCool() {
            // When
            boolean required = matcher.isOuterRequired(15);

            // Then
            assertThat(required).isFalse();
        }

        @Test
        @DisplayName("더운 날(30도)에서 아우터 불필요")
        void shouldNotRequireOuterInHot() {
            // When
            boolean required = matcher.isOuterRequired(30);

            // Then
            assertThat(required).isFalse();
        }
    }

    @Nested
    @DisplayName("getSuitableClothTypes - 적합 의상 타입 조회")
    class GetSuitableClothTypesTest {

        @Test
        @DisplayName("한파 시 모든 의상 타입에 대한 점수 반환")
        void shouldReturnScoresForAllTypesInFreezing() {
            // When
            Map<ClothType, Integer> scores = matcher.getSuitableClothTypes(0);

            // Then
            assertThat(scores).containsKey(ClothType.OUTER);
            assertThat(scores).containsKey(ClothType.TOP);
            assertThat(scores).containsKey(ClothType.BOTTOM);
            assertThat(scores.get(ClothType.OUTER)).isGreaterThan(scores.get(ClothType.DRESS));
        }
    }

    @Nested
    @DisplayName("getTemperatureDescription - 온도 설명")
    class GetTemperatureDescriptionTest {

        @Test
        @DisplayName("0도는 한파로 표시")
        void shouldReturnFreezingDescription() {
            assertThat(matcher.getTemperatureDescription(0)).isEqualTo("한파");
        }

        @Test
        @DisplayName("7도는 추움으로 표시")
        void shouldReturnColdDescription() {
            assertThat(matcher.getTemperatureDescription(7)).isEqualTo("추움");
        }

        @Test
        @DisplayName("15도는 쌀쌀함으로 표시")
        void shouldReturnCoolDescription() {
            assertThat(matcher.getTemperatureDescription(15)).isEqualTo("쌀쌀함");
        }

        @Test
        @DisplayName("18도는 선선함으로 표시")
        void shouldReturnMildDescription() {
            assertThat(matcher.getTemperatureDescription(18)).isEqualTo("선선함");
        }

        @Test
        @DisplayName("25도는 따뜻함으로 표시")
        void shouldReturnWarmDescription() {
            assertThat(matcher.getTemperatureDescription(25)).isEqualTo("따뜻함");
        }

        @Test
        @DisplayName("30도는 더움으로 표시")
        void shouldReturnHotDescription() {
            assertThat(matcher.getTemperatureDescription(30)).isEqualTo("더움");
        }
    }
}
