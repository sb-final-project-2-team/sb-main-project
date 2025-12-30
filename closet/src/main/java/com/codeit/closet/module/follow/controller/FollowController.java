package com.codeit.closet.module.follow.controller;

import com.codeit.closet.module.follow.dto.FollowCreateRequest;
import com.codeit.closet.module.follow.dto.FollowDTO;
import com.codeit.closet.module.follow.dto.FollowListResponse;
import com.codeit.closet.module.follow.dto.FollowSummaryDTO;
import com.codeit.closet.module.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우 생성
    @PostMapping
    public ResponseEntity<FollowDTO> create(
            @RequestBody FollowCreateRequest followCreateRequest
    ) {
        FollowDTO result = followService.create(followCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 팔로우 요약 정보 조회
    @GetMapping("/summary")
    public ResponseEntity<FollowSummaryDTO> summary(
            @RequestParam UUID userId
    ) {
        FollowSummaryDTO result = followService.findFollowSummary(userId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 팔로잉 목록 조회
    @GetMapping("/followings")
    public ResponseEntity<FollowListResponse> followings(
            @RequestParam UUID followerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam int limit, // 필수니까 null 불가능이라 Integer -> int
            @RequestParam(required = false) String nameLike
    ) {
        FollowListResponse result = followService.findFollowingList(
                followerId,
                cursor,
                idAfter,
                limit,
                nameLike
        );
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    // 팔로워 목록 조회
    @GetMapping("/followers")
    public  ResponseEntity<FollowListResponse> followers(
            @RequestParam UUID followeeId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam int limit, // 필수니까 null 불가능이라 Integer -> int
            @RequestParam(required = false) String nameLike
    ) {
        FollowListResponse result = followService.findFollowerList(
                followeeId,
                cursor,
                idAfter,
                limit,
                nameLike
        );
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 팔로우 취소
    @DeleteMapping("/{followId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID followId
    ) {
        followService.delete(followId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
