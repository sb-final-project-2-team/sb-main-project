package com.codeit.closet.module.notification.service;

import com.codeit.closet.module.notification.entity.Notification;
import com.codeit.closet.module.notification.repository.NotificationRepository;
import com.codeit.closet.module.notification.template.NotificationTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("NotificationService 통합 테스트")
class NotificationServiceIntegrationTest {

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
  @DisplayName("createWithRawContent - DM 알림 생성")
  void createWithRawContent_DM() {
    // given
    String senderName = "홍길동";
    String rawContent = "안녕하세요! 메시지입니다.";

    // when
    notificationService.createWithRawContent(
        testReceiverId,
        NotificationTemplate.DM_RECEIVED,
        rawContent,
        senderName
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    assertThat(notifications).isNotEmpty();

    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("[DM] 홍길동");
    assertThat(notification.getContent()).isEqualTo(rawContent);
  }

  @Test
  @DisplayName("createWithRawContent - 댓글 알림 생성")
  void createWithRawContent_Comment() {
    // given
    String commenterName = "김철수";
    String commentContent = "좋은 글이네요!";

    // when
    notificationService.createWithRawContent(
        testReceiverId,
        NotificationTemplate.COMMENT,
        commentContent,
        commenterName
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("김철수님이 댓글을 달았어요.");
    assertThat(notification.getContent()).isEqualTo(commentContent);
  }

  @Test
  @DisplayName("createWithRawContent - 좋아요 알림 생성")
  void createWithRawContent_Like() {
    // given
    String likerName = "이영희";
    String feedContent = "멋진 피드입니다.";

    // when
    notificationService.createWithRawContent(
        testReceiverId,
        NotificationTemplate.LIKE,
        feedContent,
        likerName
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("이영희님이 내 피드를 좋아합니다");
    assertThat(notification.getContent()).isEqualTo(feedContent);
  }

  @Test
  @DisplayName("createWithRenderContent - 권한 변경 알림")
  void createWithRenderContent_RoleChanged() {
    // given
    Object[] titleArgs = {};
    Object[] contentArgs = {"USER", "ADMIN"};

    // when
    notificationService.createWithRenderContent(
        testReceiverId,
        NotificationTemplate.ROLE_CHANGED,
        titleArgs,
        contentArgs
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("내 권한이 변경되었어요.");
    assertThat(notification.getContent()).isEqualTo("내 권한이 [USER]에서 [ADMIN]로 변경되었어요.");
  }

  @Test
  @DisplayName("createWithRenderContent - 속성 추가 알림")
  void createWithRenderContent_AttributeAdd() {
    // given
    Object[] titleArgs = {};
    Object[] contentArgs = {"색상"};

    // when
    notificationService.createWithRenderContent(
        testReceiverId,
        NotificationTemplate.ATTRIBUTE_ADD,
        titleArgs,
        contentArgs
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("의상 속성이 추가되었어요.");
    assertThat(notification.getContent()).isEqualTo("[색상] 속성을 확인해보세요.");
  }

  @Test
  @DisplayName("createWithRenderContent - 속성 변경 알림")
  void createWithRenderContent_AttributeChanged() {
    // given
    Object[] titleArgs = {};
    Object[] contentArgs = {"사이즈"};

    // when
    notificationService.createWithRenderContent(
        testReceiverId,
        NotificationTemplate.ATTRIBUTE_CHANGED,
        titleArgs,
        contentArgs
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("의상 속성이 변경되었어요.");
    assertThat(notification.getContent()).isEqualTo("[사이즈] 속성을 확인해보세요.");
  }

  @Test
  @DisplayName("createWithRenderContent - 피드 알림")
  void createWithRenderContent_Feed() {
    // given
    String authorName = "박민수";
    Object[] titleArgs = {authorName};
    Object[] contentArgs = {authorName};

    // when
    notificationService.createWithRenderContent(
        testReceiverId,
        NotificationTemplate.FEED,
        titleArgs,
        contentArgs
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("박민수님이 새로운 피드를 작성했어요.");
    assertThat(notification.getContent()).isEqualTo("박민수님이 따끈따끈한 소식을 전해왔어요!");
  }

  @Test
  @DisplayName("createWithRenderContent - 팔로우 알림")
  void createWithRenderContent_Followed() {
    // given
    String followerName = "정수진";
    Object[] titleArgs = {followerName};
    Object[] contentArgs = {followerName};

    // when
    notificationService.createWithRenderContent(
        testReceiverId,
        NotificationTemplate.FOLLOWED,
        titleArgs,
        contentArgs
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("정수진님이 회원님을 팔로우했습니다.");
    assertThat(notification.getContent()).isEqualTo("정수진님의 피드를 확인해보세요");
  }

  @Test
  @DisplayName("createWithRenderContent - 날씨 경고 알림")
  void createWithRenderContent_WeatherAlert() {
    // given
    Object[] titleArgs = {};
    Object[] contentArgs = {};

    // when
    notificationService.createWithRenderContent(
        testReceiverId,
        NotificationTemplate.WEATHER_ALERT,
        titleArgs,
        contentArgs
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("급격한 날씨 변화가 관측되었어요.");
    assertThat(notification.getContent()).isEqualTo("기온이 급격히 변했습니다. 외출 시 주의하세요.");
  }

  @Test
  @DisplayName("createWithRenderContent - titleArgs가 null인 경우")
  void createWithRenderContent_NullTitleArgs() {
    // given
    Object[] titleArgs = null;
    Object[] contentArgs = {"USER", "ADMIN"};

    // when
    notificationService.createWithRenderContent(
        testReceiverId,
        NotificationTemplate.ROLE_CHANGED,
        titleArgs,
        contentArgs
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("내 권한이 변경되었어요.");
  }

  @Test
  @DisplayName("createWithRenderContent - titleArgs가 빈 배열인 경우")
  void createWithRenderContent_EmptyTitleArgs() {
    // given
    Object[] titleArgs = {};
    Object[] contentArgs = {"USER", "ADMIN"};

    // when
    notificationService.createWithRenderContent(
        testReceiverId,
        NotificationTemplate.ROLE_CHANGED,
        titleArgs,
        contentArgs
    );

    // then
    List<Notification> notifications = notificationRepository.findAll();
    Notification notification = notifications.stream()
        .filter(n -> n.getReceiverId().equals(testReceiverId))
        .findFirst()
        .orElseThrow();

    assertThat(notification.getTitle()).isEqualTo("내 권한이 변경되었어요.");
  }
}
