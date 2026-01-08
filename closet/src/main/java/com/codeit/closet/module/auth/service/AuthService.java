package com.codeit.closet.module.auth.service;

import com.codeit.closet.common.security.jwt.JwtInformation;
import com.codeit.closet.module.auth.dto.ResetPasswordRequest;

public interface AuthService {

  JwtInformation refreshToken(String refreshToken);

  void resetPassword(ResetPasswordRequest request);
}
