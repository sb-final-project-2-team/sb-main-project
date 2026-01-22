package com.codeit.closet.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.UserRole;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class ClosetUserDetailsTest {

  private UserDTO userDTO(UserRole role) {
    return new UserDTO(
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        Instant.parse("2026-01-01T00:00:00Z"),
        "test@test.com",
        "테스트유저",
        role,
        false
    );
  }


  @Test
  @DisplayName("UserDetails는 email을 username으로 사용한다")
  void getUsername_returns_email() {
    // given
    UserDTO userDTO = userDTO(UserRole.USER);
    ClosetUserDetails userDetails =
        new ClosetUserDetails(userDTO, "ENC_PASSWORD", null, null);

    // when & then
    assertThat(userDetails.getUsername())
        .isEqualTo("test@test.com");
    assertThat(userDetails.getPassword())
        .isEqualTo("ENC_PASSWORD");
  }

  @Test
  @DisplayName("UserDetails는 ROLE_ 접두사가 붙은 권한을 반환한다")
  void getAuthorities_returns_role_authority() {
    // given
    UserDTO userDTO = userDTO(UserRole.ADMIN);
    ClosetUserDetails userDetails =
        new ClosetUserDetails(userDTO, "PW", null, null);

    // when
    Collection<? extends GrantedAuthority> authorities =
        userDetails.getAuthorities();

    // then
    assertThat(authorities).hasSize(1);
    assertThat(authorities.iterator().next().getAuthority())
        .isEqualTo("ROLE_ADMIN");
  }

  @Test
  @DisplayName("임시 비밀번호와 만료 시간은 그대로 보존된다")
  void temp_password_fields_are_preserved() {
    // given
    Instant expiredAt = Instant.parse("2026-01-01T01:00:00Z");
    UserDTO userDTO = userDTO(UserRole.USER);

    ClosetUserDetails userDetails =
        new ClosetUserDetails(
            userDTO,
            "PW",
            "TEMP_PW",
            expiredAt
        );

    // then
    assertThat(userDetails.getTempPassword())
        .isEqualTo("TEMP_PW");
    assertThat(userDetails.getTempPasswordExpiredAt())
        .isEqualTo(expiredAt);
  }

  @Test
  @DisplayName("UserDTO가 같으면 UserDetails는 동등하다")
  void equals_and_hashcode_based_on_userDTO() {
    // given
    UserDTO sameUserDTO = userDTO(UserRole.USER);

    ClosetUserDetails user1 =
        new ClosetUserDetails(sameUserDTO, "PW1", null, null);
    ClosetUserDetails user2 =
        new ClosetUserDetails(sameUserDTO, "PW2", "TEMP", Instant.now());

    // then
    assertThat(user1).isEqualTo(user2);
    assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
  }
}