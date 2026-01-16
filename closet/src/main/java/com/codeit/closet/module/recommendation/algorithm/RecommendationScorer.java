package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.weather.entity.WeatherData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 추천 점수 종합 계산 (0-100점 클램핑)
 * - 온도(0-40) + 강수(0-15) + 속성(0-30) = 최대 85점
 */
@Component
@RequiredArgsConstructor
public class RecommendationScorer {

    private final TemperatureClothMatcher temperatureClothMatcher;
    private final PrecipitationClothMatcher precipitationClothMatcher;
    private final AttributeScoreCalculator attributeScoreCalculator;

    /**
     * 단일 의상의 점수 계산
     * @param cloth 의상
     * @param weather 날씨 데이터
     * @param userSensitivity 사용자 온도 민감도 (1-5)
     * @param attributeMaps 미리 로드된 속성 맵 (clothId -> (속성명 -> 속성값))
     * @return 의상 점수 (0-100)
     */
    public int calculateClothScore(Cloth cloth, WeatherData weather, int userSensitivity,
                                   Map<UUID, Map<String, String>> attributeMaps) {
        double adjustedTemp = temperatureClothMatcher.adjustTemperature(
                weather.getTemperatureCurrent(), userSensitivity);

        // 1. 온도 적합도 점수 (0-40점)
        int tempScore = temperatureClothMatcher.calculateTemperatureScore(cloth.getType(), adjustedTemp);

        // 2. 강수 적합도 점수 (0-15점) - 정규화 적용
        int precipScore = precipitationClothMatcher.calculateNormalizedPrecipitationScore(
                weather.getPrecipitationType(), cloth.getType());

        // 3. 의상 속성 점수 (0-30점)
        int attributeScore = attributeScoreCalculator.calculateAttributeScore(cloth, adjustedTemp, weather, attributeMaps);

        // 4. 최종 점수 (0-100 범위로 클램핑)
        int totalScore = tempScore + precipScore + attributeScore;
        return Math.max(0, Math.min(100, totalScore));
    }

    /**
     * 코디 조합의 총점 계산
     * 각 의상 점수의 가중 평균
     * @param outfit 코디 조합 (의상 리스트)
     * @param weather 날씨 데이터
     * @param userSensitivity 사용자 온도 민감도
     * @param attributeMaps 미리 로드된 속성 맵 (clothId -> (속성명 -> 속성값))
     * @return 코디 점수 (0-100)
     */
    public int calculateOutfitScore(List<Cloth> outfit, WeatherData weather, int userSensitivity,
                                    Map<UUID, Map<String, String>> attributeMaps) {
        if (outfit == null || outfit.isEmpty()) {
            return 0;
        }

        // 각 의상 점수 합산
        int totalScore = 0;
        for (Cloth cloth : outfit) {
            totalScore += calculateClothScore(cloth, weather, userSensitivity, attributeMaps);
        }

        // 평균 점수 반환
        return totalScore / outfit.size();
    }

    /**
     * 보정된 체감온도 계산 (외부에서 사용 가능)
     */
    public double getAdjustedTemperature(double actualTemp, int userSensitivity) {
        return temperatureClothMatcher.adjustTemperature(actualTemp, userSensitivity);
    }
}
