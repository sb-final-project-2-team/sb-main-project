package com.codeit.closet.module.notification.event;

import java.util.UUID;

import com.codeit.closet.module.notification.template.NotificationTemplate;

public record NotifyUserEvent(
	UUID receiverId,
	NotificationTemplate template,
	String rawContent,
	Object[] titleArgs,
	Object[] contentArgs
) {
}
