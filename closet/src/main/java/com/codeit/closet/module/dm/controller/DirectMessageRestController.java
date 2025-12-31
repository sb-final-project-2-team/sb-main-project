package com.codeit.closet.module.dm.controller;

import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;
import com.codeit.closet.module.dm.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
public class DirectMessageRestController {

    private final DirectMessageService directMessageService;

    /*
    ws 쪽에서 [DM 저장 api] 호출 예정
    ws는 실시간 채팅만, api 쪽은 저장 및 조회로 기능 분리
    */

    // DM 저장
    @PostMapping
    public DirectMessageDTOCursorResponse createDirectMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID receiverId,
            @RequestParam String content
    ) {
        UUID senderId = jwt.getClaim("userId");
        return directMessageService.createDirectMessage(senderId, receiverId, content);
    }

    // 이전 DM 내역 조회
    @GetMapping
    public DirectMessageDTOCursorResponse getDirectMessages(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID userId, // 대화 상대 id
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam int limit
    ) {
        UUID myUserId = jwt.getClaim("userId");
        return directMessageService.getDirectMessages(myUserId, userId, cursor, idAfter, limit);
    }
}
