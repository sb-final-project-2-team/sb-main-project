package com.codeit.closet.common.oauth;

import com.codeit.closet.common.exception.ErrorResponse;
import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.common.security.jwt.JwtInformation;
import com.codeit.closet.common.security.jwt.JwtRegistry;
import com.codeit.closet.common.security.jwt.JwtTokenProvider;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.mapper.UserMapper;
import com.codeit.closet.module.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry<UUID> jwtRegistry;
  private final ObjectMapper objectMapper;
  private final UserMapper userMapper;

  @Value("${closet.base_url.redirect}")
  private String base_url;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
    String email = null;
    String providerId = null;

    if ("google".equals(registrationId)) {
      email = oauth2User.getAttribute("email");
      providerId = oauth2User.getAttribute("sub");   // Google 고유 ID
    } else if ("kakao".equals(registrationId)) {
      Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");

      if (kakaoAccount != null) {
        email = (String) kakaoAccount.get("email"); // 카카오 이메일
      }
      Object idObj = oauth2User.getAttribute("id");
      if (idObj != null) {
        providerId = String.valueOf(idObj);
      }

    }
    if (email == null && providerId == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      ErrorResponse errorResponse = new ErrorResponse(
          new NoSuchElementException("사용자 식별 정보를 가져올 수 없습니다."),
          HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      return;
    }

    User user;

    if (email != null) {
      user = userRepository.findByEmail(email).orElseThrow(
          () -> new NoSuchElementException("존재하지 않는 회원입니다."));
    } else {
      user = userRepository.findByProviderId(providerId).orElseThrow(
          () -> new NoSuchElementException("존재하지 않는 회원입니다."));
    }

    UserDTO userDTO = userMapper.toUserDTO(user);
    ClosetUserDetails closetUserDetails = new ClosetUserDetails(userDTO, null, null, null);
    try {
      String accessToken = jwtTokenProvider.generateAccessToken(closetUserDetails);
      String refreshToken = jwtTokenProvider.generateRefreshToken(closetUserDetails);

      Cookie refreshTokenCookie = jwtTokenProvider.generateRefreshTokenCookie(refreshToken);
      refreshTokenCookie.setHttpOnly(true);
      refreshTokenCookie.setPath("/");
      response.addCookie(refreshTokenCookie);

      response.sendRedirect(base_url);
      response.setStatus(HttpServletResponse.SC_OK);

      jwtRegistry.registerJwtInformation(
          new JwtInformation(closetUserDetails.getUserDTO(), accessToken, refreshToken));
    } catch (JOSEException e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      ErrorResponse errorResponse = new ErrorResponse(e,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
  }
}
