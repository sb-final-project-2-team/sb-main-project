package com.codeit.closet.module.notification.event;

import com.codeit.closet.module.notification.entity.NotificationLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationEvent 테스트")
class NotificationEventTest {

  @Test
  @DisplayName("NotificationEvent 생성 및 필드 접근")
  void createNotificationEvent() {
    // given
    UUID id = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String title = "테스트 알림";
    String content = "테스트 내용";
    NotificationLevel level = NotificationLevel.INFO;
    Instant createdAt = Instant.now();

    // when
    NotificationEvent event = new NotificationEvent(
        id,
        receiverId,
        title,
        content,
        level,
        createdAt
    );

    // then
    assertThat(event.id()).isEqualTo(id);
    assertThat(event.receiverId()).isEqualTo(receiverId);
    assertThat(event.title()).isEqualTo(title);
    assertThat(event.content()).isEqualTo(content);
    assertThat(event.level()).isEqualTo(level);
    assertThat(event.createdAt()).isEqualTo(createdAt);
  }

  @Test
  @DisplayName("NotificationEvent equals 테스트")
  void testEquals() {
    // given
    UUID id = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    NotificationEvent event1 = new NotificationEvent(
        id, receiverId, "제목", "내용", NotificationLevel.INFO, createdAt
    );

    NotificationEvent event2 = new NotificationEvent(
        id, receiverId, "제목", "내용", NotificationLevel.INFO, createdAt
    );

    // when & then
    assertThat(event1).isEqualTo(event2);
    assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
  }

  @Test
  @DisplayName("NotificationEvent 다른 객체와 비교")
  void testNotEquals() {
    // given
    NotificationEvent event1 = new NotificationEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "제목1",
        "내용1",
        NotificationLevel.INFO,
        Instant.now()
    );

    NotificationEvent event2 = new NotificationEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "제목2",
        "내용2",
        NotificationLevel.WARNING,
        Instant.now()
    );

    // when & then
    assertThat(event1).isNotEqualTo(event2);
  }

  @Test
  @DisplayName("NotificationEvent toString 테스트")
  void testToString() {
    // given
    UUID id = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();

    NotificationEvent event = new NotificationEvent(
        id,
        receiverId,
        "테스트",
        "내용",
        NotificationLevel.INFO,
        Instant.now()
    );

    // when
    String result = event.toString();

    // then
    assertThat(result).contains("NotificationEvent");
    assertThat(result).contains(id.toString());
    assertThat(result).contains(receiverId.toString());
    assertThat(result).contains("테스트");
    assertThat(result).contains("내용");
  }

  @Test
  @DisplayName("NotificationEvent 모든 NotificationLevel 테스트")
  void testAllLevels() {
    // given
    UUID id = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    // when & then
    for (NotificationLevel level : NotificationLevel.values()) {
      NotificationEvent event = new NotificationEvent(
          id, receiverId, "제목", "내용", level, createdAt
      );

      assertThat(event.level()).isEqualTo(level);
    }
  }

  @Test
  @DisplayName("NotificationEvent null 필드 허용 테스트")
  void testNullFields() {
    // when
    NotificationEvent event = new NotificationEvent(
        null, null, null, null, null, null
    );

    // then
    assertThat(event.id()).isNull();
    assertThat(event.receiverId()).isNull();
    assertThat(event.title()).isNull();
    assertThat(event.content()).isNull();
    assertThat(event.level()).isNull();
    assertThat(event.createdAt()).isNull();
  }
}
