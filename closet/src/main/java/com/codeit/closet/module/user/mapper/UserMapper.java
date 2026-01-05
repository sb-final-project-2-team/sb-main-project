package com.codeit.closet.module.user.mapper;

import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.dto.user.UserSummary;
import com.codeit.closet.module.user.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role")
    UserDTO toUserDTO(User user);

    @Mapping(target = "role", source = "role")
    List<UserDTO> toUserDTOs(List<User> users);

    // 차후에 LocationDTO랑 맵핑 해야함.
    @Mapping(target = "profileImageUrl", source = "binaryContent.fileUrl")
    @Mapping(target = "userId", source = "id")
    ProfileDTO toProfileDTO(User user);

    default UserSummary toUserSummary(User user) {
        if (user == null) return null;
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getBinaryContent() != null
                        ? user.getBinaryContent().getFileUrl()
                        : null
        );
    }
}
