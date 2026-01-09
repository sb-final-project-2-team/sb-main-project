package com.codeit.closet.module.dm.controller;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.dm.dto.DirectMessageDTO;
import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;
import com.codeit.closet.module.dm.dto.DirectMessageSaveRequest;
import com.codeit.closet.module.dm.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal ClosetUserDetails userDetails,
            @RequestBody DirectMessageSaveRequest directMessageSaveRequest
    ) {
        log.info("[Controller] DM 저장 api 호출");

        UUID senderId = userDetails.getUserDTO().id();
        UUID receiverId = directMessageSaveRequest.receiverId();

        return directMessageService.create(
                senderId,
                receiverId,
                directMessageSaveRequest.content()
        );
    }

    // 이전 DM 내역 조회
    @GetMapping
    public DirectMessageDTOCursorResponse getDirectMessages(
            @AuthenticationPrincipal ClosetUserDetails userDetails,
            @RequestParam("userId") UUID receiverId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam int limit
    ) {
        log.info("[Controller] 이전 DM 내역 조회 호출");
        UUID senderId = userDetails.getUserDTO().id();
        return directMessageService.findDirectMessages(senderId, receiverId, cursor, idAfter, limit);
    }
}
