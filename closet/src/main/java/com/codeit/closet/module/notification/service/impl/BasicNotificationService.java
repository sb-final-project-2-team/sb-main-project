package com.codeit.closet.module.notification.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.entity.Notification;
import com.codeit.closet.module.notification.mapper.NotificationMapper;
import com.codeit.closet.module.notification.repository.NotificationRepository;
import com.codeit.closet.module.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationMapper notificationMapper;

	@Override
	@Transactional(readOnly = true)
	public List<NotificationDTO> findAllByReceiverId(UUID receiverId) {
		log.debug("알림 목록 조회 시작: receiverId={}", receiverId);

		List<NotificationDTO> result = notificationRepository
			.findAllByReceiverIdOrderByCreatedAtDesc(receiverId)
			.stream()
			.map(notificationMapper::toDto)
			.toList();

		log.debug("알림 목록 조회 완료: receiverId={} count={}", receiverId, result.size());
		return result;
	}

	@Override
	@Transactional
	public void delete(UUID id, UUID receiverId) {
		log.debug("알림 삭제 시작: notificationId={}, receiverId={}", id, receiverId);

		Notification notification = notificationRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Id를 찾을 수 없습니다." + id));

		if (!notification.getReceiverId().equals(receiverId)) {
			throw new IllegalArgumentException("권한이 없습니다.: 수신자가 일치하지 않습니다.");
		}

		notificationRepository.delete(notification);

		log.debug("알림 삭제 완료: id={}", id);

	}

}
