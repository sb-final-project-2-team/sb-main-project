package com.codeit.closet.module.notification.event;

import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNotificationEventPublisher implements NotificationEventPublisher {
	private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

	@Override
	public void publish(NotificationEvent event) {

		String notificationKey = toKey(event.receiverId()); // 수신자 기준으로 지정

		kafkaTemplate.send(
			NotificationTopics.NOTIFICATION_CREATED,
			notificationKey,
			event
		);

		log.info(
			"[Kafka] 알림 생성 이벤트 발행 완료 (receiverId={}, notificationId={})",
			notificationKey,
			event.id()
		);
	}

	private String toKey(UUID receiverId) {
		return receiverId != null ? receiverId.toString() : "unknown";
	}
}
