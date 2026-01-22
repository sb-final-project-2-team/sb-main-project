package com.codeit.closet.module.recommendation.config;

import com.codeit.closet.module.cloth.entity.ClothType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.codeit.closet.module.recommendation.config.TemperatureRangeConfig.TemperatureRange;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TemperatureRangeConfig 테스트")
class TemperatureRangeConfigTest {

    @Nested
    @DisplayName("TemperatureRange.fromTemperature - 온도 구간 판단")
    class FromTemperatureTest {

        @Test
        @DisplayName("4도 이하는 FREEZING")
        void shouldReturnFreezingForVeryLowTemp() {
            assertThat(TemperatureRange.fromTemperature(4.0)).isEqualTo(TemperatureRange.FREEZING);
            assertThat(TemperatureRange.fromTemperature(0.0)).isEqualTo(TemperatureRange.FREEZING);
            assertThat(TemperatureRange.fromTemperature(-10.0)).isEqualTo(TemperatureRange.FREEZING);
        }

        @Test
        @DisplayName("5-9도는 COLD")
        void shouldReturnColdForColdTemp() {
            assertThat(TemperatureRange.fromTemperature(5.0)).isEqualTo(TemperatureRange.COLD);
            assertThat(TemperatureRange.fromTemperature(7.0)).isEqualTo(TemperatureRange.COLD);
            assertThat(TemperatureRange.fromTemperature(9.0)).isEqualTo(TemperatureRange.COLD);
        }

        @Test
        @DisplayName("10-16도는 COOL")
        void shouldReturnCoolForCoolTemp() {
            assertThat(TemperatureRange.fromTemperature(10.0)).isEqualTo(TemperatureRange.COOL);
            assertThat(TemperatureRange.fromTemperature(13.0)).isEqualTo(TemperatureRange.COOL);
            assertThat(TemperatureRange.fromTemperature(16.0)).isEqualTo(TemperatureRange.COOL);
        }

        @Test
        @DisplayName("17-19도는 MILD")
        void shouldReturnMildForMildTemp() {
            assertThat(TemperatureRange.fromTemperature(17.0)).isEqualTo(TemperatureRange.MILD);
            assertThat(TemperatureRange.fromTemperature(18.0)).isEqualTo(TemperatureRange.MILD);
            assertThat(TemperatureRange.fromTemperature(19.0)).isEqualTo(TemperatureRange.MILD);
        }

        @Test
        @DisplayName("20-27도는 WARM")
        void shouldReturnWarmForWarmTemp() {
            assertThat(TemperatureRange.fromTemperature(20.0)).isEqualTo(TemperatureRange.WARM);
            assertThat(TemperatureRange.fromTemperature(24.0)).isEqualTo(TemperatureRange.WARM);
            assertThat(TemperatureRange.fromTemperature(27.0)).isEqualTo(TemperatureRange.WARM);
        }

        @Test
        @DisplayName("28도 이상은 HOT")
        void shouldReturnHotForHighTemp() {
            assertThat(TemperatureRange.fromTemperature(28.0)).isEqualTo(TemperatureRange.HOT);
            assertThat(TemperatureRange.fromTemperature(35.0)).isEqualTo(TemperatureRange.HOT);
            assertThat(TemperatureRange.fromTemperature(40.0)).isEqualTo(TemperatureRange.HOT);
        }

        @Test
        @DisplayName("경계값 테스트 - 반올림 적용")
        void shouldApplyRoundingForBoundaryValues() {
            // 4.4 -> 반올림 4 -> FREEZING
            assertThat(TemperatureRange.fromTemperature(4.4)).isEqualTo(TemperatureRange.FREEZING);
            // 4.5 -> 반올림 5 -> COLD
            assertThat(TemperatureRange.fromTemperature(4.5)).isEqualTo(TemperatureRange.COLD);
            // 9.4 -> 반올림 9 -> COLD
            assertThat(TemperatureRange.fromTemperature(9.4)).isEqualTo(TemperatureRange.COLD);
            // 9.5 -> 반올림 10 -> COOL
            assertThat(TemperatureRange.fromTemperature(9.5)).isEqualTo(TemperatureRange.COOL);
        }

        @Test
        @DisplayName("극한 저온은 FREEZING")
        void shouldReturnFreezingForExtremelyLowTemp() {
            assertThat(TemperatureRange.fromTemperature(-50.0)).isEqualTo(TemperatureRange.FREEZING);
            assertThat(TemperatureRange.fromTemperature(-100.0)).isEqualTo(TemperatureRange.FREEZING);
        }

