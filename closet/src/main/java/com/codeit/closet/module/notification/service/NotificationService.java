package com.codeit.closet.module.notification.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;


import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.template.NotificationTemplate;

public interface NotificationService {

	List<NotificationDTO> findAllByReceiverId(UUID receiverId);

	void deleteNotification(UUID id, UUID receiverId);

	// (팔로우, 좋아요, 댓글, 권한변경, DM)
	void create(UUID receiverId, String title, String content);

	// (급격한 날씨 변화 알림)
	void createMany(Set<UUID> receiverIds, String title, String content);

	// 템플릿 전용 오버로드

	/**
	 * 제목만 템플릿을 쓰고, 본문은 외부에서 받은 원문(Raw)을 그대로 사용
	 * (DM,  Like, Comment, Feed)
	 */
	default void createWithRawContent(UUID receiverId, NotificationTemplate template, String rawContent, Object... titleArgs) {
		create(receiverId, template.renderTitle(titleArgs), rawContent);
	}

	/**
	 * 제목과 본문 모두 템플릿을 사용하여 렌더링
	 * (Role, Attribute, Weather)
	 */
	default void createWithRenderContent(UUID receiverId, NotificationTemplate template,Object[] titleArgs, Object... contentArgs) {
		create(receiverId,template.renderTitle(), template.renderContent(contentArgs));
	}
}