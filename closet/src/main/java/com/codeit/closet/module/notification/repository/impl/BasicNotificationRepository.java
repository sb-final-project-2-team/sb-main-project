package com.codeit.closet.module.notification.repository.impl;

import static com.codeit.closet.module.notification.entity.QNotification.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.codeit.closet.module.notification.entity.Notification;
import com.codeit.closet.module.notification.repository.NotificationQueryRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BasicNotificationRepository implements NotificationQueryRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Notification> findByCursor(
		UUID receiverId,
		Instant cursorCreatedAt,
		UUID idAfter,
		int limit
	) {
		return queryFactory
			.selectFrom(notification)
			.where(
				notification.receiverId.eq(receiverId),
				cursorCondition(cursorCreatedAt, idAfter)
			)
			.orderBy(
				notification.createdAt.desc(),
				notification.id.desc()
			)
			.limit(limit + 1)
			.fetch();
	}

	private BooleanExpression cursorCondition(Instant createdAt, UUID idAfter) {
		if (createdAt == null || idAfter == null) {
			return null;
		}

		return notification.createdAt.lt(createdAt)
			.or(
				notification.createdAt.eq(createdAt)
					.and(notification.id.lt(idAfter))
			);
	}
}