        @Test
        @DisplayName("극한 고온은 HOT")
        void shouldReturnHotForExtremelyHighTemp() {
            assertThat(TemperatureRange.fromTemperature(50.0)).isEqualTo(TemperatureRange.HOT);
            assertThat(TemperatureRange.fromTemperature(100.0)).isEqualTo(TemperatureRange.HOT);
        }
    }

    @Nested
    @DisplayName("getSuitableClothTypes - 온도별 의상 타입 적합도")
    class GetSuitableClothTypesTest {

        @Test
        @DisplayName("FREEZING 온도에서 OUTER 최고 점수")
        void shouldReturnHighOuterScoreInFreezing() {
            Map<ClothType, Integer> suitability = TemperatureRangeConfig.getSuitableClothTypes(0.0);

            assertThat(suitability.get(ClothType.OUTER)).isEqualTo(40);
            assertThat(suitability.get(ClothType.TOP)).isEqualTo(35);
            assertThat(suitability.get(ClothType.DRESS)).isEqualTo(10);
        }

        @Test
        @DisplayName("COLD 온도에서 OUTER 높은 점수")
        void shouldReturnHighOuterScoreInCold() {
            Map<ClothType, Integer> suitability = TemperatureRangeConfig.getSuitableClothTypes(7.0);

            assertThat(suitability.get(ClothType.OUTER)).isEqualTo(38);
            assertThat(suitability.get(ClothType.DRESS)).isEqualTo(15);
        }

        @Test
        @DisplayName("COOL 온도에서 TOP 높은 점수")
        void shouldReturnHighTopScoreInCool() {
            Map<ClothType, Integer> suitability = TemperatureRangeConfig.getSuitableClothTypes(13.0);

            assertThat(suitability.get(ClothType.TOP)).isEqualTo(38);
            assertThat(suitability.get(ClothType.OUTER)).isEqualTo(32);
        }

        @Test
        @DisplayName("MILD 온도에서 TOP 최고 점수")
        void shouldReturnMaxTopScoreInMild() {
            Map<ClothType, Integer> suitability = TemperatureRangeConfig.getSuitableClothTypes(18.0);

            assertThat(suitability.get(ClothType.TOP)).isEqualTo(40);
            assertThat(suitability.get(ClothType.OUTER)).isEqualTo(22);
        }

        @Test
        @DisplayName("WARM 온도에서 DRESS 높은 점수")
        void shouldReturnHighDressScoreInWarm() {
            Map<ClothType, Integer> suitability = TemperatureRangeConfig.getSuitableClothTypes(24.0);

            assertThat(suitability.get(ClothType.TOP)).isEqualTo(40);
            assertThat(suitability.get(ClothType.DRESS)).isEqualTo(38);
            assertThat(suitability.get(ClothType.OUTER)).isEqualTo(15);
        }

        @Test
        @DisplayName("HOT 온도에서 DRESS, TOP 최고 점수, OUTER 최저 점수")
        void shouldReturnMaxDressAndMinOuterScoreInHot() {
            Map<ClothType, Integer> suitability = TemperatureRangeConfig.getSuitableClothTypes(35.0);

            assertThat(suitability.get(ClothType.DRESS)).isEqualTo(40);
            assertThat(suitability.get(ClothType.TOP)).isEqualTo(40);
            assertThat(suitability.get(ClothType.OUTER)).isEqualTo(5);
            assertThat(suitability.get(ClothType.SCARF)).isEqualTo(5);
        }

        @Test
        @DisplayName("모든 온도 구간에서 모든 ClothType에 대한 값 존재")
        void shouldHaveValuesForAllClothTypes() {
            double[] temps = {0.0, 7.0, 13.0, 18.0, 24.0, 35.0};

            for (double temp : temps) {
                Map<ClothType, Integer> suitability = TemperatureRangeConfig.getSuitableClothTypes(temp);

                for (ClothType type : ClothType.values()) {
                    assertThat(suitability).containsKey(type);
                    assertThat(suitability.get(type)).isBetween(0, 40);
                }
            }
        }
    }

    @Nested
    @DisplayName("isRequiredType - 필수 의상 타입 판단")
    class IsRequiredTypeTest {

