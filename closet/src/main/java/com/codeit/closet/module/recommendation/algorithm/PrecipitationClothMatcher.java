package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.ClothType;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 강수 조건 기반 점수 계산 (0-15점)
 * - 방수 아이템: +20 보너스, 부적합 아이템: -30 페널티 → 정규화
 */
@Component
public class PrecipitationClothMatcher {

    // 점수 상수
    private static final int PRECIPITATION_BONUS = 20;
    private static final int PRECIPITATION_PENALTY = -30;
    private static final int MAX_PRECIPITATION_SCORE = 15;
    private static final int RAW_SCORE_OFFSET = 30;
    private static final double RAW_SCORE_RANGE = 50.0;

    // 강수 유형별 추천 의상 타입
    private static final Map<PrecipitationType, Set<ClothType>> RECOMMENDED_FOR_PRECIPITATION;

    // 비/눈 올 때 비추천 의상 타입
    private static final Set<ClothType> AVOID_IN_PRECIPITATION;

    static {
        RECOMMENDED_FOR_PRECIPITATION = new EnumMap<>(PrecipitationType.class);

        // 비 올 때: 아우터(방수 자켓), 신발(방수화)
        RECOMMENDED_FOR_PRECIPITATION.put(PrecipitationType.RAIN,
                EnumSet.of(ClothType.OUTER, ClothType.SHOES));

        // 눈 올 때: 아우터(패딩), 목도리, 모자
        RECOMMENDED_FOR_PRECIPITATION.put(PrecipitationType.SNOW,
                EnumSet.of(ClothType.OUTER, ClothType.SCARF, ClothType.HAT, ClothType.SHOES));

        // 비/눈 올 때: 방수+보온 아우터
        RECOMMENDED_FOR_PRECIPITATION.put(PrecipitationType.RAIN_SNOW,
                EnumSet.of(ClothType.OUTER, ClothType.SCARF, ClothType.SHOES));

        // 소나기: 가벼운 방수 자켓
        RECOMMENDED_FOR_PRECIPITATION.put(PrecipitationType.SHOWER,
                EnumSet.of(ClothType.OUTER));

        // 비/눈 올 때 비추천 의상 (긴 드레스, 얇은 신발류 등)
        AVOID_IN_PRECIPITATION = EnumSet.of(ClothType.DRESS);
    }

    /**
     * 강수 조건에 따른 의상 타입 가중치 계산
     * @param precipType 강수 유형
     * @param clothType 의상 타입
     * @return 가중치 점수 (-30 ~ +20)
     */
    public int getPrecipitationWeight(PrecipitationType precipType, ClothType clothType) {
        if (precipType == null || precipType == PrecipitationType.NONE) {
            return 0;  // 강수 없으면 가중치 없음
        }

        // 비/눈 올 때 추천 의상 보너스
        Set<ClothType> recommended = RECOMMENDED_FOR_PRECIPITATION.get(precipType);
        if (recommended != null && recommended.contains(clothType)) {
            return PRECIPITATION_BONUS;
        }

        // 비/눈 올 때 비추천 의상 페널티
        if (AVOID_IN_PRECIPITATION.contains(clothType)) {
            return PRECIPITATION_PENALTY;
        }

        return 0;
    }

    /**
     * 강수 조건에 따른 정규화된 점수 계산 (0-15점 범위)
     * @param precipType 강수 유형
     * @param clothType 의상 타입
     * @return 정규화된 점수 (0-15)
     */
    public int calculateNormalizedPrecipitationScore(PrecipitationType precipType, ClothType clothType) {
        int rawScore = getPrecipitationWeight(precipType, clothType);
        // rawScore 범위: -30 ~ +20 → 0 ~ 15점으로 정규화
        return (int) ((rawScore + RAW_SCORE_OFFSET) / RAW_SCORE_RANGE * MAX_PRECIPITATION_SCORE);
    }

    /**
     * 강수 상태 설명 반환
     * @param precipType 강수 유형
     * @return 설명 문자열
     */
    public String getPrecipitationDescription(PrecipitationType precipType) {
        if (precipType == null) {
            return "";
        }
        return switch (precipType) {
            case NONE -> "";
            case RAIN -> "비";
            case SNOW -> "눈";
            case RAIN_SNOW -> "비/눈";
            case SHOWER -> "소나기";
        };
    }

}
