package com.codeit.closet.common.oauth;

import java.util.Map;

public class OAuth2UserInfoFactory {

  public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
    if ("google".equalsIgnoreCase(registrationId)) {
      return new GoogleOAuth2UserInfo(attributes);
    }
    if ("kakao".equalsIgnoreCase(registrationId)) {
      return new KakaoOAuth2UserInfo(attributes);
    }
    throw new IllegalArgumentException("지원하지 않는 OAuth2 Provider: " + registrationId);
  }
}
