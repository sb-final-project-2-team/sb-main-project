package com.codeit.closet.module.notification.mapper;

import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.entity.Notification;
import com.codeit.closet.module.notification.entity.NotificationLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("NotificationMapper 통합 테스트")
class NotificationMapperTest {

  @Autowired
  private NotificationMapper notificationMapper;

  @Test
  @DisplayName("toDto - 기본 변환")
  void toDto_Basic() {
    // given
    UUID notificationId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    Notification notification = Notification.builder()
        .id(notificationId)
        .receiverId(receiverId)
        .title("테스트 알림")
        .content("테스트 내용")
        .level(NotificationLevel.INFO)
        .createdAt(createdAt)
        .build();

    // when
    NotificationDTO result = notificationMapper.toDto(notification);

    // then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(notificationId);
    assertThat(result.receiverId()).isEqualTo(receiverId);
    assertThat(result.title()).isEqualTo("테스트 알림");
    assertThat(result.content()).isEqualTo("테스트 내용");
    assertThat(result.level()).isEqualTo(NotificationLevel.INFO);
    assertThat(result.createdAt()).isEqualTo(createdAt);
  }

  @Test
  @DisplayName("toDto - 모든 NotificationLevel 값 테스트")
  void toDto_AllLevels() {
    // given
    UUID receiverId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    for (NotificationLevel level : NotificationLevel.values()) {
      Notification notification = Notification.builder()
          .id(UUID.randomUUID())
          .receiverId(receiverId)
          .title("레벨 테스트")
          .content("레벨: " + level.name())
          .level(level)
          .createdAt(createdAt)
          .build();

      // when
      NotificationDTO result = notificationMapper.toDto(notification);

      // then
      assertThat(result.level()).isEqualTo(level);
    }
  }

  @Test
  @DisplayName("toDto - 긴 제목과 내용")
  void toDto_LongTitleAndContent() {
    // given
    String longTitle = "A".repeat(200);
    String longContent = "B".repeat(10000);

    Notification notification = Notification.builder()
        .id(UUID.randomUUID())
        .receiverId(UUID.randomUUID())
        .title(longTitle)
        .content(longContent)
        .level(NotificationLevel.INFO)
        .createdAt(Instant.now())
        .build();

    // when
    NotificationDTO result = notificationMapper.toDto(notification);

    // then
    assertThat(result.title()).isEqualTo(longTitle);
    assertThat(result.content()).isEqualTo(longContent);
  }

  @Test
  @DisplayName("toDto - 특수 문자가 포함된 제목과 내용")
  void toDto_SpecialCharacters() {
    // given
    String specialTitle = "알림! @#$% 한글 테스트 😀";
    String specialContent = "내용 with 특수문자 <>\"'&";

    Notification notification = Notification.builder()
        .id(UUID.randomUUID())
        .receiverId(UUID.randomUUID())
        .title(specialTitle)
        .content(specialContent)
        .level(NotificationLevel.INFO)
        .createdAt(Instant.now())
        .build();

    // when
    NotificationDTO result = notificationMapper.toDto(notification);

    // then
    assertThat(result.title()).isEqualTo(specialTitle);
    assertThat(result.content()).isEqualTo(specialContent);
  }

  @Test
  @DisplayName("toDto - 여러 알림 변환 일관성 테스트")
  void toDto_MultipleNotifications() {
    // given
    UUID receiverId = UUID.randomUUID();

    Notification notification1 = Notification.builder()
        .id(UUID.randomUUID())
        .receiverId(receiverId)
        .title("알림 1")
        .content("내용 1")
        .level(NotificationLevel.INFO)
        .createdAt(Instant.now())
        .build();

    Notification notification2 = Notification.builder()
        .id(UUID.randomUUID())
        .receiverId(receiverId)
        .title("알림 2")
        .content("내용 2")
        .level(NotificationLevel.WARNING)
        .createdAt(Instant.now())
        .build();

    Notification notification3 = Notification.builder()
        .id(UUID.randomUUID())
        .receiverId(receiverId)
        .title("알림 3")
        .content("내용 3")
        .level(NotificationLevel.ERROR)
        .createdAt(Instant.now())
        .build();

    // when
    NotificationDTO result1 = notificationMapper.toDto(notification1);
    NotificationDTO result2 = notificationMapper.toDto(notification2);
    NotificationDTO result3 = notificationMapper.toDto(notification3);

    // then
    assertThat(result1.title()).isEqualTo("알림 1");
    assertThat(result2.title()).isEqualTo("알림 2");
    assertThat(result3.title()).isEqualTo("알림 3");

    assertThat(result1.level()).isEqualTo(NotificationLevel.INFO);
    assertThat(result2.level()).isEqualTo(NotificationLevel.WARNING);
    assertThat(result3.level()).isEqualTo(NotificationLevel.ERROR);
  }

  @Test
  @DisplayName("toDto - 같은 receiverId를 가진 여러 알림")
  void toDto_SameReceiver() {
    // given
    UUID sharedReceiverId = UUID.randomUUID();

    Notification notification1 = Notification.builder()
        .id(UUID.randomUUID())
        .receiverId(sharedReceiverId)
        .title("첫 번째 알림")
        .content("첫 번째 내용")
        .level(NotificationLevel.INFO)
        .createdAt(Instant.now())
        .build();

    Notification notification2 = Notification.builder()
        .id(UUID.randomUUID())
        .receiverId(sharedReceiverId)
        .title("두 번째 알림")
        .content("두 번째 내용")
        .level(NotificationLevel.INFO)
        .createdAt(Instant.now())
        .build();

    // when
    NotificationDTO result1 = notificationMapper.toDto(notification1);
    NotificationDTO result2 = notificationMapper.toDto(notification2);

    // then
    assertThat(result1.receiverId()).isEqualTo(sharedReceiverId);
    assertThat(result2.receiverId()).isEqualTo(sharedReceiverId);
    assertThat(result1.id()).isNotEqualTo(result2.id());
  }
}
