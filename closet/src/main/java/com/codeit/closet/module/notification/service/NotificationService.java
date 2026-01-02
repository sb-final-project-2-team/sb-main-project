package com.codeit.closet.module.notification.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.codeit.closet.module.notification.dto.NotificationDto;

public interface NotificationService {

	List<NotificationDto> findAllByReceiverId(UUID receiverId);

	void delete(UUID id, UUID receiverId);

	void create(Set<UUID> receiverIds, String title, String content);
}
