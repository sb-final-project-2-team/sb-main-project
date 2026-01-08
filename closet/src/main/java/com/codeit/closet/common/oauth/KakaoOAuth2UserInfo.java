package com.codeit.closet.common.oauth;

import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

  private final Map<String, Object> attributes;

  @Override
  public String getId() {
    return String.valueOf(attributes.get("id"));
  }

  @Override
  @SuppressWarnings("unchecked")
  public String getEmail() {
    Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
    if (account == null) {
      return null;
    }
    return (String) account.get("email");
  }

  @Override
  @SuppressWarnings("unchecked")
  public String getName() {
    Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
    if (properties == null) {
      return null;
    }
    return (String) properties.get("nickname");
  }
}
