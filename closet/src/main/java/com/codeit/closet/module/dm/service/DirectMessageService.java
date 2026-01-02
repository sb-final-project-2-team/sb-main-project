package com.codeit.closet.module.dm.service;

import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;

import java.util.UUID;

public interface DirectMessageService {
    DirectMessageDTOCursorResponse createDirectMessage(UUID senderId, UUID receiverId, String content);

    DirectMessageDTOCursorResponse getDirectMessages(UUID myUserId, UUID userId, String cursor, UUID idAfter, int limit);
}
