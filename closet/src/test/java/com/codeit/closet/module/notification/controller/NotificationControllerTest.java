package com.codeit.closet.module.notification.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.notification.dto.NotificationDTO;
import com.codeit.closet.module.notification.dto.NotificationDTOCursorResponse;
import com.codeit.closet.module.notification.entity.NotificationLevel;
import com.codeit.closet.module.notification.service.NotificationQueryService;
import com.codeit.closet.module.notification.service.NotificationService;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = NotificationController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = com.codeit.closet.common.config.SecurityConfig.class
    ))
@Import({com.codeit.closet.common.config.TestSecurityConfig.class, com.codeit.closet.common.exception.GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("NotificationController 테스트")
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private NotificationQueryService notificationQueryService;

  @MockBean
  private NotificationService notificationService;

  private UUID testUserId;
  private UUID testNotificationId;
  private UserDTO testUserDTO;
  private ClosetUserDetails mockUserDetails;
  private NotificationDTO testNotificationDTO;
  private NotificationDTOCursorResponse testResponse;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testNotificationId = UUID.randomUUID();

    testUserDTO = new UserDTO(
        testUserId,
        Instant.now(),
        "test@example.com",
        "testuser",
        UserRole.USER,
        false
    );

    mockUserDetails = new ClosetUserDetails(testUserDTO, "password123", null, null);

    testNotificationDTO = new NotificationDTO(
        testNotificationId,
        testUserId,
        "테스트 알림",
        "테스트 내용",
        NotificationLevel.INFO,
        Instant.now()
    );

    testResponse = new NotificationDTOCursorResponse(
        List.of(testNotificationDTO),
        null,
        null,
        false,
        1L,
        "createdAt",
        "DESCENDING"
    );
  }

  @Test
  @DisplayName("알림 목록 조회 성공")
  void findNotifications_Success() throws Exception {
    // given
    int limit = 20;
    when(notificationQueryService.findByReceiverId(
        eq(testUserId),
        isNull(),
        isNull(),
        eq(limit)
    )).thenReturn(testResponse);

    // when & then
    mockMvc.perform(get("/api/notifications")
            .param("limit", String.valueOf(limit))
            .with(user(mockUserDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(testNotificationId.toString()))
        .andExpect(jsonPath("$.data[0].title").value("테스트 알림"))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.totalCount").value(1));

    verify(notificationQueryService, times(1))
        .findByReceiverId(testUserId, null, null, limit);
  }

  @Test
  @DisplayName("알림 목록 조회 - cursor와 idAfter 포함")
  void findNotifications_WithCursor() throws Exception {
    // given
    int limit = 20;
    String cursor = "testCursor";
    UUID idAfter = UUID.randomUUID();

    when(notificationQueryService.findByReceiverId(
        eq(testUserId),
        eq(cursor),
        eq(idAfter),
        eq(limit)
    )).thenReturn(testResponse);

    // when & then
    mockMvc.perform(get("/api/notifications")
            .param("limit", String.valueOf(limit))
            .param("cursor", cursor)
            .param("idAfter", idAfter.toString())
            .with(user(mockUserDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray());

    verify(notificationQueryService, times(1))
        .findByReceiverId(testUserId, cursor, idAfter, limit);
  }

  @Test
  @DisplayName("알림 목록 조회 - 빈 결과")
  void findNotifications_EmptyResult() throws Exception {
    // given
    int limit = 20;
    NotificationDTOCursorResponse emptyResponse = new NotificationDTOCursorResponse(
        List.of(),
        null,
        null,
        false,
        0L,
        "createdAt",
        "DESCENDING"
    );

    when(notificationQueryService.findByReceiverId(
        eq(testUserId),
        isNull(),
        isNull(),
        eq(limit)
    )).thenReturn(emptyResponse);

    // when & then
    mockMvc.perform(get("/api/notifications")
            .param("limit", String.valueOf(limit))
            .with(user(mockUserDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.totalCount").value(0));
  }

  @Test
  @DisplayName("알림 삭제 성공")
  void deleteNotification_Success() throws Exception {
    // given
    doNothing().when(notificationService)
        .deleteNotification(testNotificationId, testUserId);

    // when & then
    mockMvc.perform(delete("/api/notifications/{notificationId}", testNotificationId)
            .with(user(mockUserDetails)))
        .andExpect(status().isNoContent());

    verify(notificationService, times(1))
        .deleteNotification(testNotificationId, testUserId);
  }

  @Test
  @DisplayName("알림 삭제 실패 - 권한 없음")
  void deleteNotification_Unauthorized() throws Exception {
    // given
    doThrow(new IllegalArgumentException("알림을 찾을 수 없거나 권한이 없습니다."))
        .when(notificationService)
        .deleteNotification(testNotificationId, testUserId);

    // when & then
    mockMvc.perform(delete("/api/notifications/{notificationId}", testNotificationId)
            .with(user(mockUserDetails)))
        .andExpect(status().isBadRequest());

    verify(notificationService, times(1))
        .deleteNotification(testNotificationId, testUserId);
  }

  @Test
  @DisplayName("알림 삭제 - 다른 사용자의 알림")
  void deleteNotification_DifferentUser() throws Exception {
    // given
    UUID otherNotificationId = UUID.randomUUID();
    doThrow(new IllegalArgumentException("알림을 찾을 수 없거나 권한이 없습니다."))
        .when(notificationService)
        .deleteNotification(otherNotificationId, testUserId);

    // when & then
    mockMvc.perform(delete("/api/notifications/{notificationId}", otherNotificationId)
            .with(user(mockUserDetails)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("알림 목록 조회 - limit이 음수일 때")
  void findNotifications_NegativeLimit() throws Exception {
    // when & then
    mockMvc.perform(get("/api/notifications")
            .param("limit", "-1")
            .with(user(mockUserDetails)))
        .andExpect(status().isOk()); // 서비스 레이어에서 처리

    verify(notificationQueryService, times(1))
        .findByReceiverId(eq(testUserId), isNull(), isNull(), eq(-1));
  }

  @Test
  @DisplayName("알림 목록 조회 - limit이 매우 큰 값일 때")
  void findNotifications_LargeLimit() throws Exception {
    // given
    int largeLimit = 1000;
    when(notificationQueryService.findByReceiverId(
        eq(testUserId),
        isNull(),
        isNull(),
        eq(largeLimit)
    )).thenReturn(testResponse);

    // when & then
    mockMvc.perform(get("/api/notifications")
            .param("limit", String.valueOf(largeLimit))
            .with(user(mockUserDetails)))
        .andExpect(status().isOk());

    verify(notificationQueryService, times(1))
        .findByReceiverId(testUserId, null, null, largeLimit);
  }
}
