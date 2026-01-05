package com.codeit.closet.module.dm.service.impl;

import com.codeit.closet.module.dm.dto.DirectMessageDTO;
import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;
import com.codeit.closet.module.dm.entity.DirectMessage;
import com.codeit.closet.module.dm.mapper.DirectMessageMapper;
import com.codeit.closet.module.dm.repository.DirectMessageRepository;
import com.codeit.closet.module.dm.service.DirectMessageService;
import com.codeit.closet.module.dm.util.DmKeyUtil;
import com.codeit.closet.module.user.dto.user.UserSummary;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.mapper.UserMapper;
import com.codeit.closet.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicDirectMessageService implements DirectMessageService {
    private final DirectMessageRepository directMessageRepository;
    private final DirectMessageMapper directMessageMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public DirectMessageDTO create(UUID senderId, UUID receiverId, String content) {
        log.info("[Service] DM 저장 api 호출");

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("receiver not found"));

        String dmKey = DmKeyUtil.of(senderId, receiverId);

        DirectMessage directMessage = DirectMessage.builder()
                .dmKey(dmKey)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .build();

        directMessageRepository.save(directMessage);

        UserSummary senderSummary = userMapper.toUserSummary(sender);
        UserSummary receiverSummary = userMapper.toUserSummary(receiver);

        log.info("[Service] sender:{} receiver:{}", senderId, receiverId);

        return directMessageMapper.toDirectMessageDTO(
                directMessage, senderSummary, receiverSummary
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DirectMessageDTOCursorResponse findDirectMessages(UUID myUserId, UUID userId, String cursor, UUID idAfter, int limit) {
        return null;
    }
}
