package com.codeit.closet.module.dm.dto;

import java.util.UUID;

// ws 서버에서 보냄
public record DirectMessageSaveRequest(
        UUID receiverId,
        UUID senderId,
        String content
) {}