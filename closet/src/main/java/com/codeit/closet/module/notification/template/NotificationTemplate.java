package com.codeit.closet.module.notification.template;

import java.util.regex.Matcher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTemplate {

	// front: icSend
	DM_RECEIVED(
		"[DM] {}", // 송신자 이름이 들어가야함
		null
	),

	// front: icHeart
	LIKE(
		"{}님이 내 피드를 좋아합니다", // 좋아요를 누른 사용자의 이름
		"{}님이 회원님의 피드를 좋아합니다."
	),

	// front: icMessage
	COMMENT(
		"{}님이 댓글을 달았어요.",
		null
	),

	// front: icChangeUser
	ROLE_CHANGED(
		// 권한 변경 USER -> ADMIN이라면
		//"내 권한이 [USER]에서 [ADMIN]으로 변경되었어요로 표기
		"내 권한이 변경되었어요.",
		"내 권한이 [{}]에서 [{}]로 변경되었어요."
	),

	ATTRIBUTE_ADD(
		"의상 속성이 추가되었어요.",
		"[{}] 속성을 확인해보세요." // 속성명이 들어가야함
	),

	// front: icSettingPlus
	ATTRIBUTE_CHANGED(
		"의상 속성이 변경되었어요.",
		"[{}] 속성을 확인해보세요." // 속성명이 들어가야함
	),

	// front: icHanger
	FEED(
		"{}님이 새로운 피드를 작성했어요.",
		"{}님이 따끈따끈한 소식을 전해왔어요!"
	),

	FOLLOWED(
		"{}님이 회원님을 팔로우했습니다.",
		"{}님의 피드를 확인해보세요"
	),

	// front: 매핑 조건이 없음 default = icMessage
	WEATHER_ALERT(
		"급격한 날씨 변화가 관측되었어요.",
		"기온이 급격히 변했습니다. 외출 시 주의하세요." // 경우의 수를 enum으로 표기할건지 아니면 단일 방식으로 할건지
	);

	private final String titleTemplate;
	private final String contentTemplate;

	public String renderTitle(Object... args) {
		return render(titleTemplate, args);
	}

	public String renderContent(Object... args) {
		if (contentTemplate == null) {
			throw new IllegalStateException("해당 알림 타입은 템플릿 기반 렌더링을 지원하지 않습니다." + name());
		}
		return render(contentTemplate, args);
	}

	// 공통 치환 함수
	private String render(String template, Object... args) {
		if (template == null ) {
			return null;
		}
		if (args == null || args.length == 0) {
			return template;
		}

		String result = template;
		for (Object arg : args) {
			result = result.replaceFirst("\\{}", Matcher.quoteReplacement(String.valueOf(arg)));
		}
		return result;
	}

}
