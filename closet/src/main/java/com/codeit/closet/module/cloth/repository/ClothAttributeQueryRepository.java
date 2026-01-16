package com.codeit.closet.module.cloth.repository;

import com.codeit.closet.module.cloth.dto.ClothAttributeValueDTO;

import java.util.List;
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

    /**
     * 여러 의상 ID로 속성명-값 맵 일괄 조회 (N+1 방지)
     * @param clothIds 의상 ID 목록
     * @return 의상ID -> (속성명 -> 속성값) 맵
     */
    Map<UUID, Map<String, String>> findAttributeMapsByClothIds(List<UUID> clothIds);

    /**
     * 여러 의상 ID로 ClothAttributeValueDTO 리스트 일괄 조회 (DTO 변환용)
     * @param clothIds 의상 ID 목록
     * @return 의상ID -> ClothAttributeValueDTO 리스트 맵
     */
    Map<UUID, List<ClothAttributeValueDTO>> findAttributeDTOsByClothIds(List<UUID> clothIds);
}
