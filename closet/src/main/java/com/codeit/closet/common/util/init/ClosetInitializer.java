package com.codeit.closet.common.util.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClosetInitializer implements ApplicationRunner {

  private final InitAdminService initService;
  private final FeedInitService feedInitService;

  @Override
  public void run(ApplicationArguments args) {
    initService.initAdmin();        // 애플리케이션 실행 시 ADMIN 권한을 가진 어드민 계정이 초기화
    feedInitService.reindex();
  }
}