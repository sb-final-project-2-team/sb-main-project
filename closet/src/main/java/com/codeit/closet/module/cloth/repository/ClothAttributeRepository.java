package com.codeit.closet.module.cloth.repository;

import com.codeit.closet.module.cloth.entity.ClothAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClothAttributeRepository extends JpaRepository<ClothAttribute, UUID> {

    // 중복 검사 (속성 이름)
    boolean existsByName(String name);
}
