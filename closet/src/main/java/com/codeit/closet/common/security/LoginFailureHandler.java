package com.codeit.closet.common.security;

import com.codeit.closet.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {
    log.error("Authentication Failed : {}", exception.getMessage());

    // 응답값 생성
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    ErrorResponse errorResponse = new ErrorResponse(exception, HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
