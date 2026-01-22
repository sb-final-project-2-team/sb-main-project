package com.codeit.closet.common.oauth;

import com.codeit.closet.module.user.entity.AuthProvider;
import com.codeit.closet.module.user.entity.User;
import com.codeit.closet.module.user.repository.UserRepository;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // 테스트용 loadUser 분리
  protected OAuth2User loadOAuth2User(OAuth2UserRequest request) {
    return super.loadUser(request);
  }

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = loadOAuth2User(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,
        oauth2User.getAttributes());
    String email = userInfo.getEmail();
    String name = userInfo.getName();
    String providerId = userInfo.getId();
    AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

    String attributeKey = null;

    if (registrationId.equals("google")) {
      attributeKey = "email";

    } else if (registrationId.equals("kakao")) {
      if (email == null) {
        email = name + "_" + providerId + "@kakao.com";
      }

      attributeKey = "kakao_account";
    } else {
      throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자 입니다! : " + registrationId);
    }

    String finalEmail = email;
    User user = userRepository.findByEmail(email)
        .map(existingUser -> updateProviderIfNeeded(existingUser, provider, providerId))
        .orElseGet(() -> createSocialUser(finalEmail, name, provider, providerId));


    return new DefaultOAuth2User(
        Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
        oauth2User.getAttributes(),
        attributeKey
    );

  }

  @Transactional
  protected User createSocialUser(String email, String name, AuthProvider provider,
      String providerId) {
    String dummyPassword = passwordEncoder.encode(UUID.randomUUID().toString());

    User user = User.builder()
        .email(email)
        .name(name != null ? name : "USER")
        .password(dummyPassword)
        .provider(provider)
        .providerId(providerId)
        .build();

    return userRepository.save(user);
  }

  @Transactional
  protected User updateProviderIfNeeded(User user, AuthProvider provider, String providerId) {
    if (user.getProvider() == AuthProvider.LOCAL) {
      user.updateSocialInfo(provider, providerId);
      return user;
    }
    return user;
  }
}
