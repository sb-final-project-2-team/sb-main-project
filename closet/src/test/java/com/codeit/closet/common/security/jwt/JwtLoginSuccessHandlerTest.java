package com.codeit.closet.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class JwtLoginSuccessHandlerTest {

  @Mock
  JwtTokenProvider jwtTokenProvider;

  @Mock
  JwtRegistry<UUID> jwtRegistry;

  ObjectMapper objectMapper = new ObjectMapper();

  JwtLoginSuccessHandler handler;

  static final UUID USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");

  @BeforeEach
  void setUp() {
    handler = new JwtLoginSuccessHandler(
        objectMapper,
        jwtTokenProvider,
        jwtRegistry
    );
  }

  @Test
  @DisplayName("로그인 성공 시 access/refresh 토큰 발급 및 쿠키 설정")
  void onAuthenticationSuccess_success() throws Exception {
    // given
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Authentication authentication = mock(Authentication.class);

    StringWriter sw = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(sw));

    UserDTO userDTO = new UserDTO(
        USER_ID,
        null,
        "test@test.com",
        "tester",
        UserRole.USER,
        false
    );

    ClosetUserDetails userDetails =
        new ClosetUserDetails(userDTO, null, null, null);

    when(authentication.getPrincipal()).thenReturn(userDetails);

    when(jwtTokenProvider.generateAccessToken(userDetails))
        .thenReturn("access.token");

    when(jwtTokenProvider.generateRefreshToken(userDetails))
        .thenReturn("refresh.token");

    ResponseCookie refreshCookie =
        ResponseCookie.from("CLOSET_REFRESH_TOKEN", "refresh-token")
            .httpOnly(true)
            .secure(true)
            .domain(".otboo.store")
            .sameSite("None")
            .path("/")
            .build();

    when(jwtTokenProvider.generateRefreshTokenCookie(any()))
        .thenReturn(refreshCookie);

    // when
    handler.onAuthenticationSuccess(request, response, authentication);

    // then
    verify(response).setStatus(HttpServletResponse.SC_OK);
    verify(jwtRegistry).registerJwtInformation(any(JwtInformation.class));

    JwtDTO jwtDTO =
        objectMapper.readValue(sw.toString(), JwtDTO.class);

    assertThat(jwtDTO.accessToken()).isEqualTo("access.token");
    assertThat(jwtDTO.userDTO().email()).isEqualTo("test@test.com");
  }
  @Test
  @DisplayName("토큰 생성 중 예외 발생 시 500 반환")
  void onAuthenticationSuccess_jose_exception() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Authentication authentication = mock(Authentication.class);

    StringWriter sw = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(sw));

    UserDTO userDTO = new UserDTO(
        USER_ID,
        null,
        "test@test.com",
        "tester",
        UserRole.USER,
        false
    );

    ClosetUserDetails userDetails =
        new ClosetUserDetails(userDTO, null, null, null);

    when(authentication.getPrincipal()).thenReturn(userDetails);

    when(jwtTokenProvider.generateAccessToken(userDetails))
        .thenThrow(new JOSEException("sign error"));

    // when
    handler.onAuthenticationSuccess(request, response, authentication);

    // then
    verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    verify(jwtRegistry, never()).registerJwtInformation(any());
  }
  @Test
  @DisplayName("principal 타입이 잘못되면 401 반환")
  void onAuthenticationSuccess_invalid_principal() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Authentication authentication = mock(Authentication.class);

    StringWriter sw = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(sw));

    when(authentication.getPrincipal())
        .thenReturn("invalid-principal");

    // when
    handler.onAuthenticationSuccess(request, response, authentication);

    // then
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(jwtRegistry, never()).registerJwtInformation(any());
  }

}
