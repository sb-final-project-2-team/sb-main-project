package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 계절 기반 의상 필터링
 * - 기온에 따라 허용 계절 결정
 * - 온도 민감도에 따라 허용 범위 조정
 * - 아우터는 겨울(≤9°C)에 WINTER만 허용
 */
@Component
public class SeasonFilter {

    private static final String ATTR_SEASON = "SEASON";

    // 계절 상수
    private static final String WINTER = "WINTER";
    private static final String FALL = "FALL";
    private static final String SPRING = "SPRING";
    private static final String SUMMER = "SUMMER";

    // 아우터 특별 규칙 적용 온도 기준
    private static final double WINTER_OUTER_THRESHOLD = 9.0;

    /**
     * 체감온도와 민감도에 따른 허용 계절 목록 반환
     * @param adjustedTemp 보정된 체감온도
     * @param sensitivity 온도 민감도 (1-5)
     * @return 허용 계절 Set
     */
    public Set<String> getAllowedSeasons(double adjustedTemp, int sensitivity) {
        Set<String> allowed = new HashSet<>();

        // 기본 계절 결정
        String primarySeason = determinePrimarySeason(adjustedTemp);
        allowed.add(primarySeason);

        // 민감도에 따른 인접 계절 추가
        if (sensitivity <= 2) {
            // 추위 많이 탐 → 더 따뜻한 계절만 추가
            addWarmerAdjacentSeason(allowed, primarySeason);
        } else if (sensitivity >= 4) {
            // 더위 많이 탐 → 더 시원한 계절도 추가
            addCoolerAdjacentSeason(allowed, primarySeason);
        } else {
            // 보통 → 양쪽 인접 계절 추가
            addAllAdjacentSeasons(allowed, primarySeason);
        }

        return allowed;
    }

    /**
     * 의상 리스트에서 허용 계절에 해당하는 의상만 필터링
     * @param clothes 전체 의상 리스트
     * @param adjustedTemp 보정된 체감온도
     * @param sensitivity 온도 민감도
     * @param attributeMaps 속성 맵
     * @return 필터링된 의상 리스트
     */
    public List<Cloth> filterByAllowedSeasons(List<Cloth> clothes, double adjustedTemp,
                                               int sensitivity,
                                               Map<UUID, Map<String, String>> attributeMaps) {
        Set<String> allowedSeasons = getAllowedSeasons(adjustedTemp, sensitivity);

        return clothes.stream()
                .filter(cloth -> isClothAllowed(cloth, adjustedTemp, allowedSeasons, attributeMaps))
                .collect(Collectors.toList());
    }

    /**
     * 개별 의상이 허용되는지 확인
     * - 아우터 특별 규칙 적용
     * - 계절 속성 없으면 허용 (기존 데이터 호환성)
     */
    public boolean isClothAllowed(Cloth cloth, double adjustedTemp,
                                   Set<String> allowedSeasons,
                                   Map<UUID, Map<String, String>> attributeMaps) {
        String clothSeason = getSeasonAttribute(cloth.getId(), attributeMaps);

        // 계절 속성 없으면 허용
        if (clothSeason == null || clothSeason.isEmpty()) {
            return true;
        }

        // 아우터 특별 규칙: 겨울에는 WINTER 아우터만
        if (cloth.getType() == ClothType.OUTER && adjustedTemp <= WINTER_OUTER_THRESHOLD) {
            return WINTER.equalsIgnoreCase(clothSeason);
        }

        // 일반 계절 필터링
        return allowedSeasons.stream()
                .anyMatch(season -> season.equalsIgnoreCase(clothSeason));
    }

