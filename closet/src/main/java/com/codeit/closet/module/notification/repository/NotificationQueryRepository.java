package com.codeit.closet.module.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.codeit.closet.module.notification.entity.Notification;

public interface NotificationQueryRepository {
	List<Notification> findByCursor(
		UUID receiverId,
		Instant cursorCreatedAt,
		UUID idAfter,
		int limit
	);
}
