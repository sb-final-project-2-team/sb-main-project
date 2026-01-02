package com.codeit.closet.module.clothes.repository;

import com.codeit.closet.module.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, UUID> {

    // 삭제되지 않은 Clothes 단건 조회
    Optional<Clothes> findByIdAndDeletedAtIsNull(UUID id);

    // 소유자의 삭제되지 않은 Clothes 목록 조회
    List<Clothes> findAllByOwnerIdAndDeletedAtIsNull(UUID ownerId);

    // 중복 검사 (같은 소유자의 같은 이름)
    boolean existsByOwnerIdAndNameAndDeletedAtIsNull(UUID ownerId, String name);
}
