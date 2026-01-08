package com.codeit.closet.common.oauth;

import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleOAuth2UserInfo implements OAuth2UserInfo{
  private final Map<String, Object> attributes;

  @Override
  public String getId() {
    return (String) attributes.get("sub");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }

  @Override
  public String getName() {
    return (String) attributes.get("name");
  }
}
