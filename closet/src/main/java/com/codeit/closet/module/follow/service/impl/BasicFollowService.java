package com.codeit.closet.module.follow.service.impl;

import com.codeit.closet.module.follow.dto.FollowCreateRequest;
import com.codeit.closet.module.follow.dto.FollowDTO;
import com.codeit.closet.module.follow.dto.FollowListResponse;
import com.codeit.closet.module.follow.dto.FollowSummaryDTO;
import com.codeit.closet.module.follow.repository.FollowRepository;
import com.codeit.closet.module.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicFollowService implements FollowService {
    private final FollowRepository followRepository;

    @Override
    @Transactional
    public FollowDTO createFollow(
            FollowCreateRequest followCreateRequest
    ) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public FollowSummaryDTO findFollowSummary(
            UUID userId
    ) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public FollowListResponse findFollowingList(
            UUID followerId,
            String cursor,
            UUID idAfter,
            Integer limit,
            String nameLike
    ) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public FollowListResponse findFollowerList(
            UUID followeeId,
            String cursor,
            UUID idAfter,
            Integer limit,
            String nameLike
    ) {
        return null;
    }

    @Override
    @Transactional
    public void deleteFollow(
            UUID followId
    ) {

    }
}
