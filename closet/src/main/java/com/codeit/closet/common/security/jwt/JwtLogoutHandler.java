package com.codeit.closet.common.security.jwt;

import com.codeit.closet.common.security.ClosetUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry<UUID> jwtRegistry;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {
    ResponseCookie responseCookie = jwtTokenProvider.generateRefreshTokenExpirationCookie();
    response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    try {
      jwtRegistry.invalidateJwtInformationByUserId(
          ((ClosetUserDetails) authentication.getPrincipal()).getUserDTO().id());
    } catch (Exception e) {
      log.debug("JWT logout 성공 - refresh token 초기화 완료");
    }

  }
}
