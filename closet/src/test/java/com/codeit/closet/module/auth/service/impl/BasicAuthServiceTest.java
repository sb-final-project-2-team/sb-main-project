package com.codeit.closet.module.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.codeit.closet.common.mail.service.MailService;
import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.common.security.jwt.JwtInformation;
import com.codeit.closet.common.security.jwt.JwtRegistry;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.module.auth.dto.ResetPasswordRequest;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

  @InjectMocks
  private BasicAuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MailService mailService;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private JwtRegistry<UUID> jwtRegistry;

  @Mock
  private UserDetailsService userDetailsService;


  @Test
  @DisplayName("refreshToken 성공 시 access/refresh 토큰 재발급")
  void refreshToken_success() throws JOSEException {
    // given
    String oldRefreshToken = "old-refresh-token";
    String newAccessToken = "new-access-token";
    String newRefreshToken = "new-refresh-token";
    String email = "test@test.com";

    ClosetUserDetails userDetails = mock(ClosetUserDetails.class);

    given(jwtTokenProvider.validateRefreshToken(oldRefreshToken)).willReturn(true);
    given(jwtRegistry.hasActiveJwtInformationByRefreshToken(oldRefreshToken)).willReturn(true);
    given(jwtTokenProvider.getEmailFromToken(oldRefreshToken)).willReturn(email);
    given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);
    given(jwtTokenProvider.generateAccessToken(userDetails)).willReturn(newAccessToken);
    given(jwtTokenProvider.generateRefreshToken(userDetails)).willReturn(newRefreshToken);
    given(userDetails.getUserDTO()).willReturn(null); // DTO 검증이 목적이 아니라면 null도 OK

    // when
    JwtInformation result = authService.refreshToken(oldRefreshToken);

    // then
    assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
    assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);

    then(jwtRegistry).should()
        .rotateJwtInformation(eq(oldRefreshToken), any(JwtInformation.class));
  }

  @Test
  @DisplayName("refreshToken 검증 실패 시 예외 발생")
  void refreshToken_invalid() {
    // given
    String refreshToken = "invalid-token";
    given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(false);

    // expect
    assertThatThrownBy(() -> authService.refreshToken(refreshToken))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("비밀번호 초기화 성공")
  void resetPassword_success() {
    // given
    String email = "test@test.com";
    String encodedPassword = "encoded-password";

    User user = mock(User.class);
    ResetPasswordRequest request = new ResetPasswordRequest(email);

    given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
    given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);
    given(user.getEmail()).willReturn(email);

    // when
    authService.resetPassword(request);

    // then
    then(user).should().updateTempPassword(encodedPassword);
    then(mailService).should().sendResetPasswordMail(eq(email), anyString());
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 비밀번호 초기화 시 예외 발생")
  void resetPassword_userNotFound() {
    // given
    ResetPasswordRequest request = new ResetPasswordRequest("notfound@test.com");
    given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

    // expect
    assertThatThrownBy(() -> authService.resetPassword(request))
        .isInstanceOf(NoSuchElementException.class);
  }
}