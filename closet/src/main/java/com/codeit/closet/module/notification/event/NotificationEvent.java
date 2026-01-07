package com.codeit.closet.module.notification.event;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent (
	UUID id,
	UUID receiverId,
	String title,
	String content,
	String level,
	Instant createdAt
){}
