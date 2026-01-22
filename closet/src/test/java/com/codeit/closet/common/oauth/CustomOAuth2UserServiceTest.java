package com.codeit.closet.common.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.closet.module.user.entity.AuthProvider;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.entity.UserRole;
import com.codeit.closet.module.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

  @Mock
  UserRepository userRepository;

  @Mock
  PasswordEncoder passwordEncoder;

  CustomOAuth2UserService service;

  OAuth2UserRequest userRequest;

  ClientRegistration clientRegistration;

  @BeforeEach
  void setUp() {
    service = spy(new CustomOAuth2UserService(userRepository, passwordEncoder));

    clientRegistration =
        ClientRegistration.withRegistrationId("google")
            .clientId("client-id")
            .clientSecret("secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("email", "profile")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName("email")
            .clientName("Google")
            .build();
    OAuth2AccessToken accessToken =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "dummy-access-token",
            Instant.now(),
            Instant.now().plusSeconds(60)
        );

    userRequest = new OAuth2UserRequest(clientRegistration, accessToken);
  }
  @Test
  @DisplayName("Kakao 신규 사용자면 회원을 생성한다")
  void loadUser_kakao_new_user() {
    // given
    clientRegistration =
        ClientRegistration.withRegistrationId("kakao")
            .clientId("client-id")
            .clientSecret("secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("profile_nickname", "account_email")
            .authorizationUri("https://kauth.kakao.com/oauth/authorize")
            .tokenUri("https://kauth.kakao.com/oauth/token")
            .userInfoUri("https://kapi.kakao.com/v2/user/me")
            .userNameAttributeName("id")
            .clientName("Kakao")
            .build();

    OAuth2AccessToken accessToken =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "dummy-access-token",
            Instant.now(),
            Instant.now().plusSeconds(60)
        );

    userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of(
            "id", 9999L,
            "kakao_account", Map.of(
                "email", "kakao@test.com",
                "profile", Map.of("nickname", "kakaoUser")
            )
        ),
        "id"
    );

    doReturn(oauth2User)
        .when(service)
        .loadOAuth2User(any(OAuth2UserRequest.class));

    when(userRepository.findByEmail("kakao@test.com"))
        .thenReturn(Optional.empty());

    when(passwordEncoder.encode(any()))
        .thenReturn("encoded");

    when(userRepository.save(any(User.class)))
        .thenAnswer(inv -> {
          User user = inv.getArgument(0);
          user.updateRole(UserRole.USER);
          return user;
        });

    // when
    OAuth2User result = service.loadUser(userRequest);

    // then
    verify(userRepository).save(any(User.class));
    assertThat(result.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
  }



  @Test
  @DisplayName("Kakao에서 email이 없으면 자동 생성한다")
  void loadUser_kakao_email_fallback() {
    // given
    clientRegistration =
        ClientRegistration.withRegistrationId("kakao")
            .clientId("client-id")
            .clientSecret("secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("profile_nickname")
            .authorizationUri("https://kauth.kakao.com/oauth/authorize")
            .tokenUri("https://kauth.kakao.com/oauth/token")
            .userInfoUri("https://kapi.kakao.com/v2/user/me")
            .userNameAttributeName("id")
            .clientName("Kakao")
            .build();

    OAuth2AccessToken accessToken =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "dummy-access-token",
            Instant.now(),
            Instant.now().plusSeconds(60)
        );

    userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of(
            "id", 7777L,
            "kakao_account", Map.of(
                "profile", Map.of("nickname", "kakaoUser")
            )
        ),
        "id"
    );

    doReturn(oauth2User)
        .when(service)
        .loadOAuth2User(any(OAuth2UserRequest.class));

    when(userRepository.findByEmail("null_7777@kakao.com"))
        .thenReturn(Optional.empty());

    when(passwordEncoder.encode(any()))
        .thenReturn("encoded");

    when(userRepository.save(any(User.class)))
        .thenAnswer(inv -> {
          User user = inv.getArgument(0);
          user.updateRole(UserRole.USER);
          return user;
        });

    // when
    OAuth2User result = service.loadUser(userRequest);

    // then
    verify(userRepository).save(any(User.class));
    assertThat(result.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
  }

  @Test
  @DisplayName("Google 신규 사용자면 회원을 생성한다")
  void loadUser_google_new_user() {
    // given
    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of(
            "email", "test@gmail.com",
            "name", "tester",
            "sub", "123"
        ),
        "email"
    );

    doReturn(oauth2User)
        .when(service)
        .loadOAuth2User(any(OAuth2UserRequest.class));

    when(userRepository.findByEmail("test@gmail.com"))
        .thenReturn(Optional.empty());

    when(passwordEncoder.encode(any()))
        .thenReturn("encoded");

    when(userRepository.save(any(User.class)))
        .thenAnswer(inv -> {
          User user = inv.getArgument(0);
          user.updateRole(UserRole.USER);
          return user;
        });

    // when
    OAuth2User result = service.loadUser(userRequest);

    // then
    verify(userRepository).save(any(User.class));
    assertThat(result.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
  }

  @Test
  @DisplayName("Google 기존 사용자면 회원을 생성하지 않는다")
  void loadUser_google_existing_user() {
    // given
    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of(
            "email", "test@gmail.com",
            "name", "tester",
            "sub", "123"
        ),
        "email"
    );

    doReturn(oauth2User)
        .when(service)
        .loadOAuth2User(any(OAuth2UserRequest.class));

    User existingUser = User.builder()
        .email("test@gmail.com")
        .name("tester")
        .password("encoded")
        .provider(AuthProvider.GOOGLE)
        .providerId("123")
        .role(UserRole.USER)
        .build();

    when(userRepository.findByEmail("test@gmail.com"))
        .thenReturn(Optional.of(existingUser));

    // when
    OAuth2User result = service.loadUser(userRequest);

    // then
    verify(userRepository, never()).save(any(User.class));

    assertThat(result.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
  }

  @Test
  @DisplayName("소셜 신규 사용자를 생성한다")
  void createSocialUser_success() {
    // given
    when(passwordEncoder.encode(any()))
        .thenReturn("encoded-password");

    when(userRepository.save(any(User.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // when
    User user = service.createSocialUser(
        "test@gmail.com",
        "tester",
        AuthProvider.GOOGLE,
        "123"
    );

    // then
    verify(passwordEncoder).encode(any());
    verify(userRepository).save(any(User.class));

    assertThat(user.getEmail()).isEqualTo("test@gmail.com");
    assertThat(user.getName()).isEqualTo("tester");
    assertThat(user.getProvider()).isEqualTo(AuthProvider.GOOGLE);
    assertThat(user.getProviderId()).isEqualTo("123");
    assertThat(user.getPassword()).isNotNull();
  }

  @Test
  @DisplayName("이미 소셜 사용자는 provider를 변경하지 않는다")
  void updateProviderIfNeeded_social_user() {
    // given
    User socialUser = User.builder()
        .email("google@test.com")
        .name("google")
        .password("pw")
        .provider(AuthProvider.GOOGLE)
        .providerId("old-id")
        .role(UserRole.USER)
        .build();

    // when
    User result = service.updateProviderIfNeeded(
        socialUser,
        AuthProvider.GOOGLE,
        "new-id"
    );

    // then
    assertThat(result.getProvider()).isEqualTo(AuthProvider.GOOGLE);
    assertThat(result.getProviderId()).isEqualTo("old-id");
  }

}

