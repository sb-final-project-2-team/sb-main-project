package com.codeit.closet.module.dm.controller;

import com.codeit.closet.module.dm.dto.DirectMessageDTO;
import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;
import com.codeit.closet.module.dm.dto.DirectMessageSaveRequest;
import com.codeit.closet.module.dm.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
public class DirectMessageRestController {

    private final DirectMessageService directMessageService;

    // DM 저장
    @PostMapping
    public DirectMessageDTO createDirectMessage(
//            @AuthenticationPrincipal Jwt jwt,
            @RequestBody DirectMessageSaveRequest directMessageSaveRequest
            ) {
//        UUID senderId = jwt.getClaim("userId"); senderId는 jwt로 분리 예정
        log.info("[Controller] DM 저장 api 호출");
        return directMessageService.create(
                directMessageSaveRequest.senderId(),
                directMessageSaveRequest.receiverId(),
                directMessageSaveRequest.content()
        );
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
        return directMessageService.findDirectMessages(myUserId, userId, cursor, idAfter, limit);
    }
}
