package com.codeit.closet.module.follow.service;

import com.codeit.closet.module.follow.dto.FollowCreateRequest;
import com.codeit.closet.module.follow.dto.FollowDTO;
import com.codeit.closet.module.follow.dto.FollowListResponse;
import com.codeit.closet.module.follow.dto.FollowSummaryDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface FollowService {
    FollowDTO createFollow(FollowCreateRequest followCreateRequest);

    FollowSummaryDTO findFollowSummary(UUID userId, UUID viewerId);

    FollowListResponse findFollowingList(
            UUID followerId,
            String cursor,
            UUID idAfter,
            int limit,
            String sortBy,
            String sortDirection,
            String nameLike
    );

    FollowListResponse findFollowerList(
            UUID followeeId,
            String cursor,
            UUID idAfter,
            int limit,
            String sortBy,
            String sortDirection,
            String nameLike
    );

    void deleteFollow(UUID followId);
}
