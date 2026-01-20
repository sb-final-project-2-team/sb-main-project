package com.codeit.closet.module.notification.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationMapper notificationMapper;
	private final NotificationEventPublisher eventPublisher;

	@Override
	@Transactional
	public void deleteNotification(UUID id, UUID receiverId) {
		log.info("[알림] 읽음 처리 요청 id={},receiverId={}", id, receiverId);

		long deleted = notificationRepository.deleteByIdAndReceiverId(id, receiverId);

		if (deleted == 0){
			throw new IllegalArgumentException("알림을 찾을 수 없거나 권한이 없습니다.");
		}

		log.info("[알림] 읽음 처리 완료 id={}", id);

	}

	@Override
	@Transactional
	public void createNotification(UUID receiverId, String title, String content) {

		// 단일 알림 생성
		Notification notification = Notification.builder()
			.receiverId(receiverId)
			.title(title)
			.content(content)
			.level(NotificationLevel.INFO)
			.createdAt(Instant.now())
			.build();

		Notification saved = notificationRepository.save(notification);

		publishEvent(saved);

		log.info("[Notification] 단건 알림 생성 완료(id={} -> receiverId={})", saved.getId(), receiverId);
	}

	@Override
	@Transactional
	public void createManyNotification(Set<UUID> receiverIds, String title, String content) {
		// 입력 검증
		if (receiverIds == null || receiverIds.isEmpty()) {
			throw new IllegalArgumentException("receiverIds는 필수입니다.");
		}
		if (receiverIds.contains(null)) {
			throw new IllegalArgumentException("receiverIds에 null이 포함될 수 없습니다.");
		}
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("title은 필수입니다.");
		}
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("content는 필수 입니다.");
		}

		List<Notification> notifications = new ArrayList<>(receiverIds.size());

		for (UUID receiverId : receiverIds) {
			notifications.add(
				Notification.builder()
					.receiverId(receiverId)
					.title(title)
					.content(content)
					.level(NotificationLevel.INFO)
					.createdAt(Instant.now())
					.build()
			);
		}

		List<Notification> savedList = notificationRepository.saveAll(notifications);
		log.info("[Notification] 다건 알림 생성 완료 (count={}", savedList.size());

		publishManyAfterCommit(savedList);
	}

	private void publishEvent(Notification notification) {
		NotificationEvent event = new NotificationEvent(
			notification.getId(),
			notification.getReceiverId(),
			notification.getTitle(),
			notification.getContent(),
			notification.getLevel(),
			notification.getCreatedAt()
		);

		eventPublisher.publish(event);
	}

		// 이벤트 발행은 별도로 처리
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishManyAfterCommit(List<Notification> savedList) {

		for (Notification notification : savedList) {
			try {
				publishEvent(notification);
			} catch (Exception e) {
			    log.error("[Notification] 이벤트 발행 실패 (id={}", notification.getId(), e); // 부분 실패 처리 전략: 로그만 기록하고 계속 진행
			}
		}
	}

}
