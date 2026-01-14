package com.codeit.closet.module.cloth.repository;

import com.codeit.closet.module.cloth.entity.Cloth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClothRepository extends JpaRepository<Cloth, UUID> {

    // 소유자의 Cloth 목록 조회
    List<Cloth> findAllByOwner_Id(UUID ownerId);

    // 중복 검사 (같은 소유자의 같은 이름)
    boolean existsByOwner_IdAndName(UUID ownerId, String name);
}
