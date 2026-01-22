package com.codeit.closet.module.notification.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.dto.NotificationDTOCursorResponse;
import com.codeit.closet.module.notification.entity.Notification;
import com.codeit.closet.module.notification.mapper.NotificationMapper;
import com.codeit.closet.module.notification.repository.NotificationRepository;
import com.codeit.closet.module.notification.service.NotificationQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationQueryService implements NotificationQueryService {
	private final NotificationRepository notificationRepository;
	private final NotificationMapper notificationMapper;

	private record Cursor(Instant createdAt) {}

	@Override
	public NotificationDTOCursorResponse findByReceiverId(
		UUID receiverId,
		String cursor,
		UUID idAfter,
		int limit
	) {
		Cursor parsed = parseCursor(cursor);

		List<Notification> fetched = notificationRepository.findByCursor(
			receiverId,
			parsed.createdAt(),
			idAfter,
			limit
		);

		boolean hasNext = fetched.size() > limit;
		if (hasNext) {
			fetched = fetched.subList(0, limit);
		}

		List<NotificationDTO> data = fetched.stream()
			.map(notificationMapper::toDto)
			.toList();

		String nextCursor = null;
		UUID nextIdAfter = null;

		if (hasNext && !fetched.isEmpty()) {
			Notification last = fetched.get(fetched.size() - 1);
			nextCursor = encodeCursor(last.getCreatedAt());
			nextIdAfter = last.getId();
		}

		return new NotificationDTOCursorResponse(
			data,
			nextCursor,
			nextIdAfter,
			hasNext,
			notificationRepository.countByReceiverId(receiverId),
			"createdAt",
			"DESCENDING"
		);
	}

	private Cursor parseCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return new Cursor(null);
		}

		try {
			String decoded = new String(Base64.getUrlDecoder().decode(cursor));
			return new Cursor(Instant.parse(decoded));
		} catch (Exception e) {
			log.warn("[알림] 잘못된 cursor 형식: {}", cursor);
		    return new Cursor(null);
		}
	}

	private String encodeCursor(Instant createdAt) {
		return Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(createdAt.toString().getBytes());
	}
}
