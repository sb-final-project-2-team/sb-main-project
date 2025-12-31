package com.codeit.closet.module.dm.service.impl;

import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;
import com.codeit.closet.module.dm.repository.DirectMessageRepository;
import com.codeit.closet.module.dm.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicDirectMessageService implements DirectMessageService {
    private final DirectMessageRepository directMessageRepository;

    @Override
    @Transactional
    public DirectMessageDTOCursorResponse createDirectMessage(UUID senderId, UUID receiverId, String content) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public DirectMessageDTOCursorResponse getDirectMessages(UUID myUserId, UUID userId, String cursor, UUID idAfter, int limit) {
        return null;
    }
}
