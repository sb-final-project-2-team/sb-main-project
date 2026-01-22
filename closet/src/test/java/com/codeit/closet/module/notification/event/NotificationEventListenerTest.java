package com.codeit.closet.module.notification.event;

import com.codeit.closet.module.notification.service.NotificationService;
import com.codeit.closet.module.notification.template.NotificationTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListener 테스트")
class NotificationEventListenerTest {

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private NotificationEventListener eventListener;

  @Test
  @DisplayName("rawContent가 있는 이벤트 처리")
  void handleNotification_WithRawContent() {
    // given
    UUID receiverId = UUID.randomUUID();
    String rawContent = "테스트 원본 내용";
    Object[] titleArgs = {"사용자1"};

    NotifyUserEvent event = new NotifyUserEvent(
        receiverId,
        NotificationTemplate.DM_RECEIVED,
        rawContent,
        titleArgs,
        null
    );

    // when
    eventListener.handleNotification(event);

    // then
    verify(notificationService, times(1))
        .createWithRawContent(receiverId, NotificationTemplate.DM_RECEIVED, rawContent, titleArgs);
    verify(notificationService, never())
        .createWithRenderContent(any(), any(), any(), any());
  }

  @Test
  @DisplayName("rawContent가 null인 이벤트 처리 - 렌더링 사용")
  void handleNotification_WithoutRawContent() {
    // given
    UUID receiverId = UUID.randomUUID();
    Object[] titleArgs = {};
    Object[] contentArgs = {"속성1"};

    NotifyUserEvent event = new NotifyUserEvent(
        receiverId,
        NotificationTemplate.ATTRIBUTE_ADD,
        null,
        titleArgs,
        contentArgs
    );

    // when
    eventListener.handleNotification(event);

    // then
    verify(notificationService, times(1))
        .createWithRenderContent(receiverId, NotificationTemplate.ATTRIBUTE_ADD, titleArgs, contentArgs);
    verify(notificationService, never())
        .createWithRawContent(any(), any(), any(), any());
  }

  @Test
  @DisplayName("DM 수신 이벤트 처리")
  void handleNotification_DmReceived() {
    // given
    UUID receiverId = UUID.randomUUID();
    String senderName = "송신자";
    String messageContent = "안녕하세요!";

    NotifyUserEvent event = new NotifyUserEvent(
        receiverId,
        NotificationTemplate.DM_RECEIVED,
        messageContent,
        new Object[]{senderName},
        null
    );

    // when
    eventListener.handleNotification(event);

    // then
    verify(notificationService, times(1))
        .createWithRawContent(
            receiverId,
            NotificationTemplate.DM_RECEIVED,
            messageContent,
            new Object[]{senderName}
        );
  }

  @Test
  @DisplayName("댓글 이벤트 처리")
  void handleNotification_Comment() {
    // given
    UUID receiverId = UUID.randomUUID();
    String commenterName = "댓글작성자";
    String commentContent = "좋은 피드네요!";

    NotifyUserEvent event = new NotifyUserEvent(
        receiverId,
        NotificationTemplate.COMMENT,
        commentContent,
        new Object[]{commenterName},
        null
    );

    // when
    eventListener.handleNotification(event);

    // then
    verify(notificationService, times(1))
        .createWithRawContent(
            receiverId,
            NotificationTemplate.COMMENT,
            commentContent,
            new Object[]{commenterName}
        );
  }

  @Test
  @DisplayName("권한 변경 이벤트 처리")
  void handleNotification_RoleChanged() {
    // given
    UUID receiverId = UUID.randomUUID();
    Object[] titleArgs = {};
    Object[] contentArgs = {"USER", "ADMIN"};

    NotifyUserEvent event = new NotifyUserEvent(
        receiverId,
        NotificationTemplate.ROLE_CHANGED,
        null,
        titleArgs,
        contentArgs
    );

    // when
    eventListener.handleNotification(event);

    // then
    verify(notificationService, times(1))
        .createWithRenderContent(
            receiverId,
            NotificationTemplate.ROLE_CHANGED,
            titleArgs,
            contentArgs
        );
  }

  @Test
  @DisplayName("속성 추가 이벤트 처리")
  void handleNotification_AttributeAdd() {
    // given
    UUID receiverId = UUID.randomUUID();
    Object[] titleArgs = {};
    Object[] contentArgs = {"색상"};

    NotifyUserEvent event = new NotifyUserEvent(
        receiverId,
        NotificationTemplate.ATTRIBUTE_ADD,
        null,
        titleArgs,
        contentArgs
    );

    // when
    eventListener.handleNotification(event);

    // then
    verify(notificationService, times(1))
        .createWithRenderContent(
            receiverId,
            NotificationTemplate.ATTRIBUTE_ADD,
            titleArgs,
            contentArgs
        );
  }

  @Test
  @DisplayName("날씨 경고 이벤트 처리")
  void handleNotification_WeatherAlert() {
    // given
    UUID receiverId = UUID.randomUUID();
    Object[] titleArgs = {};
    Object[] contentArgs = {};

    NotifyUserEvent event = new NotifyUserEvent(
        receiverId,
        NotificationTemplate.WEATHER_ALERT,
        null,
        titleArgs,
        contentArgs
    );

    // when
    eventListener.handleNotification(event);

    // then
    verify(notificationService, times(1))
        .createWithRenderContent(
            receiverId,
            NotificationTemplate.WEATHER_ALERT,
            titleArgs,
            contentArgs
        );
  }
}
