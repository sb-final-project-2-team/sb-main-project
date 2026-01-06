package com.codeit.closet.module.cloth.repository;

import com.codeit.closet.module.cloth.entity.ClothAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClothAttributeValueRepository extends JpaRepository<ClothAttributeValue, UUID> {

    // 특정 옷의 속성 값 목록 조회
    List<ClothAttributeValue> findAllByClothId(UUID clothId);

    // 특정 옷의 속성 값 삭제
    @Modifying
    @Transactional
    void deleteAllByClothId(UUID clothId);
}
