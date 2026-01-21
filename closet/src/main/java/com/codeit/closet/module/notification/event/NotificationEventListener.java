package com.codeit.closet.module.notification.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.codeit.closet.module.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
	private final NotificationService notificationService;

	@EventListener
	public void handleNotification(NotifyUserEvent event) {

		if (event.rawContent() != null) {
			notificationService.createWithRawContent(
				event.receiverId(),
				event.template(),
				event.rawContent(),
				event.titleArgs()
			);
			return;
		}

		notificationService.createWithRenderContent(
			event.receiverId(),
			event.template(),
			event.titleArgs(),
			event.contentArgs()
		);
	}
}
