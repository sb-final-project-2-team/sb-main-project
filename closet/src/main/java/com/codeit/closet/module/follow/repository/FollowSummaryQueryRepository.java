package com.codeit.closet.module.follow.repository;

import com.codeit.closet.module.follow.dto.FollowSummaryDTO;

import java.util.UUID;

public interface FollowSummaryQueryRepository {
    FollowSummaryDTO findFollowSummary(UUID followeeId, UUID viewerId);
}
