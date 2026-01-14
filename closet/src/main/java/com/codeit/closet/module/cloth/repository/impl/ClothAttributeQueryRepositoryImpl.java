package com.codeit.closet.module.cloth.repository.impl;

import com.codeit.closet.module.cloth.repository.ClothAttributeQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
