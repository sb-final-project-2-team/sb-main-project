package com.codeit.closet.module.notification.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.entity.Notification;
import com.codeit.closet.module.notification.entity.NotificationLevel;
import com.codeit.closet.module.notification.event.NotificationEvent;
import com.codeit.closet.module.notification.event.NotificationEventPublisher;
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
	private final NotificationEventPublisher eventPublisher;

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
	public void deleteNotification(UUID id, UUID receiverId) {
		log.debug("알림 삭제 시작: notificationId={}, receiverId={}", id, receiverId);

		Notification notification = notificationRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다." + id));

		if (!notification.getReceiverId().equals(receiverId)) {
			throw new IllegalArgumentException("권한이 없습니다.: 수신자가 일치하지 않습니다.");
		}

		notificationRepository.delete(notification);

		log.debug("알림 삭제 완료: id={}", id);

	}

	@Override
	@Transactional
	public void create(UUID receiverId, String title, String content) {

		// 단일 알림 생성
		Notification notification = Notification.builder()
			.receiverId(receiverId)
			.title(title)
			.content(content)
			.build();

		Notification saved = notificationRepository.save(notification);

		NotificationEvent event = new NotificationEvent(
			saved.getId(),
			saved.getReceiverId(),
			saved.getTitle(),
			saved.getContent(),
			NotificationLevel.INFO,
			getCreatedAt(saved)
		);

		eventPublisher.publish(event);

		log.info("[Notification] 단건 알림 생성 완료(id={} -> receiverId={})", saved.getId(), receiverId);
	}

	@Override
	@Transactional
	public void createMany(Set<UUID> receiverIds, String title, String content) {

		List<Notification> notifications = new ArrayList<>(receiverIds.size());

		for (UUID receiverId : receiverIds) {
			notifications.add(
				Notification.builder()
					.receiverId(receiverId)
					.title(title)
					.content(content)
					.build()
			);
		}

		List<Notification> savedList = notificationRepository.saveAll(notifications);

		for (Notification saved : savedList) {
			NotificationEvent event = new NotificationEvent(
				saved.getId(),
				saved.getReceiverId(),
				saved.getTitle(),
				saved.getContent(),
				NotificationLevel.INFO,
				getCreatedAt(saved)
			);
			eventPublisher.publish(event);
		}

		log.info("[Notification] 다건 알림 생성 완료 (count={})", savedList.size());
	}

	private Instant getCreatedAt(Notification notification) {
		if (notification.getCreatedAt() != null) {
			return notification.getCreatedAt();
		}
		return Instant.now();
	}

}
