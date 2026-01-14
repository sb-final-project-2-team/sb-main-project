package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.cloth.entity.ClothType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 코디 조합 생성 컴포넌트
 * - 필수: (TOP + BOTTOM) 또는 DRESS
 * - 선택: OUTER (온도에 따라 필수화)
 */
@Component
public class OutfitCombinationGenerator {

    /**
     * 의상 목록으로 코디 조합 생성
     * @param clothes 사용자의 의상 목록
     * @param outerRequired 아우터 필수 여부
     * @param maxCombinations 최대 조합 수
     * @return 코디 조합 목록 (각 조합은 의상 리스트)
     */
    public List<List<Cloth>> generateCombinations(
            List<Cloth> clothes,
            boolean outerRequired,
            int maxCombinations) {

        // 의상을 타입별로 그룹화
        Map<ClothType, List<Cloth>> clothesByType = groupByType(clothes);

        List<List<Cloth>> combinations = new ArrayList<>();

        // 1. TOP + BOTTOM 조합 생성
        List<Cloth> tops = clothesByType.getOrDefault(ClothType.TOP, List.of());
        List<Cloth> bottoms = clothesByType.getOrDefault(ClothType.BOTTOM, List.of());
        List<Cloth> outers = clothesByType.getOrDefault(ClothType.OUTER, List.of());

        for (Cloth top : tops) {
            for (Cloth bottom : bottoms) {
                if (combinations.size() >= maxCombinations * 2) break;

                // 기본 조합 (TOP + BOTTOM)
                List<Cloth> outfit = new ArrayList<>();
                outfit.add(top);
                outfit.add(bottom);

                // 아우터 추가
                if (outerRequired && !outers.isEmpty()) {
                    // 아우터 필수면 아우터 포함 조합만 추가
                    for (Cloth outer : outers) {
                        if (combinations.size() >= maxCombinations * 2) break;
                        List<Cloth> outfitWithOuter = new ArrayList<>(outfit);
                        outfitWithOuter.add(outer);
                        combinations.add(outfitWithOuter);
                    }
                } else if (!outers.isEmpty()) {
                    // 아우터 선택적이면 두 버전 모두 추가
                    combinations.add(outfit);
                    for (Cloth outer : outers) {
                        if (combinations.size() >= maxCombinations * 2) break;
                        List<Cloth> outfitWithOuter = new ArrayList<>(outfit);
                        outfitWithOuter.add(outer);
                        combinations.add(outfitWithOuter);
                    }
                } else {
                    combinations.add(outfit);
                }
            }
        }

        // 2. DRESS 단독 조합 생성
        List<Cloth> dresses = clothesByType.getOrDefault(ClothType.DRESS, List.of());
        for (Cloth dress : dresses) {
            if (combinations.size() >= maxCombinations * 2) break;

            List<Cloth> outfit = new ArrayList<>();
            outfit.add(dress);

            // 아우터 추가 (선택적)
            if (!outers.isEmpty()) {
                for (Cloth outer : outers) {
                    if (combinations.size() >= maxCombinations * 2) break;
                    List<Cloth> outfitWithOuter = new ArrayList<>(outfit);
                    outfitWithOuter.add(outer);
                    combinations.add(outfitWithOuter);
                }
                // 아우터 필수가 아니면 원피스만으로도 조합 추가
                if (!outerRequired) {
                    combinations.add(outfit);
                }
            } else {
                combinations.add(outfit);
            }
        }

        return combinations;
    }

    /**
     * 유효한 코디 조합인지 검증
     * - TOP + BOTTOM 또는 DRESS 필수
     */
    public boolean isValidCombination(List<Cloth> outfit) {
        if (outfit == null || outfit.isEmpty()) {
            return false;
        }

        boolean hasTop = outfit.stream().anyMatch(c -> c.getType() == ClothType.TOP);
        boolean hasBottom = outfit.stream().anyMatch(c -> c.getType() == ClothType.BOTTOM);
        boolean hasDress = outfit.stream().anyMatch(c -> c.getType() == ClothType.DRESS);

        return (hasTop && hasBottom) || hasDress;
    }

    /**
     * 추천 가능한 코디 조합을 만들 수 있는지 확인
     */
    public boolean canGenerateCombinations(List<Cloth> clothes) {
        Map<ClothType, List<Cloth>> clothesByType = groupByType(clothes);

        boolean hasTopAndBottom =
                !clothesByType.getOrDefault(ClothType.TOP, List.of()).isEmpty() &&
                        !clothesByType.getOrDefault(ClothType.BOTTOM, List.of()).isEmpty();

        boolean hasDress = !clothesByType.getOrDefault(ClothType.DRESS, List.of()).isEmpty();

        return hasTopAndBottom || hasDress;
    }

    /**
     * 의상을 타입별로 그룹화
     */
    private Map<ClothType, List<Cloth>> groupByType(List<Cloth> clothes) {
        return clothes.stream()
                .collect(Collectors.groupingBy(
                        Cloth::getType,
                        () -> new EnumMap<>(ClothType.class),
                        Collectors.toList()
                ));
    }
}
