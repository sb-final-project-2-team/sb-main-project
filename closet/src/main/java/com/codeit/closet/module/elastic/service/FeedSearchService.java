package com.codeit.closet.module.elastic.service;

import com.codeit.closet.module.elastic.document.FeedDocument;
import com.codeit.closet.module.elastic.dto.FeedSearchResult;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.SkyStatus;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
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
            b.must(m -> m.matchPhrase(mp -> mp
                .field("content")
                .query(keywordLike)
                .slop(1)
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
        .withTimeout(Duration.ofMillis(500))
        .withPageable(PageRequest.of(0, limit + 1));

    if (cursor != null) {
      CursorInfo cursorInfo = parseCursor(cursor);
      if (cursorInfo != null) {
        builder.withSearchAfter(
            List.of(cursorInfo.sortValue().toEpochMilli(), cursorInfo.idAfter()));
      }
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
      var lastHit = hits.getSearchHits().get(limit - 1);
      Instant lastCreatedAt = lastHit.getContent().getCreatedAt();
      nextAfter = UUID.fromString(lastHit.getContent().getId());
      nextCursor = encodeCursor(lastCreatedAt, nextAfter);
    }

    return new FeedSearchResult(
        feedIds,
        nextCursor,
        nextAfter,
        hasNext,
        totalCount
    );
  }

  private String resolveSortField(String sortBy) {
    return switch (sortBy) {
      case "createdAt" -> "createdAt";
      case "likeCount" -> "likeCount";
      default -> "createdAt";
    };
  }

  private record CursorInfo(Instant sortValue, UUID idAfter) {

  }

  private CursorInfo parseCursor(String cursor) {
    if (cursor == null) {
      return null;
    }
    try {
      String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
      String[] parts = decoded.split("\\|");
      if (parts.length < 2) {
        return null;
      }
      return new CursorInfo(
          Instant.parse(parts[0]),
          UUID.fromString(parts[1])
      );
    } catch (Exception e) {
      return null;
    }
  }

  private String encodeCursor(Instant sortValue, UUID idAfter) {
    String raw = sortValue + "|" + idAfter;
    return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }
}

