package com.codeit.closet.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

  private final InitAdminService initService;

  @Override
  public void run(ApplicationArguments args) {
    initService.initAdmin();        // 애플리케이션 실행 시 ADMIN 권한을 가진 어드민 계정이 초기화
  }
}