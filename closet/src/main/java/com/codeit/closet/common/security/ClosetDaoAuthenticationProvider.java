package com.codeit.closet.common.security;

import com.codeit.closet.module.user.repository.UserRepository;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ClosetDaoAuthenticationProvider extends DaoAuthenticationProvider {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public ClosetDaoAuthenticationProvider(UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder,
      RoleHierarchy roleHierarchy, UserRepository userRepository) {

    super(userDetailsService);
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;

    setPasswordEncoder(passwordEncoder);
    setAuthoritiesMapper(new RoleHierarchyAuthoritiesMapper(roleHierarchy));
  }

  @Override
  @Transactional
  protected void additionalAuthenticationChecks(UserDetails userDetails,
      UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    String rawPassword = authentication.getCredentials().toString();
    ClosetUserDetails closetUserDetails = (ClosetUserDetails) userDetails;

    // 일반 비밀번호
    if (passwordEncoder.matches(rawPassword, closetUserDetails.getPassword())) {
      if (closetUserDetails.getTempPassword() != null) {
        userRepository.clearTempPassword(closetUserDetails.getUserDTO().id());
      }
      return;
    }

    // 임시 비밀번호
    if (closetUserDetails.getTempPassword() != null &&
        closetUserDetails.getTempPasswordExpiredAt() != null) {

      if (closetUserDetails.getTempPasswordExpiredAt().isBefore(Instant.now())) {
        userRepository.clearTempPassword(closetUserDetails.getUserDTO().id());
        throw new BadCredentialsException("임시 비밀번호가 만료됨.");
      }

      if (passwordEncoder.matches(rawPassword, closetUserDetails.getTempPassword())) {
        userRepository.clearTempPassword(closetUserDetails.getUserDTO().id());
        return;
      }
    }

      throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
  }
}
