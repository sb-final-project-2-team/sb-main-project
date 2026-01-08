package com.codeit.closet.module.dm.service.impl;

import com.codeit.closet.module.dm.dto.DirectMessageDTO;
import com.codeit.closet.module.dm.dto.DirectMessageDTOCursorResponse;
import com.codeit.closet.module.dm.entity.DirectMessage;
import com.codeit.closet.module.dm.mapper.DirectMessageMapper;
import com.codeit.closet.module.dm.repository.DirectMessageRepository;
import com.codeit.closet.module.dm.service.DirectMessageService;
import com.codeit.closet.module.dm.util.DmKeyUtil;
import com.codeit.closet.module.user.entity.User;
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

    @Override
    @Transactional
    public DirectMessageDTO create(
            UUID senderId,
            UUID receiverId,
            String content
    ) {
        log.info("[Service] DM 저장 api 호출");

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("송신자를 찾을 수 없습니다."));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

        String dmKey = DmKeyUtil.of(senderId, receiverId);

        DirectMessage directMessage = DirectMessage.builder()
                .dmKey(dmKey)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        directMessageRepository.save(directMessage);

        log.info("[Service] sender:{} receiver:{}", senderId, receiverId);

        return directMessageMapper.toDTO(directMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public DirectMessageDTOCursorResponse findDirectMessages(
            UUID senderId,
            UUID receiverId,
            String cursor,
            UUID idAfter,
            int limit
    ) {
        log.info("[Service] 이전 DM 내역 조회 호출");

        if(!userRepository.existsById(senderId)) {
            throw new IllegalArgumentException("송신자를 찾을 수 없습니다.");
        }
        if(!userRepository.existsById(receiverId)) {
            throw new IllegalArgumentException("수신자를 찾을 수 없습니다.");
        }
        return directMessageRepository.findDirectMessagesByDmKey(
                DmKeyUtil.of(senderId, receiverId), cursor, limit
        );
    }
}
