package com.codeit.closet.module.notification.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("NotificationTemplate 테스트")
class NotificationTemplateTest {

  @Test
  @DisplayName("DM 수신 템플릿 - 제목 렌더링")
  void dmReceived_RenderTitle() {
    // given
    String senderName = "홍길동";

    // when
    String title = NotificationTemplate.DM_RECEIVED.renderTitle(senderName);

    // then
    assertThat(title).isEqualTo("[DM] 홍길동");
  }

  @Test
  @DisplayName("좋아요 템플릿 - 제목 렌더링")
  void like_RenderTitle() {
    // given
    String userName = "김철수";

    // when
    String title = NotificationTemplate.LIKE.renderTitle(userName);

    // then
    assertThat(title).isEqualTo("김철수님이 내 피드를 좋아합니다");
  }

  @Test
  @DisplayName("댓글 템플릿 - 제목 렌더링")
  void comment_RenderTitle() {
    // given
    String commenterName = "이영희";

    // when
    String title = NotificationTemplate.COMMENT.renderTitle(commenterName);

    // then
    assertThat(title).isEqualTo("이영희님이 댓글을 달았어요.");
  }

  @Test
  @DisplayName("권한 변경 템플릿 - 제목과 내용 렌더링")
  void roleChanged_RenderTitleAndContent() {
    // when
    String title = NotificationTemplate.ROLE_CHANGED.renderTitle();
    String content = NotificationTemplate.ROLE_CHANGED.renderContent("USER", "ADMIN");

    // then
    assertThat(title).isEqualTo("내 권한이 변경되었어요.");
    assertThat(content).isEqualTo("내 권한이 [USER]에서 [ADMIN]로 변경되었어요.");
  }

  @Test
  @DisplayName("속성 추가 템플릿 - 제목과 내용 렌더링")
  void attributeAdd_RenderTitleAndContent() {
    // when
    String title = NotificationTemplate.ATTRIBUTE_ADD.renderTitle();
    String content = NotificationTemplate.ATTRIBUTE_ADD.renderContent("색상");

    // then
    assertThat(title).isEqualTo("의상 속성이 추가되었어요.");
    assertThat(content).isEqualTo("[색상] 속성을 확인해보세요.");
  }

  @Test
  @DisplayName("속성 변경 템플릿 - 제목과 내용 렌더링")
  void attributeChanged_RenderTitleAndContent() {
    // when
    String title = NotificationTemplate.ATTRIBUTE_CHANGED.renderTitle();
    String content = NotificationTemplate.ATTRIBUTE_CHANGED.renderContent("사이즈");

    // then
    assertThat(title).isEqualTo("의상 속성이 변경되었어요.");
    assertThat(content).isEqualTo("[사이즈] 속성을 확인해보세요.");
  }

  @Test
  @DisplayName("피드 템플릿 - 제목과 내용 렌더링")
  void feed_RenderTitleAndContent() {
    // given
    String authorName = "박민수";

    // when
    String title = NotificationTemplate.FEED.renderTitle(authorName);
    String content = NotificationTemplate.FEED.renderContent(authorName);

    // then
    assertThat(title).isEqualTo("박민수님이 새로운 피드를 작성했어요.");
    assertThat(content).isEqualTo("박민수님이 따끈따끈한 소식을 전해왔어요!");
  }

  @Test
  @DisplayName("팔로우 템플릿 - 제목과 내용 렌더링")
  void followed_RenderTitleAndContent() {
    // given
    String followerName = "정수진";

    // when
    String title = NotificationTemplate.FOLLOWED.renderTitle(followerName);
    String content = NotificationTemplate.FOLLOWED.renderContent(followerName);

    // then
    assertThat(title).isEqualTo("정수진님이 회원님을 팔로우했습니다.");
    assertThat(content).isEqualTo("정수진님의 피드를 확인해보세요");
  }

