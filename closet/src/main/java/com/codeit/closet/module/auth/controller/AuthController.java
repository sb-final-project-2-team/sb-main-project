package com.codeit.closet.module.auth.controller;

import com.codeit.closet.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  // private final AuthService authService;

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String token = csrfToken.getToken();
    log.info("csrfToken: {}", token);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
  // 토큰 재발급
  //@PostMapping("/refresh")

  // 비밀번호 초기화
  //@PostMapping("/reset-password")
}
