package com.codeit.closet.module.user.mapper;

import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role")
    UserDTO toUserDTO(User user);

    // 차후에 LocationDTO랑 맵핑 해야함.
    ProfileDTO toProfileDTO(User user);
}
