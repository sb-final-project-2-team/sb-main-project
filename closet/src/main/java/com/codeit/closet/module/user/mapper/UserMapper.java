package com.codeit.closet.module.user.mapper;

import com.codeit.closet.module.user.dto.profile.ProfileDTO;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.dto.user.UserSummary;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.weather.mapper.WeatherMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
    WeatherMapper.class
})
public interface UserMapper {

    @Mapping(target = "role", source = "role")
    UserDTO toUserDTO(User user);

    @Mapping(target = "role", source = "role")
    List<UserDTO> toUserDTOs(List<User> users);

    @Mapping(target = "profileImageUrl", source = "binaryContent.fileUrl")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "location", source = "weather")
    ProfileDTO toProfileDTO(User user);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "profileImageUrl", source = "binaryContent.fileUrl")
    UserSummary toUserSummary(User user);
}
