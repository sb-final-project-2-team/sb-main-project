package com.codeit.closet.module.notification.service;

import java.util.UUID;

import com.codeit.closet.module.notification.dto.NotificationDTOCursorResponse;

public interface NotificationQueryService {
	NotificationDTOCursorResponse findByReceiverId(
		UUID receiverId,
		String cursor,
		UUID idAfter,
		int limit
	);
}
