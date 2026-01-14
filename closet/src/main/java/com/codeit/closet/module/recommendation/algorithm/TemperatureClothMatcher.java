package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.recommendation.config.TemperatureRangeConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 온도 기반 점수 계산 (0-40점)
 * - 체감온도 보정: sensitivity(1-5) → ±2도
 */
@Component
public class TemperatureClothMatcher {

    /**
     * 체감온도 보정
     * - sensitivity 1 (추위 많이 탐): +2도 보정 → 더 따뜻한 옷 추천
     * - sensitivity 3 (보통): 보정 없음
     * - sensitivity 5 (더위 많이 탐): -2도 보정 → 더 시원한 옷 추천
     *
     * @param actualTemp 실제 기온
     * @param sensitivity 사용자 온도 민감도 (1-5)
     * @return 보정된 체감 온도
     */
    public double adjustTemperature(double actualTemp, int sensitivity) {
        // sensitivity 범위 검증 (1-5)
        int validSensitivity = Math.max(1, Math.min(5, sensitivity));
        double adjustment = (3 - validSensitivity) * 1.0;  // -2 ~ +2도 범위
        return actualTemp + adjustment;
    }

    /**
     * 온도에 따른 의상 타입별 적합도 점수 반환
     * @param temperature 온도 (보정된 체감온도 권장)
     * @return 의상 타입별 적합도 맵 (0-40점)
     */
    public Map<ClothType, Integer> getSuitableClothTypes(double temperature) {
        return TemperatureRangeConfig.getSuitableClothTypes(temperature);
    }

    /**
     * 특정 의상 타입의 온도 적합도 점수 계산
     * @param clothType 의상 타입
     * @param temperature 온도
     * @return 적합도 점수 (0-40)
     */
    public int calculateTemperatureScore(ClothType clothType, double temperature) {
        Map<ClothType, Integer> suitability = getSuitableClothTypes(temperature);
        return suitability.getOrDefault(clothType, 20);
    }

    /**
     * 아우터가 필요한지 여부
     * @param temperature 온도
     * @return 아우터 필요 여부
     */
    public boolean isOuterRequired(double temperature) {
        return TemperatureRangeConfig.isRequiredType(ClothType.OUTER, temperature);
    }

    /**
     * 온도 구간 설명 반환
     * @param temperature 온도
     * @return 설명 문자열 (예: "한파", "추움", "따뜻함")
     */
    public String getTemperatureDescription(double temperature) {
        return TemperatureRangeConfig.getTemperatureDescription(temperature);
    }
}
