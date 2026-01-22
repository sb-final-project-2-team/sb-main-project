package com.codeit.closet.module.notification.event;

import com.codeit.closet.module.notification.entity.NotificationLevel;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaNotificationEventPublisher 테스트")
class KafkaNotificationEventPublisherTest {

  @Mock
  private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

  @InjectMocks
  private KafkaNotificationEventPublisher eventPublisher;

  private UUID testNotificationId;
  private UUID testReceiverId;
  private NotificationEvent testEvent;

  @BeforeEach
  void setUp() {
    testNotificationId = UUID.randomUUID();
    testReceiverId = UUID.randomUUID();

    testEvent = new NotificationEvent(
        testNotificationId,
        testReceiverId,
        "테스트 알림",
        "테스트 내용",
        NotificationLevel.INFO,
        Instant.now()
    );
  }

  @Test
  @DisplayName("Kafka로 알림 이벤트 발행 성공")
  void publish_Success() {
    // given
    CompletableFuture<SendResult<String, NotificationEvent>> future = new CompletableFuture<>();
    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    // when
    eventPublisher.publish(testEvent);

    // then
    ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);

    verify(kafkaTemplate, times(1))
        .send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

    assertThat(topicCaptor.getValue()).isEqualTo(NotificationTopics.NOTIFICATION_CREATED);
    assertThat(keyCaptor.getValue()).isEqualTo(testReceiverId.toString());
    assertThat(eventCaptor.getValue()).isEqualTo(testEvent);
  }

  @Test
  @DisplayName("receiverId가 null인 경우 'unknown' 키 사용")
  void publish_NullReceiverId() {
    // given
    NotificationEvent eventWithNullReceiver = new NotificationEvent(
        testNotificationId,
        null,
        "제목",
        "내용",
        NotificationLevel.INFO,
        Instant.now()
    );

    CompletableFuture<SendResult<String, NotificationEvent>> future = new CompletableFuture<>();
    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    // when
    eventPublisher.publish(eventWithNullReceiver);

    // then
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

    verify(kafkaTemplate, times(1))
        .send(eq(NotificationTopics.NOTIFICATION_CREATED), keyCaptor.capture(), eq(eventWithNullReceiver));

    assertThat(keyCaptor.getValue()).isEqualTo("unknown");
  }

  @Test
  @DisplayName("여러 알림 이벤트 연속 발행")
  void publish_MultipleEvents() {
    // given
    CompletableFuture<SendResult<String, NotificationEvent>> future = new CompletableFuture<>();
    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    NotificationEvent event1 = new NotificationEvent(
        UUID.randomUUID(), UUID.randomUUID(), "알림1", "내용1", NotificationLevel.INFO, Instant.now()
    );
    NotificationEvent event2 = new NotificationEvent(
        UUID.randomUUID(), UUID.randomUUID(), "알림2", "내용2", NotificationLevel.WARNING, Instant.now()
    );
    NotificationEvent event3 = new NotificationEvent(
        UUID.randomUUID(), UUID.randomUUID(), "알림3", "내용3", NotificationLevel.ERROR, Instant.now()
    );

    // when
    eventPublisher.publish(event1);
    eventPublisher.publish(event2);
    eventPublisher.publish(event3);

    // then
    verify(kafkaTemplate, times(3))
        .send(any(String.class), any(String.class), any(NotificationEvent.class));
  }

  @Test
  @DisplayName("모든 NotificationLevel에 대한 이벤트 발행")
  void publish_AllNotificationLevels() {
    // given
    CompletableFuture<SendResult<String, NotificationEvent>> future = new CompletableFuture<>();
    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    // when & then
    for (NotificationLevel level : NotificationLevel.values()) {
      NotificationEvent event = new NotificationEvent(
          UUID.randomUUID(),
          UUID.randomUUID(),
          "제목",
          "내용",
          level,
          Instant.now()
      );

      eventPublisher.publish(event);
    }

    verify(kafkaTemplate, times(NotificationLevel.values().length))
        .send(any(String.class), any(String.class), any(NotificationEvent.class));
  }

  @Test
  @DisplayName("같은 receiverId에 대한 여러 이벤트 발행")
  void publish_SameReceiverId() {
    // given
    UUID sameReceiverId = UUID.randomUUID();
    CompletableFuture<SendResult<String, NotificationEvent>> future = new CompletableFuture<>();
    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    NotificationEvent event1 = new NotificationEvent(
        UUID.randomUUID(), sameReceiverId, "알림1", "내용1", NotificationLevel.INFO, Instant.now()
    );
    NotificationEvent event2 = new NotificationEvent(
        UUID.randomUUID(), sameReceiverId, "알림2", "내용2", NotificationLevel.INFO, Instant.now()
    );

    // when
    eventPublisher.publish(event1);
    eventPublisher.publish(event2);

    // then
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

    verify(kafkaTemplate, times(2))
        .send(eq(NotificationTopics.NOTIFICATION_CREATED), keyCaptor.capture(), any(NotificationEvent.class));

    // 모든 호출이 같은 키를 사용했는지 확인
    assertThat(keyCaptor.getAllValues())
        .allMatch(key -> key.equals(sameReceiverId.toString()));
  }

  @Test
  @DisplayName("서로 다른 receiverId에 대한 이벤트 발행")
  void publish_DifferentReceiverIds() {
    // given
    UUID receiverId1 = UUID.randomUUID();
    UUID receiverId2 = UUID.randomUUID();
    UUID receiverId3 = UUID.randomUUID();

    CompletableFuture<SendResult<String, NotificationEvent>> future = new CompletableFuture<>();
    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    NotificationEvent event1 = new NotificationEvent(
        UUID.randomUUID(), receiverId1, "알림", "내용", NotificationLevel.INFO, Instant.now()
    );
    NotificationEvent event2 = new NotificationEvent(
        UUID.randomUUID(), receiverId2, "알림", "내용", NotificationLevel.INFO, Instant.now()
    );
    NotificationEvent event3 = new NotificationEvent(
        UUID.randomUUID(), receiverId3, "알림", "내용", NotificationLevel.INFO, Instant.now()
    );

    // when
    eventPublisher.publish(event1);
    eventPublisher.publish(event2);
    eventPublisher.publish(event3);

    // then
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

    verify(kafkaTemplate, times(3))
        .send(eq(NotificationTopics.NOTIFICATION_CREATED), keyCaptor.capture(), any(NotificationEvent.class));

    assertThat(keyCaptor.getAllValues()).containsExactlyInAnyOrder(
        receiverId1.toString(),
        receiverId2.toString(),
        receiverId3.toString()
    );
  }

  @Test
  @DisplayName("올바른 토픽으로 이벤트 발행")
  void publish_CorrectTopic() {
    // given
    CompletableFuture<SendResult<String, NotificationEvent>> future = new CompletableFuture<>();
    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    // when
    eventPublisher.publish(testEvent);

    // then
    verify(kafkaTemplate).send(
        eq(NotificationTopics.NOTIFICATION_CREATED),
        any(String.class),
        any(NotificationEvent.class)
    );
  }

  @Test
  @DisplayName("이벤트 내용이 정확히 전달되는지 확인")
  void publish_EventContentPreserved() {
    // given
    String specificTitle = "중요한 알림";
    String specificContent = "시스템 점검 안내";
    NotificationLevel specificLevel = NotificationLevel.WARNING;

    NotificationEvent specificEvent = new NotificationEvent(
        testNotificationId,
        testReceiverId,
        specificTitle,
        specificContent,
        specificLevel,
        Instant.now()
    );

    CompletableFuture<SendResult<String, NotificationEvent>> future = new CompletableFuture<>();
    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    // when
    eventPublisher.publish(specificEvent);

    // then
    ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);

    verify(kafkaTemplate).send(
        any(String.class),
        any(String.class),
        eventCaptor.capture()
    );

    NotificationEvent capturedEvent = eventCaptor.getValue();
    assertThat(capturedEvent.title()).isEqualTo(specificTitle);
    assertThat(capturedEvent.content()).isEqualTo(specificContent);
    assertThat(capturedEvent.level()).isEqualTo(specificLevel);
    assertThat(capturedEvent.id()).isEqualTo(testNotificationId);
    assertThat(capturedEvent.receiverId()).isEqualTo(testReceiverId);
  }

  @Test
  @DisplayName("Kafka 전송 성공 콜백 처리")
  void publish_SuccessCallback() {
    // given
    ProducerRecord<String, NotificationEvent> producerRecord = new ProducerRecord<>(
        NotificationTopics.NOTIFICATION_CREATED,
        testReceiverId.toString(),
        testEvent
    );

    RecordMetadata metadata = new RecordMetadata(
        new TopicPartition(NotificationTopics.NOTIFICATION_CREATED, 0),
        0L, 0L, System.currentTimeMillis(), 0L, 0, 0
    );

    SendResult<String, NotificationEvent> sendResult = new SendResult<>(producerRecord, metadata);
    CompletableFuture<SendResult<String, NotificationEvent>> future = CompletableFuture.completedFuture(sendResult);

    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    // when
    eventPublisher.publish(testEvent);

    // then
    verify(kafkaTemplate, times(1))
        .send(any(String.class), any(String.class), any(NotificationEvent.class));

    // 콜백이 실행되어야 함 (로그 출력)
  }

  @Test
  @DisplayName("Kafka 전송 실패 콜백 처리")
  void publish_FailureCallback() {
    // given
    CompletableFuture<SendResult<String, NotificationEvent>> future = new CompletableFuture<>();
    future.completeExceptionally(new RuntimeException("Kafka 전송 실패"));

    when(kafkaTemplate.send(any(String.class), any(String.class), any(NotificationEvent.class)))
        .thenReturn(future);

    // when
    eventPublisher.publish(testEvent);

    // then
    verify(kafkaTemplate, times(1))
        .send(any(String.class), any(String.class), any(NotificationEvent.class));

    // 에러 콜백이 실행되어야 함 (로그 출력)
  }

  @Test
  @DisplayName("여러 이벤트 발행 시 각각 콜백 처리")
  void publish_MultipleEventsWithCallbacks() {
    // given
    NotificationEvent event1 = new NotificationEvent(
        UUID.randomUUID(), UUID.randomUUID(), "알림1", "내용1", NotificationLevel.INFO, Instant.now()
    );
    NotificationEvent event2 = new NotificationEvent(
        UUID.randomUUID(), UUID.randomUUID(), "알림2", "내용2", NotificationLevel.INFO, Instant.now()
    );

    ProducerRecord<String, NotificationEvent> producerRecord1 = new ProducerRecord<>(
        NotificationTopics.NOTIFICATION_CREATED, event1.receiverId().toString(), event1
    );
    RecordMetadata metadata1 = new RecordMetadata(
        new TopicPartition(NotificationTopics.NOTIFICATION_CREATED, 0),
        0L, 0L, System.currentTimeMillis(), 0L, 0, 0
    );
    SendResult<String, NotificationEvent> sendResult1 = new SendResult<>(producerRecord1, metadata1);

    CompletableFuture<SendResult<String, NotificationEvent>> future1 = CompletableFuture.completedFuture(sendResult1);
    CompletableFuture<SendResult<String, NotificationEvent>> future2 = new CompletableFuture<>();
    future2.completeExceptionally(new RuntimeException("전송 실패"));

    when(kafkaTemplate.send(any(String.class), any(String.class), eq(event1)))
        .thenReturn(future1);
    when(kafkaTemplate.send(any(String.class), any(String.class), eq(event2)))
        .thenReturn(future2);

    // when
    eventPublisher.publish(event1);
    eventPublisher.publish(event2);

    // then
    verify(kafkaTemplate, times(2))
        .send(any(String.class), any(String.class), any(NotificationEvent.class));
  }
}
