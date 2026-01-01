package com.codeit.closet.module.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.codeit.closet.module.notification.entity.NotificationLevel;

public record NotificationDto (
	UUID id,
	UUID receiverId,
	String title,
	String content,
	NotificationLevel level,
	Instant createdAt
){}
