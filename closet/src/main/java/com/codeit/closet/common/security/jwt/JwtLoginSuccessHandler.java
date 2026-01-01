package com.codeit.closet.common.security.jwt;

import com.codeit.closet.common.exception.ErrorResponse;
import com.codeit.closet.common.security.ClosetUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry<UUID> jwtRegistry;


  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    if (authentication.getPrincipal() instanceof ClosetUserDetails closetUserDetails) {
      try {
        String accessToken = jwtTokenProvider.generateAccessToken(closetUserDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(closetUserDetails);

        Cookie refreshTokenCookie = jwtTokenProvider.generateRefreshTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);

        JwtDTO jwtDTO = new JwtDTO(closetUserDetails.getUserDTO(), accessToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(jwtDTO));

        jwtRegistry.registerJwtInformation(
            new JwtInformation(closetUserDetails.getUserDTO(), accessToken, refreshToken));

      } catch (JOSEException e) {
        ErrorResponse errorResponse = new ErrorResponse(e,
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      }
    } else {
      ErrorResponse errorResponse = new ErrorResponse(
          new Exception("Authorization failure!"), HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

  }
}
