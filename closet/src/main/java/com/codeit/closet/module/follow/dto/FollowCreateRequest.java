package com.codeit.closet.module.follow.dto;

import java.util.UUID;

public record FollowCreateRequest(
        UUID followeeId,
        UUID followerId
) {}
