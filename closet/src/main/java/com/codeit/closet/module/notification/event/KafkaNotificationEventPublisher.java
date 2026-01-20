package com.codeit.closet.module.notification.event;

import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
		).whenComplete((result, ex) ->{
			if (ex == null) {
				log.info("[Kafka] 전송 성공 (topic={}, offset={})",
					result.getRecordMetadata().topic(),
					result.getRecordMetadata().offset());
			} else {
				log.error("[Kafka] 전송 실패 (receiverId={})",
					notificationKey,ex);
			}
		});

	}

	private String toKey(UUID receiverId) {
		return receiverId != null ? receiverId.toString() : "unknown";
	}
}
