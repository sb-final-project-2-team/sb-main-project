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
import jakarta.servlet.FilterChain;
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
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  JwtTokenProvider jwtTokenProvider;

  @Mock
  JwtRegistry<UUID> jwtRegistry;

  @Mock
  FilterChain filterChain;

  ObjectMapper objectMapper = new ObjectMapper();

  JwtAuthenticationFilter filter;

  static final String TOKEN = "abc.def.ghi";
  static final UUID USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter(objectMapper, jwtTokenProvider, jwtRegistry);
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("유효한 JWT가 있으면 SecurityContext에 인증 정보가 설정된다")
  void doFilter_valid_token_sets_authentication() throws Exception {
    // given
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getHeader("Authorization"))
        .thenReturn("Bearer " + TOKEN);

    when(jwtTokenProvider.validateAccessToken(TOKEN))
        .thenReturn(true);

    when(jwtRegistry.hasActiveJwtInformationByAccessToken(TOKEN))
        .thenReturn(true);

    UserDTO userDTO = new UserDTO(
        USER_ID,
        null,
        "test@test.com",
        "tester",
        UserRole.USER,
        false
    );

    JwtObject jwtObject = new JwtObject(
        null,
        null,
        userDTO,
        TOKEN
    );

    when(jwtTokenProvider.parseAccessToken(TOKEN))
        .thenReturn(jwtObject);

    // when
    filter.doFilterInternal(request, response, filterChain);

    // then
    var authentication =
        SecurityContextHolder.getContext().getAuthentication();

    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal())
        .isInstanceOf(ClosetUserDetails.class);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("유효하지 않은 JWT면 401을 반환하고 필터 체인을 중단한다")
  void doFilter_invalid_token_returns_401() throws Exception {
    // given
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getHeader("Authorization"))
        .thenReturn("Bearer " + TOKEN);

    when(jwtTokenProvider.validateAccessToken(TOKEN))
        .thenReturn(false);

    StringWriter sw = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(sw));

    // when
    filter.doFilterInternal(request, response, filterChain);

    // then
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(any(), any());

    assertThat(SecurityContextHolder.getContext().getAuthentication())
        .isNull();
  }

  @Test
  @DisplayName("Authorization 헤더가 없으면 필터는 그냥 통과한다")
  void doFilter_no_token_pass_through() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getHeader("Authorization")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication())
        .isNull();
  }

  @Test
  @DisplayName("login, refresh 경로는 필터를 타지 않는다")
  void should_not_filter_paths() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getServletPath()).thenReturn("/api/auth/login");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

}