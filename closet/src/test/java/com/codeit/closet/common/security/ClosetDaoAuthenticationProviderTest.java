package com.codeit.closet.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ClosetDaoAuthenticationProviderTest {

  @Mock
  UserDetailsService userDetailsService;

  @Mock
  PasswordEncoder passwordEncoder;

  @Mock
  RoleHierarchy roleHierarchy;

  @Mock
  UserRepository userRepository;

  ClosetDaoAuthenticationProvider provider;

  @BeforeEach
  void setup() {
    provider = new ClosetDaoAuthenticationProvider(userDetailsService, passwordEncoder,
        roleHierarchy, userRepository);
  }


  @Test
  @DisplayName("일반_비밀번호_성공")
  void Success_Password() {
    ClosetUserDetails user =
        mockUserWithPassword("ENC_PW", "ENC_TEMP");

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken("user", "raw");

    when(passwordEncoder.matches("raw", "ENC_PW")).thenReturn(true);

    assertDoesNotThrow(() ->
        provider.additionalAuthenticationChecks(user, auth)
    );

    verify(userRepository)
        .clearTempPassword(UUID.fromString("00000000-0000-0000-0000-0000000000"));
  }

  @Test
  @DisplayName("임시_비밀번호_성공")
  void Success_Temp_Password() {
    ClosetUserDetails user = mockUser(
        "ENC_PW",
        "ENC_TEMP",
        Instant.now().plusSeconds(60)
    );

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken("user", "raw");

    when(passwordEncoder.matches("raw", "ENC_PW")).thenReturn(false);
    when(passwordEncoder.matches("raw", "ENC_TEMP")).thenReturn(true);

    assertDoesNotThrow(() ->
        provider.additionalAuthenticationChecks(user, auth)
    );

    verify(userRepository).clearTempPassword(UUID.fromString("00000000-0000-0000-0000-0000000000"));
  }

  @Test
  @DisplayName("임시_비밀번호_만료")
  void Expired_Temp_Password() {
    ClosetUserDetails user = mockUser(
        "ENC_PW",
        "ENC_TEMP",
        Instant.now().minusSeconds(10)
    );

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken("user", "raw");

    when(passwordEncoder.matches(any(), any())).thenReturn(false);

    BadCredentialsException ex = assertThrows(
        BadCredentialsException.class,
        () -> provider.additionalAuthenticationChecks(user, auth)
    );

    assertThat(ex.getMessage()).contains("임시 비밀번호가 만료됨");
    verify(userRepository).clearTempPassword(UUID.fromString("00000000-0000-0000-0000-0000000000"));
  }

  @Test
  @DisplayName("비밀번호_불일치")
  void Rejected_Password() {
    ClosetUserDetails user =
        mockUserWithOnlyPassword("ENC_PW");

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken("user", "raw");

    when(passwordEncoder.matches("raw", "ENC_PW"))
        .thenReturn(false);

    BadCredentialsException ex = assertThrows(
        BadCredentialsException.class,
        () -> provider.additionalAuthenticationChecks(user, auth)
    );

    assertThat(ex.getMessage())
        .contains("비밀번호가 일치하지 않습니다");
  }

  private ClosetUserDetails mockUserWithOnlyPassword(String password) {
    ClosetUserDetails user = mock(ClosetUserDetails.class);

    when(user.getPassword()).thenReturn(password);

    return user;
  }

  private ClosetUserDetails mockUserWithPassword(String password, String tempPassword) {
    ClosetUserDetails user = mock(ClosetUserDetails.class);

    when(user.getPassword()).thenReturn(password);
    when(user.getTempPassword()).thenReturn(tempPassword);

    UserDTO dto = mock(UserDTO.class);
    when(dto.id()).thenReturn(UUID.fromString("00000000-0000-0000-0000-0000000000"));
    when(user.getUserDTO()).thenReturn(dto);

    return user;
  }

  private ClosetUserDetails mockUser(
      String password,
      String tempPassword,
      Instant expiredAt
  ) {
    ClosetUserDetails user = mock(ClosetUserDetails.class);

    when(user.getPassword()).thenReturn(password);
    when(user.getTempPassword()).thenReturn(tempPassword);
    when(user.getTempPasswordExpiredAt()).thenReturn(expiredAt);

    UserDTO dto = mock(UserDTO.class);
    when(dto.id()).thenReturn(UUID.fromString("00000000-0000-0000-0000-0000000000"));
    when(user.getUserDTO()).thenReturn(dto);

    return user;
  }
}