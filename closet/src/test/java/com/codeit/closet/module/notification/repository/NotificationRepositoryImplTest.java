package com.codeit.closet.module.notification.repository;

import com.codeit.closet.module.notification.entity.Notification;
import com.codeit.closet.module.notification.entity.NotificationLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("NotificationRepositoryImpl 테스트")
class NotificationRepositoryImplTest {

  @Autowired
  private NotificationRepository notificationRepository;

  private UUID testReceiverId;
  private List<Notification> testNotifications;

  @BeforeEach
  void setUp() {
    testReceiverId = UUID.randomUUID();

    // 테스트 데이터 생성 (시간 순서대로)
    testNotifications = List.of(
        createNotification(testReceiverId, "알림 1", Instant.now().minusSeconds(300)),
        createNotification(testReceiverId, "알림 2", Instant.now().minusSeconds(200)),
        createNotification(testReceiverId, "알림 3", Instant.now().minusSeconds(100)),
        createNotification(testReceiverId, "알림 4", Instant.now().minusSeconds(50)),
        createNotification(testReceiverId, "알림 5", Instant.now())
    );

    notificationRepository.saveAll(testNotifications);
  }

  @Test
  @DisplayName("커서 없이 알림 목록 조회 - 최신순 정렬")
  void findByCursor_WithoutCursor() {
    // given
    int limit = 3;

    // when
    List<Notification> result = notificationRepository.findByCursor(
        testReceiverId,
        null,
        null,
        limit
    );

    // then
    assertThat(result).hasSize(limit + 1); // limit + 1 개 조회
    assertThat(result.get(0).getTitle()).isEqualTo("알림 5"); // 최신순
    assertThat(result.get(1).getTitle()).isEqualTo("알림 4");
    assertThat(result.get(2).getTitle()).isEqualTo("알림 3");
  }

  @Test
  @DisplayName("커서를 사용한 페이징 조회")
  void findByCursor_WithCursor() {
    // given
    int limit = 2;

    // 첫 페이지 조회
    List<Notification> firstPage = notificationRepository.findByCursor(
        testReceiverId,
        null,
        null,
        limit
    );

    // 마지막 항목의 커서 정보
    Notification lastItem = firstPage.get(limit - 1);
    Instant cursorCreatedAt = lastItem.getCreatedAt();
    UUID cursorId = lastItem.getId();

    // when
    List<Notification> secondPage = notificationRepository.findByCursor(
        testReceiverId,
        cursorCreatedAt,
        cursorId,
        limit
    );

    // then
    assertThat(secondPage).isNotEmpty();
    assertThat(secondPage).allMatch(n ->
        n.getCreatedAt().isBefore(cursorCreatedAt) ||
        (n.getCreatedAt().equals(cursorCreatedAt) && n.getId().compareTo(cursorId) < 0)
    );
  }

  @Test
  @DisplayName("다른 receiverId의 알림은 조회되지 않음")
  void findByCursor_FilterByReceiverId() {
    // given
    UUID otherReceiverId = UUID.randomUUID();
    notificationRepository.save(
        createNotification(otherReceiverId, "다른 사용자 알림", Instant.now())
    );

    // when
    List<Notification> result = notificationRepository.findByCursor(
        testReceiverId,
        null,
        null,
        10
    );

    // then
    assertThat(result).allMatch(n -> n.getReceiverId().equals(testReceiverId));
    assertThat(result).noneMatch(n -> n.getReceiverId().equals(otherReceiverId));
  }

  @Test
  @DisplayName("limit보다 적은 데이터가 있을 때")
  void findByCursor_LessThanLimit() {
    // given
    UUID newReceiverId = UUID.randomUUID();
    notificationRepository.save(
        createNotification(newReceiverId, "단일 알림", Instant.now())
    );

    // when
    List<Notification> result = notificationRepository.findByCursor(
        newReceiverId,
        null,
        null,
        10
    );

    // then
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("알림이 없는 경우 빈 리스트 반환")
  void findByCursor_EmptyResult() {
    // given
    UUID emptyReceiverId = UUID.randomUUID();

    // when
    List<Notification> result = notificationRepository.findByCursor(
        emptyReceiverId,
        null,
        null,
        10
    );

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("같은 시간에 생성된 알림들의 조회")
  void findByCursor_SameCreatedAt() {
    // given
    UUID sameTimeReceiverId = UUID.randomUUID();
    Instant sameTime = Instant.now();

    List<Notification> sameTimeNotifications = List.of(
        createNotification(sameTimeReceiverId, "알림 A", sameTime),
        createNotification(sameTimeReceiverId, "알림 B", sameTime),
        createNotification(sameTimeReceiverId, "알림 C", sameTime)
    );

    notificationRepository.saveAll(sameTimeNotifications);

    // when
    List<Notification> result = notificationRepository.findByCursor(
        sameTimeReceiverId,
        null,
        null,
        10
    );

    // then
    assertThat(result).hasSize(3);
    // 모든 알림이 같은 시간에 생성됨
    assertThat(result).allMatch(n -> n.getCreatedAt().equals(sameTime));
    // 모든 알림이 조회됨
    assertThat(result).extracting(Notification::getTitle)
        .containsExactlyInAnyOrder("알림 A", "알림 B", "알림 C");
  }

  @Test
  @DisplayName("receiverId로 알림 개수 카운트")
  void countByReceiverId() {
    // when
    long count = notificationRepository.countByReceiverId(testReceiverId);

    // then
    assertThat(count).isEqualTo(5);
  }

  @Test
  @DisplayName("알림이 없는 receiverId의 카운트는 0")
  void countByReceiverId_EmptyResult() {
    // given
    UUID emptyReceiverId = UUID.randomUUID();

    // when
    long count = notificationRepository.countByReceiverId(emptyReceiverId);

    // then
    assertThat(count).isEqualTo(0);
  }

  @Test
  @DisplayName("알림 삭제 성공")
  void deleteByIdAndReceiverId_Success() {
    // given
    Notification notification = testNotifications.get(0);

    // when
    long deleted = notificationRepository.deleteByIdAndReceiverId(
        notification.getId(),
        testReceiverId
    );

    // then
    assertThat(deleted).isEqualTo(1);
    assertThat(notificationRepository.findById(notification.getId())).isEmpty();
  }

  @Test
  @DisplayName("다른 receiverId로 삭제 시도 - 실패")
  void deleteByIdAndReceiverId_WrongReceiverId() {
    // given
    Notification notification = testNotifications.get(0);
    UUID wrongReceiverId = UUID.randomUUID();

    // when
    long deleted = notificationRepository.deleteByIdAndReceiverId(
        notification.getId(),
        wrongReceiverId
    );

    // then
    assertThat(deleted).isEqualTo(0);
    assertThat(notificationRepository.findById(notification.getId())).isPresent();
  }

  @Test
  @DisplayName("존재하지 않는 알림 삭제 시도")
  void deleteByIdAndReceiverId_NotFound() {
    // given
    UUID nonExistentId = UUID.randomUUID();

    // when
    long deleted = notificationRepository.deleteByIdAndReceiverId(
        nonExistentId,
        testReceiverId
    );

    // then
    assertThat(deleted).isEqualTo(0);
  }

  private Notification createNotification(UUID receiverId, String title, Instant createdAt) {
    return Notification.builder()
        .receiverId(receiverId)
        .title(title)
        .content("테스트 내용")
        .level(NotificationLevel.INFO)
        .createdAt(createdAt)
        .build();
  }
}
