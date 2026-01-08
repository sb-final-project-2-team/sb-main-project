package com.codeit.closet.module.follow.service.impl;

import com.codeit.closet.module.follow.dto.FollowCreateRequest;
import com.codeit.closet.module.follow.dto.FollowDTO;
import com.codeit.closet.module.follow.dto.FollowListResponse;
import com.codeit.closet.module.follow.dto.FollowSummaryDTO;
import com.codeit.closet.module.follow.repository.FollowRepository;
import com.codeit.closet.module.follow.service.FollowService;
import com.codeit.closet.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicFollowService implements FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FollowDTO createFollow(
            FollowCreateRequest followCreateRequest
    ) {
        UUID followeeId = followCreateRequest.followeeId();
        UUID followerId = followCreateRequest.followerId();

        if(!followRepository.existsById(followeeId)) {
            throw new IllegalArgumentException("followeeId not found");
        }
        if(!followRepository.existsById(followerId)) {
            throw new IllegalArgumentException("followerId not found");
        }

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
            int limit,
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
            int limit,
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
