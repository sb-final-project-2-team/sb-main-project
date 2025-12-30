package com.codeit.closet.module.follow.dto;

import java.util.UUID;

public record FollowSummaryDTO(
        UUID followeeId,
        long followerCount,
        long followingCount,
        boolean followedByMe,
        UUID followedByMeId,
        boolean followingMe
) {}