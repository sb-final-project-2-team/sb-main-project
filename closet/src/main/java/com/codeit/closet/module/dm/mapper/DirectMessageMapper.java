package com.codeit.closet.module.dm.mapper;

import com.codeit.closet.module.dm.dto.DirectMessageDTO;
import com.codeit.closet.module.dm.entity.DirectMessage;
import com.codeit.closet.module.user.dto.user.UserSummary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DirectMessageMapper {

    default DirectMessageDTO toDirectMessageDTO(
            DirectMessage dm,
            UserSummary sender,
            UserSummary receiver
    ) {
        return new DirectMessageDTO(
                dm.getId(),
                dm.getCreatedAt(),
                sender,
                receiver,
                dm.getContent()
        );
    }
}