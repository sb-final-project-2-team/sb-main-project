package com.codeit.closet.module.notification.service;

import com.codeit.closet.module.notification.entity.Notification;
import com.codeit.closet.module.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("BasicNotificationService 통합 테스트")
class BasicNotificationServiceIntegrationTest {

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private NotificationRepository notificationRepository;

  private UUID testReceiverId;

  @BeforeEach
  void setUp() {
    testReceiverId = UUID.randomUUID();
  }

  @Test
  @Transactional
  @DisplayName("단일 알림 생성 - 트랜잭션 커밋 후 확인")
  void createNotification_AfterCommit() {
    // given
    String title = "테스트 알림";
    String content = "테스트 내용";

    // when
    notificationService.createNotification(testReceiverId, title, content);

    // then - 트랜잭션 내에서 확인
    List<Notification> notifications = notificationRepository.findAll();
    assertThat(notifications).isNotEmpty();

    Notification savedNotification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(savedNotification.getTitle()).isEqualTo(title);
    assertThat(savedNotification.getContent()).isEqualTo(content);
    assertThat(savedNotification.getReceiverId()).isEqualTo(testReceiverId);
  }

  @Test
  @Transactional
  @DisplayName("알림 삭제 - 트랜잭션 내 확인")
  void deleteNotification_InTransaction() {
    // given
    notificationService.createNotification(testReceiverId, "삭제할 알림", "삭제 테스트");

    List<Notification> beforeDelete = notificationRepository.findAll();
    Notification toDelete = beforeDelete.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    UUID notificationId = toDelete.getId();

    // when
    notificationService.deleteNotification(notificationId, testReceiverId);

    // then
    List<Notification> afterDelete = notificationRepository.findAll();
    boolean exists = afterDelete.stream()
        .anyMatch(n -> n.getId().equals(notificationId));

    assertThat(exists).isFalse();
  }

  @Test
  @Transactional
  @DisplayName("단일 알림 생성 후 즉시 조회")
  void createNotification_ImmediateQuery() {
    // given
    String title = "즉시 조회 테스트";
    String content = "생성 직후 조회";

    // when
    notificationService.createNotification(testReceiverId, title, content);

    // then - 트랜잭션 내에서 즉시 조회 가능
    long count = notificationRepository.countByReceiverId(testReceiverId);
    assertThat(count).isGreaterThan(0);
  }
}
