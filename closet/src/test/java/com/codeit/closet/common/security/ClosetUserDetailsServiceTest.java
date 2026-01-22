package com.codeit.closet.common.security;

import static org.junit.jupiter.api.Assertions.*;

import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.mapper.UserMapper;
import com.codeit.closet.module.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class ClosetUserDetailsServiceTest {

  @Mock
  UserRepository userRepository;

  @Mock
  UserMapper userMapper;

  ClosetUserDetailsService userDetailsService;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
  @BeforeEach
  void setUp() {
    userDetailsService =
        new ClosetUserDetailsService(userRepository, userMapper);
  }

  @Test
  @DisplayName("이메일로 유저를 조회하면 UserDetails를 반환한다")
  void loadUserByUsername_success() {
    // given
    String email = "test@test.com";

    User user = mock(User.class);
    when(user.getPassword()).thenReturn("ENC_PASSWORD");
    when(user.getTempPassword()).thenReturn("TEMP_PW");
    when(user.getTempPasswordExpiredAt())
        .thenReturn(Instant.parse("2026-01-01T00:00:00Z"));

    UserDTO userDTO = new UserDTO(
        USER_ID,
        Instant.parse("2026-01-01T00:00:00Z"),
        email,
        "테스트유저",
        UserRole.USER,
        false
    );

    when(userRepository.findByEmail(email))
        .thenReturn(Optional.of(user));
    when(userMapper.toUserDTO(user))
        .thenReturn(userDTO);

    // when
    UserDetails result =
        userDetailsService.loadUserByUsername(email);

    // then
    assertThat(result).isInstanceOf(ClosetUserDetails.class);

    ClosetUserDetails details = (ClosetUserDetails) result;

    assertThat(details.getUsername()).isEqualTo(email);
    assertThat(details.getPassword()).isEqualTo("ENC_PASSWORD");
    assertThat(details.getTempPassword()).isEqualTo("TEMP_PW");
    assertThat(details.getTempPasswordExpiredAt())
        .isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  @DisplayName("존재하지 않는 이메일이면 UsernameNotFoundException을 던진다")
  void loadUserByUsername_user_not_found() {
    // given
    String email = "notfound@test.com";

    when(userRepository.findByEmail(email))
        .thenReturn(Optional.empty());

    // when & then
    UsernameNotFoundException ex = assertThrows(
        UsernameNotFoundException.class,
        () -> userDetailsService.loadUserByUsername(email)
    );

    assertThat(ex.getMessage())
        .contains("유저를 찾을 수 없습니다.")
        .contains(email);

    verify(userMapper, never()).toUserDTO(any());
  }
}