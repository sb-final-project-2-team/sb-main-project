package com.codeit.closet.common.security.jwt;

import com.codeit.closet.module.user.dto.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

public record JwtDTO(
    @JsonProperty("userDto")  // 프론트에선 userDto로 받음
    UserDTO userDTO,
    String accessToken
) {

}
