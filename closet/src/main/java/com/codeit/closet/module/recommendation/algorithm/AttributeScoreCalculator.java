package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.WeatherData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 의상 속성 기반 점수 계산 (0-30점)
 * - 계절: 0-15점, 소재: 0-10점, 두께: 0-5점
 */
@Component
public class AttributeScoreCalculator {

    // 방수 관련 소재 키워드
    private static final Set<String> WATERPROOF_MATERIALS = Set.of(
            "방수", "고어텍스", "나일론", "폴리에스터", "우비"
    );

    // 여름용 소재 키워드
    private static final Set<String> SUMMER_MATERIALS = Set.of(
            "린넨", "면", "코튼", "시어서커", "레이온"
    );

    // 겨울용 소재 키워드
    private static final Set<String> WINTER_MATERIALS = Set.of(
            "울", "캐시미어", "양모", "플리스", "기모", "퍼", "다운", "패딩"
    );

    /**
     * 의상 속성 기반 점수 계산
     * @param cloth 의상 엔티티
     * @param adjustedTemp 보정된 체감온도
     * @param weather 날씨 데이터
     * @param attributeMaps 미리 로드된 속성 맵 (clothId -> (속성명 -> 속성값))
     * @return 속성 점수 (0-30점)
     */
    public int calculateAttributeScore(Cloth cloth, double adjustedTemp, WeatherData weather,
                                       Map<UUID, Map<String, String>> attributeMaps) {
        Map<String, String> attributes = attributeMaps.getOrDefault(cloth.getId(), Collections.emptyMap());

        if (attributes.isEmpty()) {
            return 15;  // 속성 정보 없으면 기본 중간 점수
        }

        int score = 0;

        // 1. 계절 속성 (0-15점)
        score += calculateSeasonScore(attributes.get("계절"), adjustedTemp);

        // 2. 소재 속성 (0-10점)
        score += calculateMaterialScore(attributes.get("소재"), adjustedTemp, weather.getPrecipitationType());

        // 3. 두께 속성 (0-5점)
        score += calculateThicknessScore(attributes.get("두께"), adjustedTemp);

        return Math.min(30, score);  // 최대 30점
    }

    /**
     * 계절 속성 점수 계산
     */
    private int calculateSeasonScore(String season, double temp) {
        if (season == null || season.isEmpty()) {
            return 7;  // 기본 중간 점수
        }

        String currentSeason = getSeasonFromTemp(temp);

        // 정확히 매칭되면 최고 점수
        if (season.contains(currentSeason)) {
            return 15;
        }

        // 인접 계절이면 부분 점수
        if (isAdjacentSeason(season, currentSeason)) {
            return 7;
        }

        // 계절 불일치
        return 2;
    }

    /**
     * 소재 속성 점수 계산
     */
    private int calculateMaterialScore(String material, double temp, PrecipitationType precipType) {
        if (material == null || material.isEmpty()) {
            return 5;  // 기본 중간 점수
        }

        int score = 0;

        // 비/눈 올 때 방수 소재 보너스
        if (precipType != null && precipType != PrecipitationType.NONE) {
            if (containsAny(material, WATERPROOF_MATERIALS)) {
                score += 10;
                return score;  // 방수 소재면 바로 만점
            }
        }

        // 여름에 린넨/면 보너스
        if (temp >= 25 && containsAny(material, SUMMER_MATERIALS)) {
            score += 8;
        }

        // 겨울에 울/캐시미어 보너스
        if (temp <= 10 && containsAny(material, WINTER_MATERIALS)) {
            score += 8;
        }

        // 기본 점수 (적합성 판단 불가)
        if (score == 0) {
            score = 5;
        }

        return Math.min(10, score);
    }

    /**
     * 두께 속성 점수 계산
     */
    private int calculateThicknessScore(String thickness, double temp) {
        if (thickness == null || thickness.isEmpty()) {
            return 2;  // 기본 중간 점수
        }

        // 추울 때 두꺼운 옷 보너스
        if (temp <= 10 && thickness.contains("두꺼움")) {
            return 5;
        }

        // 더울 때 얇은 옷 보너스
        if (temp >= 25 && thickness.contains("얇음")) {
            return 5;
        }

        // 간절기에 보통 두께 보너스
        if (temp > 10 && temp < 25 && thickness.contains("보통")) {
            return 5;
        }

        return 2;
    }

    /**
     * 온도에 따른 계절 판단
     */
    private String getSeasonFromTemp(double temp) {
        if (temp <= 9) return "겨울";
        if (temp <= 16) return "가을";  // 또는 "봄"
        if (temp <= 22) return "봄";    // 또는 "가을"
        return "여름";
    }

    /**
     * 인접 계절 여부 판단
     */
    private boolean isAdjacentSeason(String clothSeason, String currentSeason) {
        // 봄/가을 계절 옷은 서로 인접
        if (clothSeason.contains("봄") || clothSeason.contains("가을")) {
            return currentSeason.equals("봄") || currentSeason.equals("가을");
        }
        return false;
    }

    /**
     * 문자열이 키워드 세트 중 하나를 포함하는지 확인
     */
    private boolean containsAny(String text, Set<String> keywords) {
        String lowerText = text.toLowerCase();
        return keywords.stream().anyMatch(keyword -> lowerText.contains(keyword.toLowerCase()));
    }
}
