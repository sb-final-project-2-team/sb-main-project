package com.codeit.closet.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.closet.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

class LoginFailureHandlerTest {

  ObjectMapper objectMapper;
  LoginFailureHandler failureHandler;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    failureHandler = new LoginFailureHandler(objectMapper);
  }

  @Test
  @DisplayName("로그인 실패 시 401 JSON 응답을 반환한다")
  void login_failure_returns_401_json() throws Exception {
    // given
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AuthenticationException exception =
        new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);

    when(response.getWriter()).thenReturn(writer);

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);

    // then
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setCharacterEncoding("UTF-8");

    String responseBody = stringWriter.toString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);

    assertThat(jsonNode.get("status").asInt())
        .isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(jsonNode.get("message").asText())
        .contains("아이디 또는 비밀번호가 올바르지 않습니다");
  }
}
