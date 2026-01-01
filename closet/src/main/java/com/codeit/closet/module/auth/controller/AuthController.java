package com.codeit.closet.module.auth.controller;

import com.codeit.closet.common.security.jwt.JwtDTO;
import com.codeit.closet.common.security.jwt.JwtInformation;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.module.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String token = csrfToken.getToken();
    log.info("csrfToken: {}", token);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDTO> createRefreshToken(
      @CookieValue(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
      HttpServletResponse response) {
    log.info("Refresh Token: {}", refreshToken);
    JwtInformation jwtInformation = authService.refreshToken(refreshToken);
    Cookie cookie = jwtTokenProvider.generateRefreshTokenCookie(jwtInformation.getRefreshToken());
    response.addCookie(cookie);

    JwtDTO jwtDTO = new JwtDTO(jwtInformation.getUserDTO(), jwtInformation.getAccessToken());

    return ResponseEntity.status(HttpStatus.OK).body(jwtDTO);
  }
  // 비밀번호 초기화
  //@PostMapping("/reset-password")
}
