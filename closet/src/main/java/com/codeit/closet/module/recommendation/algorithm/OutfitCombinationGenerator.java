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
 * - 우선순위 1: (TOP + BOTTOM) 조합
 * - 우선순위 2: DRESS 단독/조합
 * - 우선순위 3: 단일 의류 조합 (모든 타입 허용)
 * - 선택: OUTER (온도에 따라 필수화)
 */
@Component
public class OutfitCombinationGenerator {

    /**
     * 의상 목록으로 코디 조합 생성
     * @param clothes 사용자의 의상 목록
     * @param outerRequired 아우터 필수 여부 (true면 아우터 없는 조합은 생성하지 않음)
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

        List<Cloth> tops = clothesByType.getOrDefault(ClothType.TOP, List.of());
        List<Cloth> bottoms = clothesByType.getOrDefault(ClothType.BOTTOM, List.of());
        List<Cloth> outers = clothesByType.getOrDefault(ClothType.OUTER, List.of());
        List<Cloth> dresses = clothesByType.getOrDefault(ClothType.DRESS, List.of());

        // 아우터 필수인데 아우터가 없으면 조합 생성 불가
        if (outerRequired && outers.isEmpty()) {
            return combinations;
        }

        // 1. TOP + BOTTOM 조합 생성
        for (Cloth top : tops) {
            for (Cloth bottom : bottoms) {
                if (combinations.size() >= maxCombinations) break;

                List<Cloth> baseOutfit = List.of(top, bottom);
                addOutfitCombinations(combinations, baseOutfit, outers, outerRequired, maxCombinations);
            }
        }

        // 2. DRESS 단독 조합 생성
        for (Cloth dress : dresses) {
            if (combinations.size() >= maxCombinations) break;

            List<Cloth> baseOutfit = List.of(dress);
            addOutfitCombinations(combinations, baseOutfit, outers, outerRequired, maxCombinations);
        }

        // 3. 완전한 조합이 없으면 단일 의류 조합 생성 (폴백)
        if (combinations.isEmpty()) {
            generateSingleClothCombinations(clothes, outers, outerRequired, combinations, maxCombinations);
        }

        return combinations;
    }

    /**
     * 단일 의류로 조합 생성 (폴백 로직)
     * - 완전한 조합(TOP+BOTTOM, DRESS)이 없을 때 사용
     * - 모든 의류 타입을 단일 조합으로 추가
     */
    private void generateSingleClothCombinations(
            List<Cloth> clothes,
            List<Cloth> outers,
            boolean outerRequired,
            List<List<Cloth>> combinations,
            int maxCombinations) {

        for (Cloth cloth : clothes) {
            if (combinations.size() >= maxCombinations) break;

            // OUTER 타입은 단독 추가
            if (cloth.getType() == ClothType.OUTER) {
                combinations.add(List.of(cloth));
                continue;
            }

            // 아우터 필수인데 아우터가 없으면 건너뛰기
            if (outerRequired && outers.isEmpty()) {
                continue;
            }

            if (outerRequired) {
                // 아우터 필수: 의류 + 아우터 조합
                for (Cloth outer : outers) {
                    if (combinations.size() >= maxCombinations) break;
                    List<Cloth> outfit = new ArrayList<>();
                    outfit.add(cloth);
                    outfit.add(outer);
                    combinations.add(outfit);
                }
            } else if (!outers.isEmpty()) {
                // 아우터 선택적 + 아우터 있음: 단독과 아우터 포함 조합 모두
                combinations.add(List.of(cloth));
                if (combinations.size() < maxCombinations) {
                    for (Cloth outer : outers) {
                        if (combinations.size() >= maxCombinations) break;
                        List<Cloth> outfit = new ArrayList<>();
                        outfit.add(cloth);
                        outfit.add(outer);
                        combinations.add(outfit);
                    }
                }
            } else {
                // 아우터 없음 + 필수 아님: 단독 조합
                combinations.add(List.of(cloth));
            }
        }
    }

    /**
     * 기본 조합에 아우터를 추가하여 조합 목록에 추가
     */
    private void addOutfitCombinations(
            List<List<Cloth>> combinations,
            List<Cloth> baseOutfit,
            List<Cloth> outers,
            boolean outerRequired,
            int maxCombinations) {

        if (outerRequired) {
            // 아우터 필수: 아우터 포함 조합만 추가
            for (Cloth outer : outers) {
                if (combinations.size() >= maxCombinations) break;
                List<Cloth> outfitWithOuter = new ArrayList<>(baseOutfit);
                outfitWithOuter.add(outer);
                combinations.add(outfitWithOuter);
            }
        } else if (!outers.isEmpty()) {
            // 아우터 선택적 + 아우터 있음: 기본 조합과 아우터 포함 조합 모두 추가
            if (combinations.size() < maxCombinations) {
                combinations.add(new ArrayList<>(baseOutfit));
            }
            for (Cloth outer : outers) {
                if (combinations.size() >= maxCombinations) break;
                List<Cloth> outfitWithOuter = new ArrayList<>(baseOutfit);
                outfitWithOuter.add(outer);
                combinations.add(outfitWithOuter);
            }
        } else {
            // 아우터 없음 + 필수 아님: 기본 조합만 추가
            if (combinations.size() < maxCombinations) {
                combinations.add(new ArrayList<>(baseOutfit));
            }
        }
    }

    /**
     * 유효한 코디 조합인지 검증
     * - 1개 이상의 의류가 있으면 유효
     */
    public boolean isValidCombination(List<Cloth> outfit) {
        return outfit != null && !outfit.isEmpty();
    }

    /**
     * 추천 가능한 코디 조합을 만들 수 있는지 확인
     * - 1개 이상의 의류가 있으면 추천 가능
     */
    public boolean canGenerateCombinations(List<Cloth> clothes) {
        return clothes != null && !clothes.isEmpty();
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
