package com.codeit.closet.module.follow.repository.impl;

import com.codeit.closet.module.follow.dto.FollowSummaryDTO;
import com.codeit.closet.module.follow.entity.Follow;
import com.codeit.closet.module.follow.entity.QFollow;
import com.codeit.closet.module.follow.repository.FollowSummaryQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FollowSummaryQueryRepositoryImpl implements FollowSummaryQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QFollow follow = QFollow.follow;

    @Override
    public FollowSummaryDTO findFollowSummary(UUID followeeId, UUID viewerId) {

        Long followerCount = queryFactory
                .select(follow.count())
                .from(follow)
                .where(follow.followee.id.eq(followeeId))
                .fetchOne();


        Long followingCount = queryFactory
                .select(follow.count())
                .from(follow)
                .where(follow.follower.id.eq(followeeId))
                .fetchOne();

        Follow myFollow = queryFactory
                .selectFrom(follow)
                .where(follow.follower.id.eq(viewerId)
                        .and(follow.followee.id.eq(followeeId)))
                .fetchOne();

        boolean followedByMe = (myFollow != null);
        UUID followedByMeId = (myFollow != null) ? myFollow.getId() : null;

        boolean followingMe = queryFactory
                .selectOne()
                .from(follow)
                .where(follow.follower.id.eq(followeeId)
                        .and(follow.followee.id.eq(viewerId)))
                .fetchFirst() != null;

        long safeFollowerCount = (followerCount == null) ? 0L : followerCount;
        long safeFollowingCount = (followingCount == null) ? 0L : followingCount;

        return new FollowSummaryDTO(
                followeeId,
                safeFollowerCount,
                safeFollowingCount,
                followedByMe,
                followedByMeId,
                followingMe
        );
    }
}
