package com.codeit.closet.module.cloth.repository;

import java.util.Map;
import java.util.UUID;

/**
 * 의상 속성 조회용 QueryRepository
 * ClothAttributeValue + ClothAttribute 조인하여 속성명-값 맵 반환
 */
public interface ClothAttributeQueryRepository {

    /**
     * 의상 ID로 속성명-값 맵 조회
     * @param clothId 의상 ID
     * @return 속성명 -> 속성값 맵 (예: {"계절": "겨울", "소재": "울"})
     */
    Map<String, String> findAttributeMapByClothId(UUID clothId);
}
