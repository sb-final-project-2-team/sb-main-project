package com.codeit.closet.module.elastic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

import com.codeit.closet.module.elastic.document.FeedDocument;
import com.codeit.closet.module.elastic.dto.FeedSearchResult;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

@ExtendWith(MockitoExtension.class)
class FeedSearchServiceTest {

  @InjectMocks
  private FeedSearchService feedSearchService;

  @Mock
  private ElasticsearchTemplate elasticsearchTemplate;

  private SearchHit<FeedDocument> mockHit(UUID id, Instant createdAt) {
    FeedDocument doc = FeedDocument.builder()
        .id(id.toString())
        .createdAt(createdAt)
        .build();

    SearchHit<FeedDocument> hit = mock(SearchHit.class);
    given(hit.getContent()).willReturn(doc);

    return hit;
  }

  private SearchHit<FeedDocument> mockSizeOnlyHit() {
    return mock(SearchHit.class);
  }


  @Test
  @DisplayName("limit 초과 결과가 있으면 hasNext=true, nextCursor 생성")
  void searchFeedIds_hasNext_true() {
    // given
    int limit = 2;

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    Instant t1 = Instant.now().minusSeconds(30);
    Instant t2 = Instant.now().minusSeconds(20);

    SearchHit<FeedDocument> hit1 = mockHit(id1, t1);
    SearchHit<FeedDocument> hit2 = mockHit(id2, t2);
    SearchHit<FeedDocument> hit3 = mockSizeOnlyHit();

    SearchHits<FeedDocument> searchHits = mock(SearchHits.class);
    given(searchHits.getSearchHits()).willReturn(List.of(hit1, hit2, hit3));
    given(searchHits.getTotalHits()).willReturn(10L);

    given(elasticsearchTemplate.search(any(NativeQuery.class), eq(FeedDocument.class)))
        .willReturn(searchHits);

    // when
    FeedSearchResult result = feedSearchService.searchFeedIds(
        null,
        null,
        limit,
        "createdAt",
        "desc",
        null,
        null,
        null,
        null
    );

    // then
    assertThat(result.feedIds()).containsExactly(id1, id2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.nextIdAfter()).isEqualTo(id2);
    assertThat(result.totalCount()).isEqualTo(10L);


  }

