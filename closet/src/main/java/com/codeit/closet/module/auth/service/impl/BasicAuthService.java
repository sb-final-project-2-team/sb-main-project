package com.codeit.closet.module.auth.service.impl;

import com.codeit.closet.common.mail.service.MailService;
import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.common.security.jwt.JwtInformation;
import com.codeit.closet.common.security.jwt.JwtRegistry;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.module.auth.dto.ResetPasswordRequest;
import com.codeit.closet.module.auth.service.AuthService;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;

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

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    User user = userRepository.findByEmail(request.email()).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 회원입니다."));

    String tempPassword = generateRandomString(8);

    user.updateTempPassword(passwordEncoder.encode(tempPassword));

    mailService.sendResetPasswordMail(user.getEmail(), tempPassword);
  }

  public static String generateRandomString(int length) {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    Random random = new Random();

    StringBuilder sb = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      int randomIndex = random.nextInt(characters.length());
      char randomChar = characters.charAt(randomIndex);

      sb.append(randomChar);
    }

    return sb.toString();
  }
}
