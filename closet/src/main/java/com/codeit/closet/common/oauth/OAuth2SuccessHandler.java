package com.codeit.closet.common.oauth;

import com.codeit.closet.common.exception.ErrorResponse;
import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.common.security.jwt.JwtDTO;
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
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry<UUID> jwtRegistry;
  private final ObjectMapper objectMapper;
  private final UserMapper userMapper;


  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    String email = oauth2User.getAttribute("email");

    User user = userRepository.findByEmail(email).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 회원입니다."));

    UserDTO userDTO = userMapper.toUserDTO(user);
    ClosetUserDetails closetUserDetails = new ClosetUserDetails(userDTO, null, null, null);
    try {
      String accessToken = jwtTokenProvider.generateAccessToken(closetUserDetails);
      String refreshToken = jwtTokenProvider.generateRefreshToken(closetUserDetails);

      Cookie refreshTokenCookie = jwtTokenProvider.generateRefreshTokenCookie(refreshToken);
      refreshTokenCookie.setHttpOnly(true);
      refreshTokenCookie.setPath("/");
      response.addCookie(refreshTokenCookie);

      response.sendRedirect("http://localhost:8080");
      response.setStatus(HttpServletResponse.SC_OK);

      jwtRegistry.registerJwtInformation(
          new JwtInformation(closetUserDetails.getUserDTO(), accessToken, refreshToken));
    }catch (JOSEException e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      ErrorResponse errorResponse = new ErrorResponse(e,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
  }
}
