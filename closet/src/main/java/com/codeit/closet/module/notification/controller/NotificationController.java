package com.codeit.closet.module.notification.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.dto.NotificationDTOCursorResponse;
import com.codeit.closet.module.notification.service.NotificationQueryService;
import com.codeit.closet.module.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
	private final NotificationQueryService notificationQueryService;
	private final NotificationService notificationService;

	@GetMapping
	public ResponseEntity<NotificationDTOCursorResponse> findNotifications(
		@AuthenticationPrincipal ClosetUserDetails principal,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit
	) {
		UUID receiverId = principal.getUserDTO().id();

		log.info(
			"알림 목록 조회 요청: receiverId={}, cursor={}, idAfter={}, limit={}",
			receiverId, cursor, idAfter, limit
		);

		return ResponseEntity.ok(
			notificationQueryService.findByReceiverId(
				receiverId,
				cursor,
				idAfter,
				limit
			)
		);
	}


	@DeleteMapping("/{notificationId}")
	public ResponseEntity<Void> deleteNotification(
		@AuthenticationPrincipal ClosetUserDetails principal,
		@PathVariable UUID notificationId
	) {
		UUID receiverId = principal.getUserDTO().id();

		log.info(
			"알림 삭제 요청: notificationId={}, receiverId={}",
			notificationId, receiverId
		);

		notificationService.deleteNotification(notificationId, receiverId);

		return ResponseEntity.noContent().build();
	}
}
