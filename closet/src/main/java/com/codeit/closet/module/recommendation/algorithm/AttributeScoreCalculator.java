package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.weather.entity.WeatherData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * 의상 속성 기반 점수 계산 (0-30점)
 * - 현재 DB에 정의된 속성: SEASON, COLOR, SIZE
 * - 추천에 활용하는 속성: SEASON (계절)
 */
@Component
public class AttributeScoreCalculator {

    // 속성명 상수 (DB 스키마와 일치)
    private static final String ATTR_SEASON = "SEASON";

    // 속성 점수 상수
    private static final int DEFAULT_ATTRIBUTE_SCORE = 15;
    private static final int MAX_ATTRIBUTE_SCORE = 30;

    // 계절 점수 상수 (0-30점 범위)
    private static final int SEASON_EXACT_MATCH_SCORE = 30;
    private static final int SEASON_ADJACENT_SCORE = 15;
    private static final int SEASON_MISMATCH_SCORE = 5;
    private static final int SEASON_DEFAULT_SCORE = 15;

    /**
     * 의상 속성 기반 점수 계산
     * @param cloth 의상 엔티티
     * @param adjustedTemp 보정된 체감온도
     * @param weather 날씨 데이터 (현재 미사용, 확장성 위해 유지)
     * @param attributeMaps 미리 로드된 속성 맵 (clothId -> (속성명 -> 속성값))
     * @return 속성 점수 (0-30점)
     */
    public int calculateAttributeScore(Cloth cloth, double adjustedTemp, WeatherData weather,
                                       Map<UUID, Map<String, String>> attributeMaps) {
        // attributeMaps null 방어
        Map<UUID, Map<String, String>> safeAttributeMaps =
                attributeMaps != null ? attributeMaps : Collections.emptyMap();
        Map<String, String> attributes = safeAttributeMaps.getOrDefault(cloth.getId(), Collections.emptyMap());

        if (attributes.isEmpty()) {
            return DEFAULT_ATTRIBUTE_SCORE;
        }

        // 계절 속성 점수 (0-30점)
        int score = calculateSeasonScore(attributes.get(ATTR_SEASON), adjustedTemp);

        return Math.min(MAX_ATTRIBUTE_SCORE, score);
    }

    /**
     * 계절 속성 점수 계산
     * @param season 의상의 계절 속성값 (SPRING, SUMMER, FALL, WINTER)
     * @param temp 보정된 체감온도
     * @return 계절 점수 (0-30점)
     */
    private int calculateSeasonScore(String season, double temp) {
        if (season == null || season.isEmpty()) {
            return SEASON_DEFAULT_SCORE;
        }

        String currentSeason = getSeasonFromTemp(temp);

        // 정확히 매칭되면 최고 점수
        if (season.toUpperCase().contains(currentSeason)) {
            return SEASON_EXACT_MATCH_SCORE;
        }

        // 인접 계절이면 부분 점수
        if (isAdjacentSeason(season, currentSeason)) {
            return SEASON_ADJACENT_SCORE;
        }

        // 계절 불일치
        return SEASON_MISMATCH_SCORE;
    }

    /**
     * 온도에 따른 계절 판단 (DB 스키마와 일치하는 영문 반환)
     * @param temp 온도
     * @return 계절 문자열 (WINTER, FALL, SPRING, SUMMER)
     */
    private String getSeasonFromTemp(double temp) {
        if (temp <= 9) return "WINTER";
        if (temp <= 16) return "FALL";
        if (temp <= 22) return "SPRING";
        return "SUMMER";
    }

    /**
     * 인접 계절 여부 판단
     * - SPRING ↔ FALL 은 서로 인접 계절로 간주
     * @param clothSeason 의상의 계절 속성값
     * @param currentSeason 현재 온도 기반 계절
     * @return 인접 계절 여부
     */
    private boolean isAdjacentSeason(String clothSeason, String currentSeason) {
        String upperSeason = clothSeason.toUpperCase();
        // 봄/가을 계절 옷은 서로 인접
        if (upperSeason.contains("SPRING") || upperSeason.contains("FALL")) {
            return currentSeason.equals("SPRING") || currentSeason.equals("FALL");
        }
        return false;
    }
}
