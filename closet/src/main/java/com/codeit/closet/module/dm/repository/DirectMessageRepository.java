package com.codeit.closet.module.dm.repository;

import com.codeit.closet.module.dm.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID>, DirectMessageQueryRepository {
}