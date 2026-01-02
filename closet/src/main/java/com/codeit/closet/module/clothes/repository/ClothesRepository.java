package com.codeit.closet.module.clothes.repository;

import com.codeit.closet.module.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, UUID> {

    // 소유자의 Clothes 목록 조회
    List<Clothes> findAllByOwnerId(UUID ownerId);

    // 중복 검사 (같은 소유자의 같은 이름)
    boolean existsByOwnerIdAndName(UUID ownerId, String name);
}
