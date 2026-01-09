package com.codeit.closet.module.follow.mapper;

import com.codeit.closet.module.follow.dto.FollowDTO;
import com.codeit.closet.module.follow.entity.Follow;
import com.codeit.closet.module.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = UserMapper.class
)
public interface FollowMapper {

    @Mapping(target = "followee", source = "followee")
    @Mapping(target = "follower", source = "follower")
    FollowDTO toDTO(Follow follow);

    List<FollowDTO> toFollowDTOs(List<Follow> follows);
}
