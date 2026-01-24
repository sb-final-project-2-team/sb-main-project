package com.codeit.closet.module.auth.controller;

import com.codeit.closet.common.security.jwt.JwtDTO;
import com.codeit.closet.common.security.jwt.JwtInformation;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.module.auth.dto.ResetPasswordRequest;
import com.codeit.closet.module.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDTO> createRefreshToken(
      @CookieValue(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
      HttpServletResponse response) {
    JwtInformation jwtInformation = authService.refreshToken(refreshToken);
    ResponseCookie refreshCookie  = jwtTokenProvider.generateRefreshTokenCookie(
        jwtInformation.getRefreshToken());

    response.addHeader("Set-Cookie", refreshCookie.toString());

    JwtDTO jwtDTO = new JwtDTO(jwtInformation.getUserDTO(), jwtInformation.getAccessToken());

    return ResponseEntity.status(HttpStatus.OK).body(jwtDTO);
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {

    authService.resetPassword(request);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