    /**
     * 필터링 후 의상이 부족한 경우 확장된 계절로 재필터링
     * @param clothes 전체 의상 리스트
     * @param adjustedTemp 보정된 체감온도
     * @param attributeMaps 속성 맵
     * @return 확장 필터링된 의상 리스트
     */
    public List<Cloth> filterWithExpandedSeasons(List<Cloth> clothes, double adjustedTemp,
                                                  Map<UUID, Map<String, String>> attributeMaps) {
        // 모든 인접 계절 허용
        Set<String> expandedSeasons = getAllowedSeasons(adjustedTemp, 3);

        // 추가로 한 단계 더 확장 (새로 추가된 계절들의 인접 계절도 추가)
        Set<String> toExpand = new HashSet<>(expandedSeasons);
        for (String season : toExpand) {
            addAllAdjacentSeasons(expandedSeasons, season);
        }

        return clothes.stream()
                .filter(cloth -> isClothAllowedExpanded(cloth, adjustedTemp, expandedSeasons, attributeMaps))
                .collect(Collectors.toList());
    }

    /**
     * 확장 필터링 (아우터 규칙 완화)
     */
    private boolean isClothAllowedExpanded(Cloth cloth, double adjustedTemp,
                                           Set<String> allowedSeasons,
                                           Map<UUID, Map<String, String>> attributeMaps) {
        String clothSeason = getSeasonAttribute(cloth.getId(), attributeMaps);

        // 계절 속성 없으면 허용
        if (clothSeason == null || clothSeason.isEmpty()) {
            return true;
        }

        // 확장 모드에서도 아우터 규칙 유지 (단, FALL까지 허용)
        if (cloth.getType() == ClothType.OUTER && adjustedTemp <= WINTER_OUTER_THRESHOLD) {
            return WINTER.equalsIgnoreCase(clothSeason) || FALL.equalsIgnoreCase(clothSeason);
        }

        return allowedSeasons.stream()
                .anyMatch(season -> season.equalsIgnoreCase(clothSeason));
    }

    /**
     * 온도에 따른 주 계절 결정
     */
    public String determinePrimarySeason(double temp) {
        if (temp <= 9) return WINTER;
        if (temp <= 16) return FALL;
        if (temp <= 22) return SPRING;
        return SUMMER;
    }

    /**
     * 더 따뜻한 인접 계절 추가 (추위 많이 타는 사용자용)
     * SUMMER → (추가 없음)
     * SPRING → SUMMER 추가
     * FALL → WINTER 추가
     * WINTER → (추가 없음, 이미 가장 따뜻한 옷)
     */
    private void addWarmerAdjacentSeason(Set<String> seasons, String primarySeason) {
        switch (primarySeason) {
            case SPRING -> seasons.add(SUMMER);
            case FALL -> seasons.add(WINTER);
            // WINTER, SUMMER는 추가 없음
        }
    }

    /**
     * 더 시원한 인접 계절 추가 (더위 많이 타는 사용자용)
     * WINTER → FALL 추가
     * FALL → SPRING 추가
     * SPRING → SUMMER 추가
     * SUMMER → (추가 없음, 이미 가장 시원한 옷)
     */
    private void addCoolerAdjacentSeason(Set<String> seasons, String primarySeason) {
        switch (primarySeason) {
            case WINTER -> seasons.add(FALL);
            case FALL -> seasons.add(SPRING);
            case SPRING -> seasons.add(SUMMER);
            // SUMMER는 추가 없음
        }
    }

    /**
     * 양쪽 인접 계절 모두 추가 (보통 민감도 사용자용)
     */
    private void addAllAdjacentSeasons(Set<String> seasons, String primarySeason) {
        switch (primarySeason) {
            case WINTER -> seasons.add(FALL);
            case FALL -> {
                seasons.add(WINTER);
                seasons.add(SPRING);
            }
            case SPRING -> {
                seasons.add(FALL);
                seasons.add(SUMMER);
            }
            case SUMMER -> seasons.add(SPRING);
        }
    }

    /**
     * 의상의 계절 속성 조회
     */
    private String getSeasonAttribute(UUID clothId, Map<UUID, Map<String, String>> attributeMaps) {
        Map<String, String> attrs = attributeMaps.get(clothId);
        if (attrs == null) {
            return null;
        }
        return attrs.get(ATTR_SEASON);
    }
}
