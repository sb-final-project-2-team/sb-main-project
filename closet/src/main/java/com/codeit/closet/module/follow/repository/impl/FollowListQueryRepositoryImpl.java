package com.codeit.closet.module.follow.repository.impl;

import com.codeit.closet.module.follow.dto.FollowListResponse;
import com.codeit.closet.module.follow.entity.Follow;
import com.codeit.closet.module.follow.entity.QFollow;
import com.codeit.closet.module.follow.mapper.FollowMapper;
import com.codeit.closet.module.follow.repository.FollowListQueryRepository;
import com.codeit.closet.module.user.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FollowListQueryRepositoryImpl implements FollowListQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final FollowMapper followMapper;

    private static final QFollow follow = QFollow.follow;
    private static final QUser followerUser = new QUser("followerUser");
    private static final QUser followingUser = new QUser("followingUser");

    @Override
    public FollowListResponse findFollowingList(
            UUID followerId,
            String cursor,
            UUID idAfter,
            Integer limit,
            String sortBy,
            String sortDirection,
            String nameLike
    ) {
        int pageSize = limit != null ? limit : 20;

        CursorInfo cursorInfo = parseCursor(cursor);

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(follow.follower.id.eq(followerId));

        if (cursorInfo != null) {
            builder.and(cursorCondition(cursorInfo, sortBy, sortDirection));
        }

        if (nameLike != null && !nameLike.isEmpty()) {
            builder.and(followingUser.name.containsIgnoreCase(nameLike));
        }

        List<Follow> follows = jpaQueryFactory
                .selectFrom(follow)
                .join(follow.followee, followingUser).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifiers(sortBy, sortDirection))
                .limit(pageSize + 1)
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(follow.count())
                .from(follow)
                .join(follow.followee, followingUser)
                .where(
                        follow.follower.id.eq(followerId),
                        nameLike != null && !nameLike.isEmpty()
                                ? followingUser.name.containsIgnoreCase(nameLike)
                                : null
                )
                .fetchOne();

        boolean hasNext = follows.size() > pageSize;
        if (hasNext) {
            follows.remove(pageSize);
        }

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (!follows.isEmpty()) {
            Follow last = follows.get(follows.size() - 1);
            nextCursor = encodeCursor(last);
            nextIdAfter = last.getId();
        }

        return new FollowListResponse(
                followMapper.toFollowDTOs(follows),
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount != null ? totalCount : 0L,
                sortBy != null ? sortBy : "createdAt",
                sortDirection != null ? sortDirection : "DESCENDING"
        );
    }

    @Override
    public FollowListResponse findFollowerList(
            UUID followeeId,
            String cursor,
            UUID idAfter,
            Integer limit,
            String sortBy,
            String sortDirection,
            String nameLike
    ) {
        int pageSize = limit != null ? limit : 20;

        CursorInfo cursorInfo = parseCursor(cursor);

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(follow.followee.id.eq(followeeId));

        if (cursorInfo != null) {
            builder.and(cursorCondition(cursorInfo, sortBy, sortDirection));
        }

        if (nameLike != null && !nameLike.isEmpty()) {
            builder.and(followerUser.name.containsIgnoreCase(nameLike));
        }

        List<Follow> follows = jpaQueryFactory
                .selectFrom(follow)
                .join(follow.follower, followerUser).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifiers(sortBy, sortDirection))
                .limit(pageSize + 1)
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(follow.count())
                .from(follow)
                .join(follow.follower, followerUser)
                .where(
                        follow.followee.id.eq(followeeId),
                        nameLike != null && !nameLike.isEmpty()
                                ? followerUser.name.containsIgnoreCase(nameLike)
                                : null
                )
                .fetchOne();

        boolean hasNext = follows.size() > pageSize;
        if (hasNext) {
            follows.remove(pageSize);
        }

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (!follows.isEmpty()) {
            Follow last = follows.get(follows.size() - 1);
            nextCursor = encodeCursor(last);
            nextIdAfter = last.getId();
        }

        return new FollowListResponse(
                followMapper.toFollowDTOs(follows),
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount != null ? totalCount : 0L,
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

    private String encodeCursor(Follow follow) {
        String raw = follow.getCreatedAt() + "|" + follow.getId();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== 정렬 & 커서 조건 ====================
    private OrderSpecifier<?>[] orderSpecifiers(String sortBy, String sortDirection) {
        boolean desc = "DESCENDING".equalsIgnoreCase(sortDirection);

        if ("name".equalsIgnoreCase(sortBy)) {
            return new OrderSpecifier[]{
                    desc ? followerUser.name.desc().nullsLast() : followerUser.name.asc().nullsLast(),
                    desc ? followingUser.name.desc().nullsLast() : followingUser.name.asc().nullsLast(),
                    desc ? follow.id.desc() : follow.id.asc()
            };
        }

        return new OrderSpecifier[]{
                desc ? follow.createdAt.desc() : follow.createdAt.asc(),
                desc ? follow.id.desc() : follow.id.asc()
        };
    }

    private BooleanBuilder cursorCondition(CursorInfo cursor, String sortBy, String sortDirection) {
        BooleanBuilder builder = new BooleanBuilder();
        boolean desc = "DESCENDING".equalsIgnoreCase(sortDirection);

        if (desc) {
            builder.or(follow.createdAt.lt(cursor.createdAt())
                    .or(follow.createdAt.eq(cursor.createdAt())
                            .and(follow.id.lt(cursor.id()))));
        } else {
            builder.or(follow.createdAt.gt(cursor.createdAt())
                    .or(follow.createdAt.eq(cursor.createdAt())
                            .and(follow.id.gt(cursor.id()))));
        }

        return builder;
    }
}