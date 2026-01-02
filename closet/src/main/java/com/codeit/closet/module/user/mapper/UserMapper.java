package com.codeit.closet.module.user.mapper;

import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.dto.user.UserSummary;
import com.codeit.closet.module.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);

    // 차후에 LocationDTO랑 맵핑 해야함.
    ProfileDTO toProfileDTO(User user);

    default UserSummary toUserSummary(User user) {
        if (user == null) return null;
        return new UserSummary(
                user.getId(),
                user.getName(),
                "url"
//                user.getBinaryContent().getFileUrl() //binaryContent 연결 후 사용
        );
    }
}