  @Test
  @DisplayName("결과 수가 limit 이하이면 hasNext=false")
  void searchFeedIds_hasNext_false() {
    // given
    int limit = 3;

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    Instant t1 = Instant.now().minusSeconds(20);
    Instant t2 = Instant.now().minusSeconds(10);

    SearchHit<FeedDocument> hit1 = mockHit(id1, t1);
    SearchHit<FeedDocument> hit2 = mockHit(id2, t2);

    SearchHits<FeedDocument> searchHits = mock(SearchHits.class);
    given(searchHits.getSearchHits()).willReturn(List.of(hit1, hit2));
    given(searchHits.getTotalHits()).willReturn(2L);

    given(elasticsearchTemplate.search(any(NativeQuery.class), eq(FeedDocument.class)))
        .willReturn(searchHits);

    // when
    FeedSearchResult result = feedSearchService.searchFeedIds(
        null,
        null,
        limit,
        "createdAt",
        "desc",
        null,
        null,
        null,
        null
    );

    // then
    assertThat(result.feedIds()).containsExactly(id1, id2);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.nextIdAfter()).isNull();
    assertThat(result.totalCount()).isEqualTo(2L);
  }

  @Test
  @DisplayName("cursor가 있어도 정상적으로 검색 수행")
  void searchFeedIds_withCursor() {
    // given
    int limit = 1;

    UUID id = UUID.randomUUID();
    Instant createdAt = Instant.now().minusSeconds(10);

    SearchHit<FeedDocument> hit = mockHit(id, createdAt);

    SearchHits<FeedDocument> searchHits = mock(SearchHits.class);
    given(searchHits.getSearchHits()).willReturn(List.of(hit));
    given(searchHits.getTotalHits()).willReturn(1L);

    given(elasticsearchTemplate.search(any(NativeQuery.class), eq(FeedDocument.class)))
        .willReturn(searchHits);

    String cursor = Base64.getEncoder()
        .encodeToString((createdAt + "|" + id).getBytes());

    // when
    FeedSearchResult result = feedSearchService.searchFeedIds(
        cursor,
        id,
        limit,
        "createdAt",
        "desc",
        null,
        null,
        null,
        null
    );

    // then
    assertThat(result.feedIds()).containsExactly(id);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("keywordLike가 있으면 match_phrase(content) 쿼리가 생성된다")
  void searchFeedIds_matchPhrase_query() {
    // given
    String keywordLike = "테스트 피드";

    SearchHits<FeedDocument> emptyHits = mock(SearchHits.class);
    given(emptyHits.getSearchHits()).willReturn(List.of());
    given(emptyHits.getTotalHits()).willReturn(0L);

    ArgumentCaptor<NativeQuery> queryCaptor =
        ArgumentCaptor.forClass(NativeQuery.class);

    given(elasticsearchTemplate.search(queryCaptor.capture(), eq(FeedDocument.class)))
        .willReturn(emptyHits);

    // when
    feedSearchService.searchFeedIds(
        null,
        null,
        10,
        "createdAt",
        "desc",
        keywordLike,
        null,
        null,
        null
    );

    // then
    NativeQuery query = queryCaptor.getValue();
    String dsl = query.getQuery().toString();

    assertThat(dsl).contains("match_phrase");
    assertThat(dsl).contains("content");
    assertThat(dsl).contains(keywordLike);
  }

  @Test
  @DisplayName("skyStatus, precipitationType가 있으면 term filter가 추가된다")
  void searchFeedIds_term_filters() {
    // given
    SearchHits<FeedDocument> emptyHits = mock(SearchHits.class);
    given(emptyHits.getSearchHits()).willReturn(List.of());
    given(emptyHits.getTotalHits()).willReturn(0L);

    ArgumentCaptor<NativeQuery> queryCaptor =
        ArgumentCaptor.forClass(NativeQuery.class);

    given(elasticsearchTemplate.search(queryCaptor.capture(), eq(FeedDocument.class)))
        .willReturn(emptyHits);

    // when
    feedSearchService.searchFeedIds(
        null,
        null,
        10,
        "createdAt",
        "desc",
        null,
        SkyStatus.CLEAR,
        PrecipitationType.RAIN,
        null
    );

    // then
    String dsl = queryCaptor.getValue().getQuery().toString();

    assertThat(dsl).contains("term");
    assertThat(dsl).contains("skyStatus");
    assertThat(dsl).contains("CLEAR");

    assertThat(dsl).contains("precipitationType");
    assertThat(dsl).contains("RAIN");
  }

  @Test
  @DisplayName("authorIdEqual이 있으면 authorId term filter가 추가된다")
  void searchFeedIds_authorId_filter() {
    // given
    UUID authorId = UUID.randomUUID();

    SearchHits<FeedDocument> emptyHits = mock(SearchHits.class);
    given(emptyHits.getSearchHits()).willReturn(List.of());
    given(emptyHits.getTotalHits()).willReturn(0L);

    ArgumentCaptor<NativeQuery> queryCaptor =
        ArgumentCaptor.forClass(NativeQuery.class);

    given(elasticsearchTemplate.search(queryCaptor.capture(), eq(FeedDocument.class)))
        .willReturn(emptyHits);

    // when
    feedSearchService.searchFeedIds(
        null,
        null,
        10,
        "createdAt",
        "desc",
        null,
        null,
        null,
        authorId
    );

    // then
    String dsl = queryCaptor.getValue().getQuery().toString();

    assertThat(dsl).contains("authorId");
    assertThat(dsl).contains(authorId.toString());
  }

  @Test
  @DisplayName("keywordLike + filter가 함께 있을 경우 bool must + filter로 구성된다")
  void searchFeedIds_combined_conditions() {
    // given
    UUID authorId = UUID.randomUUID();

    SearchHits<FeedDocument> emptyHits = mock(SearchHits.class);
    given(emptyHits.getSearchHits()).willReturn(List.of());
    given(emptyHits.getTotalHits()).willReturn(0L);

    ArgumentCaptor<NativeQuery> queryCaptor =
        ArgumentCaptor.forClass(NativeQuery.class);

    given(elasticsearchTemplate.search(queryCaptor.capture(), eq(FeedDocument.class)))
        .willReturn(emptyHits);

    // when
    feedSearchService.searchFeedIds(
        null,
        null,
        10,
        "createdAt",
        "desc",
        "테스트",
        SkyStatus.CLOUDY,
        null,
        authorId
    );

    // then
    String dsl = queryCaptor.getValue().getQuery().toString();

    assertThat(dsl).contains("bool");
    assertThat(dsl).contains("must");
    assertThat(dsl).contains("match_phrase");
    assertThat(dsl).contains("filter");
    assertThat(dsl).contains("skyStatus");
    assertThat(dsl).contains("authorId");
  }

}
