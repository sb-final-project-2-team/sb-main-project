package com.codeit.closet.common.security.jwt;

// JWT 기반 세션 관리 인터페이스
public interface JwtRegistry<T> {

  void registerJwtInformation(JwtInformation jwtInformation);

  void invalidateJwtInformationByUserId(T userId);

  boolean hasActiveJwtInformationByUserId(T userId);

  boolean hasActiveJwtInformationByAccessToken(String accessToken);

  boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

  void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation);

  void clearExpiredJwtInformation();
}
