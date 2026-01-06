package com.codeit.closet.module.comment.repository.impl;

import com.codeit.closet.module.comment.dto.CommentDTOCursorResponse;
import com.codeit.closet.module.comment.entity.Comment;
import com.codeit.closet.module.comment.entity.QComment;
import com.codeit.closet.module.comment.mapper.CommentMapper;
import com.codeit.closet.module.comment.repository.CommentQueryRepository;
import com.codeit.closet.module.feed.entity.QFeed;
import com.codeit.closet.module.user.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;
  private final CommentMapper commentMapper;

  private final QComment comment = QComment.comment;
  private final QFeed feed = QFeed.feed;
  private final QUser user = QUser.user;

  @Override
  public CommentDTOCursorResponse findCommentsByCursor(UUID feedId, String cursor, UUID idAfter,
      Integer limit) {

    int pageSize = limit != null ? limit : 20;
    CursorInfo cursorInfo = parseCursor(cursor);

    BooleanBuilder builder = new BooleanBuilder();

    if (cursorInfo != null) {
      builder.and(
          comment.createdAt.lt(cursorInfo.createdAt)
              .or(comment.createdAt.eq(cursorInfo.createdAt)
                  .and(comment.id.lt(cursorInfo.id)))
      );
    }

    if (feedId != null) {
      builder.and(comment.feed.id.eq(feedId));
    }


    List<Comment> comments = jpaQueryFactory
        .selectFrom(comment)
        .join(comment.user, user).fetchJoin()
        .join(comment.feed, feed).fetchJoin()
        .where(builder)
        .orderBy(comment.createdAt.desc(), comment.id.desc())
        .limit(pageSize + 1)
        .fetch();

    Long totalCount = jpaQueryFactory
        .select(feed.commentCount)
        .where(feed.id.eq(feedId))
        .from(feed)
        .fetchOne();

    boolean hasNext = comments.size() > pageSize;
    if (hasNext) {
      comments.remove(pageSize);
    }

    String nextCursor = null;
    UUID nextAfter = null;

    if (!comments.isEmpty()) {
      Comment last = comments.get(comments.size() - 1);
      nextCursor = encodeCursor(last);
      nextAfter = last.getId();
    }

    return new CommentDTOCursorResponse(
        commentMapper.toDTOs(comments),
        nextCursor,
        nextAfter,
        hasNext,
        totalCount,
        "createdAt",
        "DESCENDING"
    );
  }


  // ==================== 커서 관련 유틸 ====================
  private record CursorInfo(Instant createdAt, UUID id) {

  }

  private String encodeCursor(Comment comment) {
    String raw = comment.getCreatedAt() + "|" + comment.getId();
    return Base64.getEncoder()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

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
}