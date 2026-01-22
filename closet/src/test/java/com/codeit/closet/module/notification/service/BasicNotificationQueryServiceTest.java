package com.codeit.closet.module.notification.service;

import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.dto.NotificationDTOCursorResponse;
import com.codeit.closet.module.notification.entity.Notification;
import com.codeit.closet.module.notification.entity.NotificationLevel;
import com.codeit.closet.module.notification.mapper.NotificationMapper;
import com.codeit.closet.module.notification.repository.NotificationRepository;
import com.codeit.closet.module.notification.service.impl.BasicNotificationQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicNotificationQueryService 테스트")
class BasicNotificationQueryServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private NotificationMapper notificationMapper;

  @InjectMocks
  private BasicNotificationQueryService queryService;

  private UUID testReceiverId;
  private Instant testCreatedAt;
  private List<Notification> testNotifications;
  private List<NotificationDTO> testNotificationDTOs;

  @BeforeEach
  void setUp() {
    testReceiverId = UUID.randomUUID();
    testCreatedAt = Instant.now();

    testNotifications = new ArrayList<>();
    testNotificationDTOs = new ArrayList<>();

    for (int i = 0; i < 3; i++) {
      UUID notificationId = UUID.randomUUID();
      Instant createdAt = testCreatedAt.minusSeconds(i * 60);

      Notification notification = Notification.builder()
          .id(notificationId)
          .receiverId(testReceiverId)
          .title("알림 " + i)
          .content("내용 " + i)
          .level(NotificationLevel.INFO)
          .createdAt(createdAt)
          .build();

      NotificationDTO dto = new NotificationDTO(
          notificationId,
          testReceiverId,
          "알림 " + i,
          "내용 " + i,
          NotificationLevel.INFO,
          createdAt
      );

      testNotifications.add(notification);
      testNotificationDTOs.add(dto);
    }
  }

  @Test
  @DisplayName("알림 목록 조회 성공 - cursor 없이")
  void findByReceiverId_WithoutCursor() {
    // given
    int limit = 20;
    when(notificationRepository.findByCursor(
        eq(testReceiverId),
        eq(null),
        eq(null),
        eq(limit)
    )).thenReturn(testNotifications);

    when(notificationRepository.countByReceiverId(testReceiverId))
        .thenReturn(3L);

    for (int i = 0; i < testNotifications.size(); i++) {
      when(notificationMapper.toDto(testNotifications.get(i)))
          .thenReturn(testNotificationDTOs.get(i));
    }

    // when
    NotificationDTOCursorResponse result = queryService.findByReceiverId(
        testReceiverId,
        null,
        null,
        limit
    );

    // then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(3);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.totalCount()).isEqualTo(3L);
    assertThat(result.sortBy()).isEqualTo("createdAt");
    assertThat(result.sortDirection()).isEqualTo("DESCENDING");

    verify(notificationRepository, times(1))
        .findByCursor(testReceiverId, null, null, limit);
    verify(notificationRepository, times(1))
        .countByReceiverId(testReceiverId);
    verify(notificationMapper, times(3)).toDto(any(Notification.class));
  }

  @Test
  @DisplayName("알림 목록 조회 성공 - cursor와 함께")
  void findByReceiverId_WithCursor() {
    // given
    int limit = 20;
    UUID idAfter = UUID.randomUUID();
    String cursor = encodeCursor(testCreatedAt);

    when(notificationRepository.findByCursor(
        eq(testReceiverId),
        any(Instant.class),
        eq(idAfter),
        eq(limit)
    )).thenReturn(testNotifications);

    when(notificationRepository.countByReceiverId(testReceiverId))
        .thenReturn(3L);

    for (int i = 0; i < testNotifications.size(); i++) {
      when(notificationMapper.toDto(testNotifications.get(i)))
          .thenReturn(testNotificationDTOs.get(i));
    }

    // when
    NotificationDTOCursorResponse result = queryService.findByReceiverId(
        testReceiverId,
        cursor,
        idAfter,
        limit
    );

    // then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(3);
    assertThat(result.hasNext()).isFalse();

    verify(notificationRepository, times(1))
        .findByCursor(eq(testReceiverId), any(Instant.class), eq(idAfter), eq(limit));
    verify(notificationMapper, times(3)).toDto(any(Notification.class));
  }

  @Test
  @DisplayName("알림 목록 조회 성공 - hasNext가 true인 경우")
  void findByReceiverId_WithHasNext() {
    // given
    int limit = 2;

    // 3개의 알림을 생성하여 limit보다 많게 만듦
    Notification notification1 = testNotifications.get(0);
    Notification notification2 = testNotifications.get(1);
    Notification notification3 = Notification.builder()
        .id(UUID.randomUUID())
        .receiverId(testReceiverId)
        .title("알림 3")
        .content("내용 3")
        .level(NotificationLevel.INFO)
        .createdAt(testCreatedAt.minusSeconds(180))
        .build();

    List<Notification> moreNotifications = List.of(notification1, notification2, notification3);

    NotificationDTO dto1 = testNotificationDTOs.get(0);
    NotificationDTO dto2 = testNotificationDTOs.get(1);

    when(notificationRepository.findByCursor(
        eq(testReceiverId),
        eq(null),
        eq(null),
        eq(limit)
    )).thenReturn(moreNotifications);

    when(notificationRepository.countByReceiverId(testReceiverId))
        .thenReturn(4L);

    when(notificationMapper.toDto(notification1))
        .thenReturn(dto1);
    when(notificationMapper.toDto(notification2))
        .thenReturn(dto2);

    // when
    NotificationDTOCursorResponse result = queryService.findByReceiverId(
        testReceiverId,
        null,
        null,
        limit
    );

    // then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.nextIdAfter()).isNotNull();

    verify(notificationRepository, times(1))
        .findByCursor(testReceiverId, null, null, limit);
  }

  @Test
  @DisplayName("알림 목록 조회 성공 - 빈 결과")
  void findByReceiverId_EmptyResult() {
    // given
    int limit = 20;
    when(notificationRepository.findByCursor(
        eq(testReceiverId),
        eq(null),
        eq(null),
        eq(limit)
    )).thenReturn(new ArrayList<>());

    when(notificationRepository.countByReceiverId(testReceiverId))
        .thenReturn(0L);

    // when
    NotificationDTOCursorResponse result = queryService.findByReceiverId(
        testReceiverId,
        null,
        null,
        limit
    );

    // then
    assertThat(result).isNotNull();
    assertThat(result.data()).isEmpty();
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.nextIdAfter()).isNull();
    assertThat(result.totalCount()).isEqualTo(0L);

    verify(notificationRepository, times(1))
        .findByCursor(testReceiverId, null, null, limit);
    verify(notificationMapper, never()).toDto(any(Notification.class));
  }

  @Test
  @DisplayName("알림 목록 조회 성공 - 잘못된 cursor (null로 처리)")
  void findByReceiverId_InvalidCursor() {
    // given
    int limit = 20;
    String invalidCursor = "invalid-cursor-string";

    when(notificationRepository.findByCursor(
        eq(testReceiverId),
        eq(null),
        eq(null),
        eq(limit)
    )).thenReturn(testNotifications);

    when(notificationRepository.countByReceiverId(testReceiverId))
        .thenReturn(3L);

    for (int i = 0; i < testNotifications.size(); i++) {
      when(notificationMapper.toDto(testNotifications.get(i)))
          .thenReturn(testNotificationDTOs.get(i));
    }

    // when
    NotificationDTOCursorResponse result = queryService.findByReceiverId(
        testReceiverId,
        invalidCursor,
        null,
        limit
    );

    // then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(3);

    verify(notificationRepository, times(1))
        .findByCursor(testReceiverId, null, null, limit);
  }

  @Test
  @DisplayName("알림 목록 조회 성공 - 빈 cursor")
  void findByReceiverId_BlankCursor() {
    // given
    int limit = 20;
    String blankCursor = "   ";

    when(notificationRepository.findByCursor(
        eq(testReceiverId),
        eq(null),
        eq(null),
        eq(limit)
    )).thenReturn(testNotifications);

    when(notificationRepository.countByReceiverId(testReceiverId))
        .thenReturn(3L);

    for (int i = 0; i < testNotifications.size(); i++) {
      when(notificationMapper.toDto(testNotifications.get(i)))
          .thenReturn(testNotificationDTOs.get(i));
    }

    // when
    NotificationDTOCursorResponse result = queryService.findByReceiverId(
        testReceiverId,
        blankCursor,
        null,
        limit
    );

    // then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(3);

    verify(notificationRepository, times(1))
        .findByCursor(testReceiverId, null, null, limit);
  }

  private String encodeCursor(Instant createdAt) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(createdAt.toString().getBytes());
  }
}
