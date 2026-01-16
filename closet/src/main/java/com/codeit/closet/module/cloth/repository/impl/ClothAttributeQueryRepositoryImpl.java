package com.codeit.closet.module.cloth.repository.impl;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;
import com.codeit.closet.module.cloth.repository.ClothAttributeQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 의상 속성 조회용 QueryRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ClothAttributeQueryRepositoryImpl implements ClothAttributeQueryRepository {

    private final EntityManager entityManager;

    /**
     * 의상 ID로 속성명-값 맵 조회
     * ClothAttributeValue.clothAttributeId → ClothAttribute.name 조인
     */
    @Override
    public Map<String, String> findAttributeMapByClothId(UUID clothId) {
        // JPQL로 ClothAttributeValue와 ClothAttribute 조인
        String jpql = """
            SELECT ca.name, cav.value
            FROM ClothAttributeValue cav
            JOIN ClothAttribute ca ON cav.clothAttributeId = ca.id
            WHERE cav.clothId = :clothId
            """;

        List<Tuple> results = entityManager.createQuery(jpql, Tuple.class)
                .setParameter("clothId", clothId)
                .getResultList();

        Map<String, String> attributeMap = new HashMap<>();
        for (Tuple tuple : results) {
            String name = tuple.get(0, String.class);
            String value = tuple.get(1, String.class);
            attributeMap.put(name, value);
        }

        return attributeMap;
    }

    /**
     * 여러 의상 ID로 속성명-값 맵 일괄 조회 (N+1 방지)
     */
    @Override
    public Map<UUID, Map<String, String>> findAttributeMapsByClothIds(List<UUID> clothIds) {
        if (clothIds == null || clothIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT cav.cloth.id, ca.name, cav.value
            FROM ClothAttributeValue cav
            JOIN cav.clothAttribute ca
            WHERE cav.cloth.id IN :clothIds
            """;

        List<Tuple> results = entityManager.createQuery(jpql, Tuple.class)
                .setParameter("clothIds", clothIds)
                .getResultList();

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
     */
    @Override
    public Map<UUID, List<ClothAttributeValueDTO>> findAttributeDTOsByClothIds(List<UUID> clothIds) {
        if (clothIds == null || clothIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT cav.cloth.id, ca.id, cav.value
            FROM ClothAttributeValue cav
            JOIN cav.clothAttribute ca
            WHERE cav.cloth.id IN :clothIds
            """;

        List<Tuple> results = entityManager.createQuery(jpql, Tuple.class)
                .setParameter("clothIds", clothIds)
                .getResultList();

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
