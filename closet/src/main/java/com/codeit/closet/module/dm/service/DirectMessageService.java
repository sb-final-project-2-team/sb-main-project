package com.codeit.closet.module.dm.service;

import com.codeit.closet.module.dm.dto.DirectMessageDTO;
import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;

import java.util.UUID;

public interface DirectMessageService {
    DirectMessageDTO create(UUID senderId, UUID receiverId, String content);

    DirectMessageDTOCursorResponse findDirectMessages(UUID myUserId, UUID userId, String cursor, UUID idAfter, int limit);
}
