package com.codeit.closet.module.notification.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {
	@Id
	@GeneratedValue
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(nullable = false, updatable = false)
	private UUID receiverId;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationLevel level;

	@Column(nullable = false, updatable = false)
	private Instant created_at;
}
