package com.codeit.closet.module.follow.repository;

import com.codeit.closet.module.follow.dto.FollowListResponse;

import java.util.UUID;

public interface FollowListQueryRepository {

    FollowListResponse findFollowingList(
            UUID followerId,
            String cursor,
            UUID idAfter,
            Integer limit,
            String sortBy,
            String sortDirection,
            String nameLike
    );

    FollowListResponse findFollowerList(
            UUID followeeId,
            String cursor,
            UUID idAfter,
            Integer limit,
            String sortBy,
            String sortDirection,
            String nameLike
    );
}
