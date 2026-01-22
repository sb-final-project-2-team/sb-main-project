package com.codeit.closet.module.elastic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import com.codeit.closet.module.elastic.document.FeedDocument;
import com.codeit.closet.module.elastic.repository.FeedElasticRepository;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedElasticServiceTest {

  @InjectMocks
  private FeedElasticService feedElasticService;

  @Mock
  private FeedElasticRepository feedElasticRepository;

  @Test
  @DisplayName("Feed를 FeedDocument로 변환하여 Elasticsearch에 저장한다")
  void index_success() {
    // given
    UUID feedId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Feed feed = mock(Feed.class);
    var user = mock(com.codeit.closet.module.user.entity.User.class);
    var weather = mock(com.codeit.closet.module.weather.entity.WeatherData.class);

    given(feed.getId()).willReturn(feedId);
    given(feed.getContent()).willReturn("test content");
    given(feed.getUser()).willReturn(user);
    given(feed.getWeather()).willReturn(weather);
    given(feed.getLikeCount()).willReturn(5L);
    given(feed.getCreatedAt()).willReturn(Instant.now());

    given(user.getId()).willReturn(userId);
    given(weather.getSkyStatus()).willReturn(SkyStatus.CLEAR);
    given(weather.getPrecipitationType()).willReturn(PrecipitationType.NONE);

    // when
    feedElasticService.index(feed);

    // then
    ArgumentCaptor<FeedDocument> captor =
        ArgumentCaptor.forClass(FeedDocument.class);

    then(feedElasticRepository).should(times(1)).save(captor.capture());

    FeedDocument saved = captor.getValue();
    assertThat(saved.getId()).isEqualTo(feedId.toString());
    assertThat(saved.getContent()).isEqualTo("test content");
    assertThat(saved.getAuthorId()).isEqualTo(userId.toString());
    assertThat(saved.getSkyStatus()).isEqualTo(SkyStatus.CLEAR);
    assertThat(saved.getPrecipitationType()).isEqualTo(PrecipitationType.NONE);
    assertThat(saved.getLikeCount()).isEqualTo(5);
  }

  @Test
  @DisplayName("Feed ID로 Elasticsearch 문서를 삭제한다")
  void delete_success() {
    // given
    UUID feedId = UUID.randomUUID();

    // when
    feedElasticService.delete(feedId);

    // then
    then(feedElasticRepository)
        .should(times(1))
        .deleteById(feedId.toString());
  }
}
