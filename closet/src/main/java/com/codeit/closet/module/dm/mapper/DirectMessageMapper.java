package com.codeit.closet.module.dm.mapper;

import com.codeit.closet.module.dm.dto.DirectMessageDTO;
import com.codeit.closet.module.dm.entity.DirectMessage;
import com.codeit.closet.module.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = UserMapper.class
)
public interface DirectMessageMapper {

    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "receiver", source = "receiver")
    DirectMessageDTO toDTO(DirectMessage dm);

    List<DirectMessageDTO> toDTOs(List<DirectMessage> messages);
}