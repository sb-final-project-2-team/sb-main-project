package com.codeit.closet.module.auth.service;

import com.codeit.closet.common.security.jwt.JwtInformation;

public interface AuthService {

  JwtInformation refreshToken(String refreshToken);
}
