package com.codeit.closet.module.user.repository.impl;

import com.codeit.closet.module.user.dto.user.UserDTOCursorResponse;
import com.codeit.closet.module.user.entity.QUser;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.mapper.UserMapper;
import com.codeit.closet.module.user.repository.UserQueryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
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
public class UserQueryRepositoryImpl implements UserQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;
  private final UserMapper userMapper;

  private static final QUser user = QUser.user;

  @Override
  public UserDTOCursorResponse findUsersByCursor(String cursor, UUID idAfter, Integer limit,
      String sortBy,
      String sortDirection, String emailLike, String roleEqual, Boolean locked) {

    int pageSize = limit != null ? limit : 20;

    CursorInfo cursorInfo = parseCursor(cursor);

    BooleanBuilder builder = new BooleanBuilder();
    if (cursorInfo != null) {
      builder.and(cursorCondition(cursorInfo, sortBy, sortDirection));
    }

    if (emailLike != null && !emailLike.isEmpty()) {
      builder.and(user.email.containsIgnoreCase(emailLike));
    }

    if (roleEqual != null && !roleEqual.isEmpty()) {
      builder.and(user.role.eq(UserRole.valueOf(roleEqual)));
    }

    if (locked != null) {
      builder.and(user.locked.eq(locked));
    }

    List<User> users = jpaQueryFactory
        .selectFrom(user)
        .leftJoin(user.binaryContent).fetchJoin()
        .leftJoin(user.weather).fetchJoin()
        .where(builder)
        .orderBy(orderSpecifiers(sortBy, sortDirection))
        .limit(pageSize + 1)
        .fetch();

    Long totalCount = jpaQueryFactory
        .select(user.count())
        .from(user)
        .where(
            emailLike != null && !emailLike.isEmpty() ? user.email.containsIgnoreCase(emailLike)
                : null,
            roleEqual != null && !roleEqual.isEmpty() ? user.role.eq(UserRole.valueOf(roleEqual))
                : null,
            locked != null ? user.locked.eq(locked) : null
        )
        .fetchOne();

    boolean hasNext = users.size() > pageSize;
    if (hasNext) {
      users.remove(pageSize);
    }

    String nextCursor = null;
    UUID nextAfter = null;

    if (!users.isEmpty()) {
      User last = users.get(users.size() - 1);
      nextCursor = encodeCursor(last);
      nextAfter = last.getId();
    }

    return new UserDTOCursorResponse(
        userMapper.toUserDTOs(users),
        nextCursor,
        nextAfter,
        hasNext,
        totalCount,
        sortBy != null ? sortBy : "createdAt",
        sortDirection != null ? sortDirection : "DESCENDING"
    );
  }

  // ==================== 커서 관련 유틸 ====================
  private record CursorInfo(Instant createdAt, UUID id) {

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
    } catch (IllegalArgumentException | DateTimeException e) {
      return null;
    }
  }

  private String encodeCursor(User user) {
    String raw = user.getCreatedAt() + "|" + user.getId();
    return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  // ==================== 정렬 & 커서 조건 ====================
  private OrderSpecifier<?>[] orderSpecifiers(String sortBy, String sortDirection) {
    boolean desc = "DESCENDING".equalsIgnoreCase(sortDirection);

    if ("email".equalsIgnoreCase(sortBy)) {
      return new OrderSpecifier[]{
          desc ? user.email.desc() : user.email.asc(),
          desc ? user.id.desc() : user.id.asc()
      };
    }

    return new OrderSpecifier[]{
        desc ? user.createdAt.desc() : user.createdAt.asc(),
        desc ? user.id.desc() : user.id.asc()
    };
  }

  private BooleanBuilder cursorCondition(CursorInfo cursor, String sortBy, String sortDirection) {
    BooleanBuilder builder = new BooleanBuilder();
    boolean desc = "DESCENDING".equalsIgnoreCase(sortDirection);

    if (desc) {
      builder.or(user.createdAt.lt(cursor.createdAt())
          .or(user.createdAt.eq(cursor.createdAt())
              .and(user.id.lt(cursor.id()))));
    } else {
      builder.or(user.createdAt.gt(cursor.createdAt())
          .or(user.createdAt.eq(cursor.createdAt())
              .and(user.id.gt(cursor.id()))));
    }

    return builder;
  }
}
