package com.codeit.closet.module.feed.repository.impl;

import com.codeit.closet.module.feed.dto.FeedDTO;
import com.codeit.closet.module.feed.dto.FeedDTOCursorResponse;
import com.codeit.closet.module.feed.entity.Feed;
import com.codeit.closet.module.feed.entity.QFeed;
import com.codeit.closet.module.feed.mapper.FeedMapper;
import com.codeit.closet.module.feed.repository.FeedQueryRepository;
import com.codeit.closet.module.like.entity.QLike;
import com.codeit.closet.module.user.entity.QUser;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.QWeatherData;
import com.codeit.closet.module.weather.entity.QWeatherRegion;
import com.codeit.closet.module.weather.entity.SkyStatus;
import com.codeit.closet.module.weather.mapper.WeatherMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedQueryRepositoryImpl implements FeedQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;
  private final FeedMapper feedMapper;

  private static final QFeed feed = QFeed.feed;
  private static final QUser user = QUser.user;
  private static final QLike like = QLike.like;
  private static final QWeatherRegion weatherRegion = QWeatherRegion.weatherRegion;
  private static final QWeatherData weatherData = QWeatherData.weatherData;
  @Override
  public FeedDTOCursorResponse findFeedsByCursor(String cursor, UUID idAfter, Integer limit,
      String sortBy,
      String sortDirection, String keywordLike, SkyStatus skyStatusEqual,
      PrecipitationType precipitationTypeEqual, UUID authorIdEqual, UUID principal) {

    if (principal == null) {
      throw new IllegalArgumentException("인증 정보가 없습니다.");
    }

    int pageSize = limit != null ? limit : 20;
    CursorInfo cursorInfo = parseCursor(cursor);

    BooleanBuilder builder = new BooleanBuilder();

    if (cursorInfo != null) {
      builder.and(cursorCondition(cursorInfo, sortBy, sortDirection));
    }

    if (keywordLike != null && !keywordLike.isEmpty()) {
      builder.and(feed.content.containsIgnoreCase(keywordLike));
    }

    if (authorIdEqual != null) {
      builder.and(feed.user.id.eq(authorIdEqual));
    }

    if (skyStatusEqual != null) {
      builder.and(feed.weather.weatherData.skyStatus.eq(skyStatusEqual));
    }

    if (precipitationTypeEqual != null) {
      builder.and(feed.weather.weatherData.precipitationType.eq(precipitationTypeEqual));
    }

    List<Feed> feeds = jpaQueryFactory
        .selectFrom(feed)
        .join(feed.user, user).fetchJoin()
        .join(feed.weather, weatherRegion).fetchJoin()
        .join(feed.weather.weatherData, weatherData).fetchJoin()
        .where(builder)
        .orderBy(orderSpecifiers(sortBy, sortDirection))
        .limit(pageSize + 1)
        .fetch();

    Long totalCount = jpaQueryFactory
        .select(feed.count())
        .from(feed)
        .join(feed.weather, weatherRegion)
        .join(feed.weather.weatherData, weatherData)
        .where(
            keywordLike != null && !keywordLike.isEmpty()
                ? feed.content.containsIgnoreCase(keywordLike)
                : null,
            authorIdEqual != null
                ? feed.user.id.eq(authorIdEqual)
                : null,
            skyStatusEqual != null
                ? feed.weather.weatherData.skyStatus.eq(skyStatusEqual)
                : null,
            precipitationTypeEqual != null
                ? feed.weather.weatherData.precipitationType.eq(precipitationTypeEqual)
                : null
        )
        .fetchOne();

    boolean hasNext = feeds.size() > pageSize;
    if (hasNext) {
      feeds.remove(pageSize);
    }

    String nextCursor = null;
    UUID nextAfter = null;

    if (!feeds.isEmpty()) {
      Feed last = feeds.get(feeds.size() - 1);
      nextCursor = encodeCursor(last);
      nextAfter = last.getId();
    }

    List<UUID> feedIds = feeds.stream()
        .map(Feed::getId)
        .toList();

    Set<UUID> likedFeedIds = new HashSet<>(
        jpaQueryFactory
            .select(like.feed.id)
            .from(like)
            .where(
                like.feed.id.in(feedIds),
                like.user.id.eq(principal)
            )
            .fetch()
    );

    List<FeedDTO> feedDTOs = feedMapper.toFeedDTOs(feeds).stream()
        .map(dto -> dto.withLikedByMe(likedFeedIds.contains(dto.id())))
        .toList();

    return new FeedDTOCursorResponse(
        feedDTOs,
        nextCursor,
        nextAfter,
        hasNext,
        totalCount,
        sortBy != null ? sortBy : "createdAt",
        sortDirection != null ? sortDirection : "DESCENDING"
    );
  }

  // ==================== 커서 관련 유틸 ====================
  private record CursorInfo(Instant createdAt, UUID id) {}

  private CursorInfo parseCursor(String cursor) {
    if (cursor == null) {
      return null;
    }
    try {
      String decoded = new String(
          Base64.getDecoder().decode(cursor),
          StandardCharsets.UTF_8
      );
      String[] parts = decoded.split("\\|");
      if (parts.length < 2) {
        return null;
      }
      return new CursorInfo(
          Instant.parse(parts[0]),
          UUID.fromString(parts[1])
      );
    } catch (IllegalArgumentException | DateTimeException e) {
      return null;
    }
  }

  private String encodeCursor(Feed feed) {
    String raw = feed.getCreatedAt() + "|" + feed.getId();
    return Base64.getEncoder()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  // ==================== 정렬 & 커서 조건 ====================
  private OrderSpecifier<?>[] orderSpecifiers(String sortBy, String sortDirection) {
    boolean desc = "DESCENDING".equalsIgnoreCase(sortDirection);

    if ("likeCount".equalsIgnoreCase(sortBy)) {
      return new OrderSpecifier[]{
          desc ? feed.likeCount.desc() : feed.likeCount.asc(),
          desc ? feed.createdAt.desc() : feed.createdAt.asc(),
          desc ? feed.id.desc() : feed.id.asc()
      };
    }

    return new OrderSpecifier[]{
        desc ? feed.createdAt.desc() : feed.createdAt.asc(),
        desc ? feed.id.desc() : feed.id.asc()
    };
  }

  private BooleanBuilder cursorCondition(
      CursorInfo cursor,
      String sortBy,
      String sortDirection
  ) {
    BooleanBuilder builder = new BooleanBuilder();
    boolean desc = "DESCENDING".equalsIgnoreCase(sortDirection);

    if (desc) {
      builder.and(
          feed.createdAt.lt(cursor.createdAt())
              .or(feed.createdAt.eq(cursor.createdAt())
                  .and(feed.id.lt(cursor.id())))
      );
    } else {
      builder.and(
          feed.createdAt.gt(cursor.createdAt())
              .or(feed.createdAt.eq(cursor.createdAt())
                  .and(feed.id.gt(cursor.id())))
      );
    }

    return builder;
  }
}
