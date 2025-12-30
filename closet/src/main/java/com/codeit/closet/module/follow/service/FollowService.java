package com.codeit.closet.module.follow.service;

import com.codeit.closet.module.follow.dto.FollowCreateRequest;
import com.codeit.closet.module.follow.dto.FollowDto;
import com.codeit.closet.module.follow.dto.FollowListResponse;
import com.codeit.closet.module.follow.dto.FollowSummaryDto;
import com.codeit.closet.module.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;

    @Transactional
    public FollowDto create(
            FollowCreateRequest followCreateRequest
    ) {
        return null;
    }

    @Transactional(readOnly = true)
    public FollowSummaryDto findFollowSummary(
            UUID userId
    ) {
        return null;
    }

    @Transactional(readOnly = true)
    public FollowListResponse findFollowingList(
            UUID followerId,
            String cursor,
            UUID idAfter,
            int limit,
            String nameLike
    ) {
        return null;
    }

    @Transactional(readOnly = true)
    public FollowListResponse findFollowerList(
            UUID followeeId,
            String cursor,
            UUID idAfter,
            int limit,
            String nameLike
    ) {
        return null;
    }

    @Transactional
    public void delete(
            UUID followId
    ) {

    }
}
