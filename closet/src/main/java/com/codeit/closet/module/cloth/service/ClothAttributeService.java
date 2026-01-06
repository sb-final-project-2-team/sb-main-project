package com.codeit.closet.module.cloth.service;

import com.codeit.closet.module.cloth.dto.ClothAttributeCreateRequest;
import com.codeit.closet.module.cloth.dto.ClothAttributeDto;
import com.codeit.closet.module.cloth.dto.ClothAttributeUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ClothAttributeService {

    /**
     * 속성 정의 생성
     */
    ClothAttributeDto create(ClothAttributeCreateRequest request);

    /**
     * 속성 정의 단건 조회
     */
    ClothAttributeDto find(UUID attributeId);

    /**
     * 속성 정의 전체 목록 조회
     */
    List<ClothAttributeDto> findAll();

    /**
     * 속성 정의 수정
     */
    ClothAttributeDto update(UUID attributeId, ClothAttributeUpdateRequest request);

    /**
     * 속성 정의 삭제
     */
    void delete(UUID attributeId);
}
