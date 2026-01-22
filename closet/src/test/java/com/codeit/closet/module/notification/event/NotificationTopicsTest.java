package com.codeit.closet.module.notification.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationTopics 테스트")
class NotificationTopicsTest {

  @Test
  @DisplayName("NOTIFICATION_CREATED 토픽 이름 확인")
  void notificationCreatedTopic() {
    // when & then
    assertThat(NotificationTopics.NOTIFICATION_CREATED)
        .isEqualTo("notification.created");
  }

  @Test
  @DisplayName("토픽 이름이 null이 아님")
  void topicNameNotNull() {
    // when & then
    assertThat(NotificationTopics.NOTIFICATION_CREATED).isNotNull();
  }

  @Test
  @DisplayName("토픽 이름이 빈 문자열이 아님")
  void topicNameNotEmpty() {
    // when & then
    assertThat(NotificationTopics.NOTIFICATION_CREATED).isNotEmpty();
  }

  @Test
  @DisplayName("토픽 이름 형식 확인 (점으로 구분)")
  void topicNameFormat() {
    // when & then
    assertThat(NotificationTopics.NOTIFICATION_CREATED).contains(".");
  }
}
