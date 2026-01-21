package com.codeit.closet.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class Http401UnauthorizedEntryPointTest {

  ObjectMapper objectMapper = new ObjectMapper();

  Http401UnauthorizedEntryPoint entryPoint;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    entryPoint = new Http401UnauthorizedEntryPoint(objectMapper);
  }

  @Test
  @DisplayName("인증되지 않은 요청 시 401 JSON 응답을 반환한다")
  void unauthorized_entry_point_returns_401_json() throws Exception {
    // given
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AuthenticationException authException = new BadCredentialsException("인증 실패");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);

    when(response.getWriter()).thenReturn(writer);

    // when
    entryPoint.commence(request, response, authException);

    // then
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

    String responseBody = stringWriter.toString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);

    assertThat(jsonNode.get("status").asInt())
        .isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(jsonNode.get("message").asText())
        .contains("인증 실패");
  }
}