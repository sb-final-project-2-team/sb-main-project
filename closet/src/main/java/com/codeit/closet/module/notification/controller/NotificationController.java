package com.codeit.closet.module.notification.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public ResponseEntity<List<NotificationDTO>> findAllByReceiverId(
		@AuthenticationPrincipal ClosetUserDetails principal) {
		UUID receiverId = principal.getUserDTO().id();
		log.info("알림 목록 조회 요청: receiverId={}", receiverId);
		List<NotificationDTO> notifications = notificationService.findAllByReceiverId(receiverId);
		log.debug("알림 목록 조회 응답: count={}", notifications.size());
		return ResponseEntity.ok(notifications);
	}

	@DeleteMapping("/{notificationId}")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal ClosetUserDetails principal,
		@PathVariable UUID notificationId) {
		UUID receiverId = principal.getUserDTO().id();
		log.info("알림 삭제 요청: id={}, receiverId={}", notificationId, receiverId);
		notificationService.delete(notificationId, receiverId);
		log.debug("알림 삭제 응답: id={}", notificationId);
		return ResponseEntity.noContent().build();
	}
}
