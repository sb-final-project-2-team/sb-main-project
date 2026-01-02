package com.codeit.closet.module.dm.repository;

import com.codeit.closet.module.dm.entity.DirectMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    List<DirectMessage> findByDmKeyOrderByIdDesc(String dmKey, Pageable pageable);

    // 커서 조회: id < nextIdAfter 인 과거 메시지들
    List<DirectMessage> findByDmKeyAndIdLessThanOrderByIdDesc(String dmKey, UUID nextIdAfter, Pageable pageable);

    long countByDmKey(String dmKey);
}
