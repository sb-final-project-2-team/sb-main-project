package com.codeit.closet.module.notification.event;

import java.time.Instant;
import java.util.UUID;

import com.codeit.closet.module.notification.entity.NotificationLevel;

public record NotificationEvent (
	UUID id,
	UUID receiverId,
	String title,
	String content,
	NotificationLevel level,
	Instant createdAt
){}
