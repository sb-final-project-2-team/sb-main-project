package com.codeit.closet.common.security;

import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class InitAdminService {

    @Value("${closet.admin.username}")
    private String adminUsername;
    @Value("${closet.admin.password}")
    private String adminPassword;
    @Value("${closet.admin.email}")
    private String adminEmail;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void initAdmin() {
        if (userRepository.existsByEmail(adminEmail) || userRepository.existsByName(adminUsername)) {
            log.warn("이미 관리자가 존재합니다.");
            return;
        }

        User admin = User.builder()
                .name(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(UserRole.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("관리자가 초기화되었습니다.");
    }

}
