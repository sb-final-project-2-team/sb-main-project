package com.codeit.closet.module.elastic.service;

import com.codeit.closet.module.elastic.document.FeedDocument;
import com.codeit.closet.module.elastic.dto.FeedSearchResult;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedSearchService {

  private final ElasticsearchTemplate elasticsearchTemplate;

  public FeedSearchResult searchFeedIds(
      String cursor, UUID idAfter, Integer limit, String sortBy,
      String sortDirection, String keywordLike, SkyStatus skyStatusEqual,
      PrecipitationType precipitationTypeEqual, UUID authorIdEqual
  ) {
    Sort.Direction direction =
        "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;

    String sortField = resolveSortField(sortBy);

    Sort sort = Sort.by(
        new Sort.Order(direction, sortField),
        new Sort.Order(direction, "id.keyword")
    );

    NativeQueryBuilder builder = new NativeQueryBuilder()
        .withQuery(q -> q.bool(b -> {

          if (keywordLike != null && !keywordLike.isBlank()) {
            b.must(m -> m.match(mm -> mm
                .field("content")
                .query(keywordLike)
                .fuzziness("AUTO")
                .minimumShouldMatch("70%")
            ));
          }

          if (skyStatusEqual != null) {
            b.filter(f -> f.term(t ->
                t.field("skyStatus").value(skyStatusEqual.name())
            ));
          }

          if (precipitationTypeEqual != null) {
            b.filter(f -> f.term(t ->
                t.field("precipitationType").value(precipitationTypeEqual.name())
            ));
          }

          if (authorIdEqual != null) {
            b.filter(f -> f.term(t ->
                t.field("authorId").value(authorIdEqual.toString())
            ));
          }

          return b;
        }))
        .withSort(sort)
        .withPageable(PageRequest.of(0, limit + 1));

    if (cursor != null && idAfter != null) {
      Object cursorValue = parseCursor(sortBy, cursor);
      builder.withSearchAfter(List.of(cursorValue, idAfter));
    }

    NativeQuery query = builder.build();

    SearchHits<FeedDocument> hits =
        elasticsearchTemplate.search(query, FeedDocument.class);

    boolean hasNext = hits.getSearchHits().size() > limit;

    List<UUID> feedIds = hits.getSearchHits().stream()
        .limit(limit)
        .map(hit -> UUID.fromString(hit.getContent().getId()))
        .toList();

    String nextCursor = null;
    UUID nextAfter = null;
    long totalCount = hits.getTotalHits();
    if (hasNext) {
      NextCursor next = extractNextCursor(hits, limit, sortBy);
      nextCursor = next.cursor();
      nextAfter = next.idAfter();
    }

    return new FeedSearchResult(
        feedIds,
        nextCursor,
        nextAfter,
        hasNext,
        totalCount
    );
  }

  private NextCursor extractNextCursor(SearchHits<FeedDocument> hits, int limit, String sortBy) {
    var sortValues = hits.getSearchHits().get(limit - 1).getSortValues();
    String nextCursor;

    if ("createdAt".equals(sortBy)) {
      long millis = ((Number) sortValues.get(0)).longValue();
      nextCursor = Instant.ofEpochMilli(millis).toString();
    } else {
      nextCursor = sortValues.get(0).toString();
    }

    UUID nextIdAfter = UUID.fromString(sortValues.get(1).toString());

    return new NextCursor(nextCursor, nextIdAfter);
  }

  private Object parseCursor(String sortBy, String raw) {
    return switch (sortBy) {
      case "likeCount" -> Long.parseLong(raw);
      case "createdAt" -> Instant.parse(raw).toEpochMilli();
      default -> throw new IllegalArgumentException("정렬값이 이상합니다.");
    };
  }

  private String resolveSortField(String sortBy) {
    return switch (sortBy) {
      case "createdAt" -> "createdAt";
      case "likeCount" -> "likeCount";
      default -> "createdAt";
    };
  }

  private record NextCursor(String cursor, UUID idAfter) {

  }
}

