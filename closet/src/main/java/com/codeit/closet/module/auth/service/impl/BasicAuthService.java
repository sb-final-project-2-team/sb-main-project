package com.codeit.closet.module.auth.service.impl;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.common.security.jwt.JwtInformation;
import com.codeit.closet.common.security.jwt.JwtRegistry;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.module.auth.service.AuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry<UUID> jwtRegistry;
  private final UserDetailsService userDetailsService;

  @Override
  public JwtInformation refreshToken(String refreshToken) {
    if (!jwtTokenProvider.validateRefreshToken(refreshToken)
    || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RuntimeException("refreshToken이 만료되었거나 존재하지 않습니다.");
    }

    String email = jwtTokenProvider.getEmailFromToken(refreshToken);
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    if (userDetails == null) {
      throw new UsernameNotFoundException("User not found");
    }

    try {
      ClosetUserDetails closetUserDetails = (ClosetUserDetails) userDetails;
      String newAccessToken = jwtTokenProvider.generateAccessToken(closetUserDetails);
      String newRefreshToken = jwtTokenProvider.generateRefreshToken(closetUserDetails);

      log.info("new RefreshToken : {}", newRefreshToken);
      JwtInformation newJwtInformation = new JwtInformation(
          closetUserDetails.getUserDTO(),
          newAccessToken,
          newRefreshToken
      );

      jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);
      return newJwtInformation;
    }catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}