        @Test
        @DisplayName("FREEZING 온도에서 OUTER 필수")
        void shouldReturnTrueForOuterInFreezing() {
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.OUTER, 0.0)).isTrue();
        }

        @Test
        @DisplayName("COLD 온도에서 OUTER 필수")
        void shouldReturnTrueForOuterInCold() {
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.OUTER, 7.0)).isTrue();
        }

        @Test
        @DisplayName("COOL 이상 온도에서 OUTER 선택적")
        void shouldReturnFalseForOuterInWarmTemp() {
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.OUTER, 15.0)).isFalse();
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.OUTER, 25.0)).isFalse();
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.OUTER, 35.0)).isFalse();
        }

        @Test
        @DisplayName("TOP은 항상 필수")
        void shouldReturnTrueForTopAlways() {
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.TOP, 0.0)).isTrue();
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.TOP, 15.0)).isTrue();
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.TOP, 35.0)).isTrue();
        }

        @Test
        @DisplayName("BOTTOM은 항상 필수")
        void shouldReturnTrueForBottomAlways() {
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.BOTTOM, 0.0)).isTrue();
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.BOTTOM, 15.0)).isTrue();
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.BOTTOM, 35.0)).isTrue();
        }

        @Test
        @DisplayName("DRESS는 필수가 아님")
        void shouldReturnFalseForDress() {
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.DRESS, 0.0)).isFalse();
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.DRESS, 25.0)).isFalse();
        }

        @Test
        @DisplayName("액세서리 타입은 필수가 아님")
        void shouldReturnFalseForAccessories() {
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.SCARF, 0.0)).isFalse();
            assertThat(TemperatureRangeConfig.isRequiredType(ClothType.HAT, 0.0)).isFalse();
        }
    }

    @Nested
    @DisplayName("getTemperatureDescription - 온도 설명")
    class GetTemperatureDescriptionTest {

        @Test
        @DisplayName("FREEZING 온도에서 '한파' 반환")
        void shouldReturnFreezingDescription() {
            assertThat(TemperatureRangeConfig.getTemperatureDescription(0.0)).isEqualTo("한파");
            assertThat(TemperatureRangeConfig.getTemperatureDescription(-10.0)).isEqualTo("한파");
        }

        @Test
        @DisplayName("COLD 온도에서 '추움' 반환")
        void shouldReturnColdDescription() {
            assertThat(TemperatureRangeConfig.getTemperatureDescription(7.0)).isEqualTo("추움");
        }

        @Test
        @DisplayName("COOL 온도에서 '쌀쌀함' 반환")
        void shouldReturnCoolDescription() {
            assertThat(TemperatureRangeConfig.getTemperatureDescription(13.0)).isEqualTo("쌀쌀함");
        }

        @Test
        @DisplayName("MILD 온도에서 '선선함' 반환")
        void shouldReturnMildDescription() {
            assertThat(TemperatureRangeConfig.getTemperatureDescription(18.0)).isEqualTo("선선함");
        }

        @Test
        @DisplayName("WARM 온도에서 '따뜻함' 반환")
        void shouldReturnWarmDescription() {
            assertThat(TemperatureRangeConfig.getTemperatureDescription(24.0)).isEqualTo("따뜻함");
        }

        @Test
        @DisplayName("HOT 온도에서 '더움' 반환")
        void shouldReturnHotDescription() {
            assertThat(TemperatureRangeConfig.getTemperatureDescription(35.0)).isEqualTo("더움");
        }
    }

    @Nested
    @DisplayName("TemperatureRange enum 테스트")
    class TemperatureRangeEnumTest {

        @Test
        @DisplayName("각 구간의 min/max 값 확인")
        void shouldHaveCorrectMinMaxValues() {
            assertThat(TemperatureRange.FREEZING.getMinTemp()).isEqualTo(-999);
            assertThat(TemperatureRange.FREEZING.getMaxTemp()).isEqualTo(4);

            assertThat(TemperatureRange.COLD.getMinTemp()).isEqualTo(5);
            assertThat(TemperatureRange.COLD.getMaxTemp()).isEqualTo(9);

            assertThat(TemperatureRange.COOL.getMinTemp()).isEqualTo(10);
            assertThat(TemperatureRange.COOL.getMaxTemp()).isEqualTo(16);

            assertThat(TemperatureRange.MILD.getMinTemp()).isEqualTo(17);
            assertThat(TemperatureRange.MILD.getMaxTemp()).isEqualTo(19);

            assertThat(TemperatureRange.WARM.getMinTemp()).isEqualTo(20);
            assertThat(TemperatureRange.WARM.getMaxTemp()).isEqualTo(27);

            assertThat(TemperatureRange.HOT.getMinTemp()).isEqualTo(28);
            assertThat(TemperatureRange.HOT.getMaxTemp()).isEqualTo(999);
        }
    }
}
