package com.codeit.closet.module.notification.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;


import com.codeit.closet.module.notification.dto.NotificationDTO;


public interface NotificationService {

	List<NotificationDTO> findAllByReceiverId(UUID receiverId);

	void deleteNotification(UUID id, UUID receiverId);

	// (팔로우, 좋아요, 댓글, 권한변경, DM)
	void create(UUID receiverId, String title, String content);

	// (급격한 날씨 변화 알림)
	void createMany(Set<UUID> receiverIds, String title, String content);

}