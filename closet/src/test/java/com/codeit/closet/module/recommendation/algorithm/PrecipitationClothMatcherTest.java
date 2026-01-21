package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PrecipitationClothMatcher 테스트")
class PrecipitationClothMatcherTest {

    private PrecipitationClothMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new PrecipitationClothMatcher();
    }

    @Nested
    @DisplayName("getPrecipitationWeight - 강수 조건 가중치")
    class GetPrecipitationWeightTest {

        @Test
        @DisplayName("강수 없음(NONE)일 때 가중치 0")
        void shouldReturnZeroForNoPrecipitation() {
            assertThat(matcher.getPrecipitationWeight(PrecipitationType.NONE, ClothType.TOP)).isZero();
            assertThat(matcher.getPrecipitationWeight(PrecipitationType.NONE, ClothType.OUTER)).isZero();
        }

        @Test
        @DisplayName("null일 때 가중치 0")
        void shouldReturnZeroForNull() {
            assertThat(matcher.getPrecipitationWeight(null, ClothType.TOP)).isZero();
        }

        @Test
        @DisplayName("비 올 때 아우터 보너스 +20")
        void shouldReturnBonusForOuterInRain() {
            assertThat(matcher.getPrecipitationWeight(PrecipitationType.RAIN, ClothType.OUTER)).isEqualTo(20);
        }

        @Test
        @DisplayName("비 올 때 드레스 페널티 -30")
        void shouldReturnPenaltyForDressInRain() {
            assertThat(matcher.getPrecipitationWeight(PrecipitationType.RAIN, ClothType.DRESS)).isEqualTo(-30);
        }

        @Test
        @DisplayName("비 올 때 일반 의상 가중치 0")
        void shouldReturnZeroForNormalClothInRain() {
            assertThat(matcher.getPrecipitationWeight(PrecipitationType.RAIN, ClothType.TOP)).isZero();
        }

        @Test
        @DisplayName("눈 올 때 아우터/목도리/모자 보너스 +20")
        void shouldReturnBonusForWinterItemsInSnow() {
            assertThat(matcher.getPrecipitationWeight(PrecipitationType.SNOW, ClothType.OUTER)).isEqualTo(20);
            assertThat(matcher.getPrecipitationWeight(PrecipitationType.SNOW, ClothType.SCARF)).isEqualTo(20);
            assertThat(matcher.getPrecipitationWeight(PrecipitationType.SNOW, ClothType.HAT)).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("calculateNormalizedPrecipitationScore - 정규화 점수")
    class CalculateNormalizedScoreTest {

        @Test
        @DisplayName("강수 없음(NONE)일 때 0점 반환")
        void shouldReturnZeroForNoPrecipitation() {
            assertThat(matcher.calculateNormalizedPrecipitationScore(PrecipitationType.NONE, ClothType.TOP)).isZero();
            assertThat(matcher.calculateNormalizedPrecipitationScore(PrecipitationType.NONE, ClothType.OUTER)).isZero();
        }

        @Test
        @DisplayName("null일 때 0점 반환")
        void shouldReturnZeroForNull() {
            assertThat(matcher.calculateNormalizedPrecipitationScore(null, ClothType.TOP)).isZero();
        }

        @Test
        @DisplayName("비 올 때 아우터 최대 15점")
        void shouldReturnMaxScoreForOuterInRain() {
            // rawScore = +20 → (20+30)/50*15 = 15점
            assertThat(matcher.calculateNormalizedPrecipitationScore(PrecipitationType.RAIN, ClothType.OUTER)).isEqualTo(15);
        }

        @Test
        @DisplayName("비 올 때 드레스 0점")
        void shouldReturnZeroForDressInRain() {
            // rawScore = -30 → (-30+30)/50*15 = 0점
            assertThat(matcher.calculateNormalizedPrecipitationScore(PrecipitationType.RAIN, ClothType.DRESS)).isZero();
        }

        @Test
        @DisplayName("비 올 때 일반 의상 9점")
        void shouldReturnMiddleScoreForNormalClothInRain() {
            // rawScore = 0 → (0+30)/50*15 = 9점
            assertThat(matcher.calculateNormalizedPrecipitationScore(PrecipitationType.RAIN, ClothType.TOP)).isEqualTo(9);
        }

        @Test
        @DisplayName("점수 범위는 0-15점")
        void scoreShouldBeInRange() {
            for (PrecipitationType precip : PrecipitationType.values()) {
                for (ClothType cloth : ClothType.values()) {
                    int score = matcher.calculateNormalizedPrecipitationScore(precip, cloth);
                    assertThat(score).isBetween(0, 15);
                }
            }
        }
    }

    @Nested
    @DisplayName("getPrecipitationDescription - 강수 설명")
    class GetPrecipitationDescriptionTest {

        @Test
        @DisplayName("강수 유형별 설명 반환")
        void shouldReturnDescription() {
            assertThat(matcher.getPrecipitationDescription(PrecipitationType.NONE)).isEmpty();
            assertThat(matcher.getPrecipitationDescription(PrecipitationType.RAIN)).isEqualTo("비");
            assertThat(matcher.getPrecipitationDescription(PrecipitationType.SNOW)).isEqualTo("눈");
            assertThat(matcher.getPrecipitationDescription(PrecipitationType.RAIN_SNOW)).isEqualTo("비/눈");
            assertThat(matcher.getPrecipitationDescription(PrecipitationType.SHOWER)).isEqualTo("소나기");
        }

        @Test
        @DisplayName("null일 때 빈 문자열 반환")
        void shouldReturnEmptyForNull() {
            assertThat(matcher.getPrecipitationDescription(null)).isEmpty();
        }
    }
}
