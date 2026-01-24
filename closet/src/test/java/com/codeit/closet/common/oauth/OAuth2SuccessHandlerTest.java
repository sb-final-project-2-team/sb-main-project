package com.codeit.closet.common.oauth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.closet.common.security.jwt.JwtInformation;
import com.codeit.closet.common.security.jwt.JwtRegistry;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.mapper.UserMapper;
import com.codeit.closet.module.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

  @Mock
  UserRepository userRepository;
  @Mock
  JwtTokenProvider jwtTokenProvider;
  @Mock
  JwtRegistry<UUID> jwtRegistry;
  @Mock
  ObjectMapper objectMapper;
  @Mock
  UserMapper userMapper;

  OAuth2SuccessHandler handler;

  MockHttpServletRequest request;
  MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    handler = new OAuth2SuccessHandler(
        userRepository,
        jwtTokenProvider,
        jwtRegistry,
        objectMapper,
        userMapper
    );

    ReflectionTestUtils.setField(handler, "base_url", "http://localhost:3000");

    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  @DisplayName("Google OAuth2 로그인 성공 시 JWT 발급 및 리다이렉트")
  void success_google_login() throws Exception {
    // given
    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of(
            "email", "test@gmail.com",
            "sub", "google-123"
        ),
        "email"
    );

    OAuth2AuthenticationToken authentication =
        new OAuth2AuthenticationToken(
            oauth2User,
            oauth2User.getAuthorities(),
            "google"
        );

    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@gmail.com")
        .build();

    UserDTO userDTO = new UserDTO(UUID.fromString("00000000-0000-0000-0000-000000000000"),
        Instant.parse("2026-01-01T00:00:00Z"),
        "test@gmail.com",
        "테스트유저",
        UserRole.USER,
        false);

    when(userRepository.findByEmail("test@gmail.com"))
        .thenReturn(Optional.of(user));

    when(userMapper.toUserDTO(user))
        .thenReturn(userDTO);

    when(jwtTokenProvider.generateAccessToken(any()))
        .thenReturn("access-token");

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
    assertThat(response.getRedirectedUrl())
        .isEqualTo("http://localhost:3000");

    verify(jwtRegistry).registerJwtInformation(any(JwtInformation.class));
  }

  @Test
  @DisplayName("Kakao OAuth2 로그인 성공 (email 존재)")
  void success_kakao_login_with_email() throws Exception {
    // given
    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of(
            "id", 7777,
            "kakao_account", Map.of(
                "email", "kakao@test.com"
            )
        ),
        "id"
    );

    OAuth2AuthenticationToken authentication =
        new OAuth2AuthenticationToken(
            oauth2User,
            oauth2User.getAuthorities(),
            "kakao"
        );

    User user = User.builder()
        .id(UUID.randomUUID())
        .email("kakao@test.com")
        .build();

    UserDTO userDTO = new UserDTO(
        user.getId(),
        Instant.parse("2026-01-01T00:00:00Z"),
        "kakao@test.com",
        "카카오유저",
        UserRole.USER,
        false
    );

    when(userRepository.findByEmail("kakao@test.com"))
        .thenReturn(Optional.of(user));

    when(userMapper.toUserDTO(user))
        .thenReturn(userDTO);

    when(jwtTokenProvider.generateAccessToken(any()))
        .thenReturn("access-token");

    ResponseCookie refreshCookie =
        ResponseCookie.from("CLOSET_REFRESH_TOKEN", "refresh-token")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .build();

    when(jwtTokenProvider.generateRefreshTokenCookie(any()))
        .thenReturn(refreshCookie);

    // when
    handler.onAuthenticationSuccess(request, response, authentication);

    // then
    assertThat(response.getRedirectedUrl())
        .isEqualTo("http://localhost:3000");

    verify(jwtRegistry).registerJwtInformation(any(JwtInformation.class));
  }

  @Test
  @DisplayName("Kakao OAuth2 로그인 성공 (email 없음 → providerId로 조회)")
  void success_kakao_login_without_email() throws Exception {
    // given
    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of(
            "id", 7777,
            "kakao_account", Map.of() // email 없음
        ),
        "id"
    );

    OAuth2AuthenticationToken authentication =
        new OAuth2AuthenticationToken(
            oauth2User,
            oauth2User.getAuthorities(),
            "kakao"
        );

    User user = User.builder()
        .id(UUID.randomUUID())
        .providerId("7777")
        .build();

    UserDTO userDTO = new UserDTO(
        user.getId(),
        Instant.parse("2026-01-01T00:00:00Z"),
        null,
        "카카오유저",
        UserRole.USER,
        false
    );

    when(userRepository.findByProviderId("7777"))
        .thenReturn(Optional.of(user));

    when(userMapper.toUserDTO(user))
        .thenReturn(userDTO);

    when(jwtTokenProvider.generateAccessToken(any()))
        .thenReturn("access-token");

    ResponseCookie refreshCookie =
        ResponseCookie.from("CLOSET_REFRESH_TOKEN", "refresh-token")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .build();

    when(jwtTokenProvider.generateRefreshTokenCookie(any()))
        .thenReturn(refreshCookie);

    // when
    handler.onAuthenticationSuccess(request, response, authentication);

    // then
    assertThat(response.getRedirectedUrl())
        .isEqualTo("http://localhost:3000");

    verify(jwtRegistry).registerJwtInformation(any(JwtInformation.class));
  }

  @Test
  @DisplayName("지원하지 않는 OAuth2 provider면 예외 발생")
  void invalid_provider() {
    OAuth2AuthenticationToken authentication =
        new OAuth2AuthenticationToken(
            mock(OAuth2User.class),
            List.of(),
            "naver"
        );

    assertThatThrownBy(() ->
        handler.onAuthenticationSuccess(request, response, authentication)
    ).isInstanceOf(OAuth2AuthenticationException.class);
  }
}
