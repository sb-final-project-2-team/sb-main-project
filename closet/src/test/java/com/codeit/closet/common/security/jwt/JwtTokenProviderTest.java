package com.codeit.closet.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codeit.closet.common.security.ClosetUserDetails;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

  static final String SECRET =
      "test-secret-key-test-secret-key-test-secret-key"; // HS256은 충분히 길어야 함

  static final long ACCESS_EXPIRE_SEC = 60;
  static final long REFRESH_EXPIRE_SEC = 120;
  static final String ISSUER = "closet-test";

  JwtTokenProvider tokenProvider;

  ClosetUserDetails userDetails;

  static final UUID USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");

  @BeforeEach
  void setUp() throws Exception {
    tokenProvider = new JwtTokenProvider(
        SECRET,
        ACCESS_EXPIRE_SEC,
        REFRESH_EXPIRE_SEC,
        ISSUER
    );

    UserDTO userDTO = new UserDTO(
        USER_ID,
        null,
        "test@test.com",
        "tester",
        UserRole.USER,
        false
    );

    userDetails = new ClosetUserDetails(
        userDTO,
        "password",
        null,
        null
    );
  }

  @Test
  @DisplayName("AccessToken 생성 후 검증에 성공한다")
  void generate_and_validate_access_token() throws Exception {
    // when
    String token = tokenProvider.generateAccessToken(userDetails);

    // then
    assertThat(token).isNotBlank();
    assertThat(tokenProvider.validateAccessToken(token)).isTrue();
  }

  @Test
  @DisplayName("RefreshToken 생성 후 검증에 성공한다")
  void generate_and_validate_refresh_token() throws Exception {
    String token = tokenProvider.generateRefreshToken(userDetails);

    assertThat(token).isNotBlank();
    assertThat(tokenProvider.validateRefreshToken(token)).isTrue();
  }

  @Test
  @DisplayName("AccessToken을 RefreshToken 검증하면 실패한다")
  void token_type_mismatch() throws Exception {
    String accessToken = tokenProvider.generateAccessToken(userDetails);

    assertThat(tokenProvider.validateRefreshToken(accessToken)).isFalse();
  }

  @Test
  @DisplayName("만료된 토큰은 검증에 실패한다")
  void expired_token_is_invalid() throws Exception {
    JwtTokenProvider shortLivedProvider =
        new JwtTokenProvider(SECRET, 0, 0, ISSUER);

    String token = shortLivedProvider.generateAccessToken(userDetails);

    Thread.sleep(10); // 만료 보장

    assertThat(shortLivedProvider.validateAccessToken(token)).isFalse();
  }

  @Test
  @DisplayName("AccessToken을 파싱하면 JwtObject를 반환한다")
  void parse_access_token() throws Exception {
    String token = tokenProvider.generateAccessToken(userDetails);

    JwtObject jwtObject = tokenProvider.parseAccessToken(token);

    assertThat(jwtObject.userDTO().id()).isEqualTo(USER_ID);
    assertThat(jwtObject.userDTO().email()).isEqualTo("test@test.com");
  }

  @Test
  @DisplayName("RefreshToken을 AccessToken으로 파싱하면 예외 발생")
  void parse_type_mismatch() throws Exception {
    String refreshToken = tokenProvider.generateRefreshToken(userDetails);

    assertThatThrownBy(() -> tokenProvider.parseAccessToken(refreshToken))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("JWT 타입 불일치");
  }

  @Test
  @DisplayName("JWT에서 이메일을 추출한다")
  void get_email_from_token() throws Exception {
    String token = tokenProvider.generateAccessToken(userDetails);

    String email = tokenProvider.getEmailFromToken(token);

    assertThat(email).isEqualTo("test@test.com");
  }

  @Test
  @DisplayName("RefreshToken 쿠키를 생성한다")
  void generate_refresh_token_cookie() {
    // when
    ResponseCookie result =
        tokenProvider.generateRefreshTokenCookie("userId");

    // then
    assertThat(result.getName())
        .isEqualTo(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME);
    assertThat(result.isHttpOnly()).isTrue();
  }

  @Test
  @DisplayName("RefreshToken 만료 쿠키를 생성한다")
  void generate_expired_cookie() {
    ResponseCookie responseCookie = tokenProvider.generateRefreshTokenExpirationCookie();

    assertThat(responseCookie.getMaxAge()).isZero();
  }

}
