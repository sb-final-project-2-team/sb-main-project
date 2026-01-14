package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 추천 기능 전용 속성 배치 로더
 * N+1 문제 방지를 위해 여러 의상의 속성을 한 번에 조회
 */
@Component
@RequiredArgsConstructor
public class RecommendationAttributeLoader {

    private final EntityManager entityManager;

    /**
     * 여러 의상 ID로 속성명-값 맵 일괄 조회
     * @param clothIds 의상 ID 목록
     * @return 의상ID -> (속성명 -> 속성값) 맵
     */
    public Map<UUID, Map<String, String>> loadAttributeMaps(List<UUID> clothIds) {
        if (clothIds == null || clothIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // JPQL로 여러 clothId에 대해 한 번에 조회
        String jpql = """
            SELECT cav.cloth.id, ca.name, cav.value
            FROM ClothAttributeValue cav
            JOIN cav.clothAttribute ca
            WHERE cav.cloth.id IN :clothIds
            """;

        List<Tuple> results = entityManager.createQuery(jpql, Tuple.class)
                .setParameter("clothIds", clothIds)
                .getResultList();

        // 결과를 clothId별로 그룹화
        Map<UUID, Map<String, String>> attributeMaps = new HashMap<>();
        for (Tuple tuple : results) {
            UUID clothId = tuple.get(0, UUID.class);
            String name = tuple.get(1, String.class);
            String value = tuple.get(2, String.class);

            attributeMaps
                    .computeIfAbsent(clothId, k -> new HashMap<>())
                    .put(name, value);
        }

        return attributeMaps;
    }

    /**
     * 여러 의상 ID로 ClothAttributeValueDTO 리스트 일괄 조회 (DTO 변환용)
     * @param clothIds 의상 ID 목록
     * @return 의상ID -> ClothAttributeValueDTO 리스트 맵
     */
    public Map<UUID, List<ClothAttributeValueDTO>> loadAttributeDTOs(List<UUID> clothIds) {
        if (clothIds == null || clothIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // JPQL로 definitionId 포함하여 조회
        String jpql = """
            SELECT cav.cloth.id, ca.id, cav.value
            FROM ClothAttributeValue cav
            JOIN cav.clothAttribute ca
            WHERE cav.cloth.id IN :clothIds
            """;

        List<Tuple> results = entityManager.createQuery(jpql, Tuple.class)
                .setParameter("clothIds", clothIds)
                .getResultList();

        // 결과를 clothId별 ClothAttributeValueDTO 리스트로 그룹화
        Map<UUID, List<ClothAttributeValueDTO>> attributeDTOs = new HashMap<>();
        for (Tuple tuple : results) {
            UUID clothId = tuple.get(0, UUID.class);
            UUID definitionId = tuple.get(1, UUID.class);
            String value = tuple.get(2, String.class);

            attributeDTOs
                    .computeIfAbsent(clothId, k -> new ArrayList<>())
                    .add(new ClothAttributeValueDTO(definitionId, value));
        }

        return attributeDTOs;
    }
}
