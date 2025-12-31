package com.codeit.closet.module.follow.dto;

import com.codeit.closet.module.user.dto.user.UserSummary;

import java.util.UUID;

public record FollowDTO(
        UUID id,
        UserSummary followee,
        UserSummary follower
) {}