  @Test
  @DisplayName("날씨 경고 템플릿 - 제목과 내용 렌더링")
  void weatherAlert_RenderTitleAndContent() {
    // when
    String title = NotificationTemplate.WEATHER_ALERT.renderTitle();
    String content = NotificationTemplate.WEATHER_ALERT.renderContent();

    // then
    assertThat(title).isEqualTo("급격한 날씨 변화가 관측되었어요.");
    assertThat(content).isEqualTo("기온이 급격히 변했습니다. 외출 시 주의하세요.");
  }

  @Test
  @DisplayName("인자 없이 제목 렌더링")
  void renderTitle_WithoutArgs() {
    // when
    String title = NotificationTemplate.WEATHER_ALERT.renderTitle();

    // then
    assertThat(title).isEqualTo("급격한 날씨 변화가 관측되었어요.");
  }

  @Test
  @DisplayName("여러 인자로 제목 렌더링")
  void renderTitle_WithMultipleArgs() {
    // when
    String content = NotificationTemplate.ROLE_CHANGED.renderContent("USER", "ADMIN");

    // then
    assertThat(content).contains("USER");
    assertThat(content).contains("ADMIN");
  }

  @Test
  @DisplayName("DM 템플릿 - contentTemplate이 null이므로 renderContent 호출 시 예외")
  void dmReceived_RenderContent_ThrowsException() {
    // when & then
    assertThatThrownBy(() -> NotificationTemplate.DM_RECEIVED.renderContent())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("해당 알림 타입은 템플릿 기반 렌더링을 지원하지 않습니다");
  }

  @Test
  @DisplayName("댓글 템플릿 - contentTemplate이 null이므로 renderContent 호출 시 예외")
  void comment_RenderContent_ThrowsException() {
    // when & then
    assertThatThrownBy(() -> NotificationTemplate.COMMENT.renderContent())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("해당 알림 타입은 템플릿 기반 렌더링을 지원하지 않습니다");
  }

  @Test
  @DisplayName("특수 문자가 포함된 인자로 렌더링")
  void renderTitle_WithSpecialCharacters() {
    // given
    String nameWithSpecialChars = "홍길동<script>alert('test')</script>";

    // when
    String title = NotificationTemplate.LIKE.renderTitle(nameWithSpecialChars);

    // then
    assertThat(title).contains(nameWithSpecialChars);
  }

  @Test
  @DisplayName("빈 문자열 인자로 렌더링")
  void renderTitle_WithEmptyString() {
    // given
    String emptyName = "";

    // when
    String title = NotificationTemplate.LIKE.renderTitle(emptyName);

    // then
    assertThat(title).isEqualTo("님이 내 피드를 좋아합니다");
  }

  @Test
  @DisplayName("null 인자로 렌더링")
  void renderTitle_WithNullArg() {
    // given
    String nullName = null;

    // when
    String title = NotificationTemplate.LIKE.renderTitle(nullName);

    // then
    assertThat(title).contains("null");
  }

  @Test
  @DisplayName("인자 개수가 플레이스홀더보다 많은 경우")
  void renderTitle_MoreArgsThanPlaceholders() {
    // given
    String name1 = "홍길동";
    String name2 = "김철수"; // 사용되지 않음

    // when
    String title = NotificationTemplate.LIKE.renderTitle(name1, name2);

    // then
    assertThat(title).contains(name1);
    // name2는 사용되지 않음
  }

  @Test
  @DisplayName("인자 개수가 플레이스홀더보다 적은 경우")
  void renderContent_LessArgsThanPlaceholders() {
    // when
    String content = NotificationTemplate.ROLE_CHANGED.renderContent("USER");

    // then
    // 두 번째 플레이스홀더는 치환되지 않음
    assertThat(content).contains("USER");
    assertThat(content).contains("{}");
  }

  @Test
  @DisplayName("모든 템플릿 타입이 정의되어 있는지 확인")
  void allTemplatesAreDefined() {
    // when & then
    NotificationTemplate[] templates = NotificationTemplate.values();

    assertThat(templates).hasSizeGreaterThan(0);
    for (NotificationTemplate template : templates) {
      assertThat(template.getTitleTemplate()).isNotNull();
    }
  }
}
