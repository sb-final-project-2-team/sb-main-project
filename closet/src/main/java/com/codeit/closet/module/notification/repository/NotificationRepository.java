package com.codeit.closet.module.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codeit.closet.module.notification.entity.Notification;

@Repository
public interface NotificationRepository
	extends JpaRepository<Notification, UUID>, NotificationQueryRepository {

	long deleteByIdAndReceiverId(UUID id, UUID receiverId);

	long countByReceiverId(UUID receiverId);

}
