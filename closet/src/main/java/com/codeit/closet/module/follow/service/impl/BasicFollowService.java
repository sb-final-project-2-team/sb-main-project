package com.codeit.closet.module.follow.service.impl;

import com.codeit.closet.module.follow.dto.FollowCreateRequest;
import com.codeit.closet.module.follow.dto.FollowDTO;
import com.codeit.closet.module.follow.dto.FollowListResponse;
import com.codeit.closet.module.follow.dto.FollowSummaryDTO;
import com.codeit.closet.module.follow.entity.Follow;
import com.codeit.closet.module.follow.mapper.FollowMapper;
import com.codeit.closet.module.follow.repository.FollowRepository;
import com.codeit.closet.module.follow.service.FollowService;
import com.codeit.closet.module.user.entity.User;
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
    private final FollowMapper followMapper;

    @Override
    @Transactional
    public FollowDTO createFollow(
            FollowCreateRequest followCreateRequest
    ) {
        User followee = userRepository.findById(followCreateRequest.followeeId())
                .orElseThrow(() -> new IllegalArgumentException("팔로우할 사용자를 찾을 수 없습니다."));
        User follower = userRepository.findById(followCreateRequest.followerId())
                .orElseThrow(() -> new IllegalArgumentException("팔로워를 찾을 수 없습니다."));

        Follow follow = Follow.builder()
                .followee(followee)
                .follower(follower)
                .build();

        followRepository.save(follow);
        return followMapper.toDTO(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public FollowSummaryDTO findFollowSummary(
            UUID userId,
            UUID viewerId
    ) {
        if(!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("조회하려는 사용자를 찾을 수 없습니다.");
        }
        return followRepository.findFollowSummary(userId, viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public FollowListResponse findFollowingList(
            UUID followerId,
            String cursor,
            UUID idAfter,
            int limit,
            String sortBy,
            String sortDirection,
            String nameLike
    ) {
        if(!userRepository.existsById(followerId)) {
            throw new IllegalArgumentException("조회하려는 사용자를 찾을 수 없습니다.");
        }
        return followRepository.findFollowingList(
                followerId,
                cursor,
                idAfter,
                limit,
                sortBy,
                sortDirection,
                nameLike
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FollowListResponse findFollowerList(
            UUID followeeId,
            String cursor,
            UUID idAfter,
            int limit,
            String sortBy,
            String sortDirection,
            String nameLike
    ) {
        if(!userRepository.existsById(followeeId)) {
            throw new IllegalArgumentException("조회하려는 사용자를 찾을 수 없습니다.");
        }
        return followRepository.findFollowerList(
                followeeId,
                cursor,
                idAfter,
                limit,
                sortBy,
                sortDirection,
                nameLike
        );
    }

    @Override
    @Transactional
    public void deleteFollow(UUID followId) {
        if(!followRepository.existsById(followId)) {
            throw new IllegalArgumentException("팔로우 정보가 없습니다.");
        }
        followRepository.deleteById(followId);
    }
}
