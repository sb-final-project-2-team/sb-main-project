package com.codeit.closet.common.exception.user;

import com.codeit.closet.common.exception.ClosetException;
import com.codeit.closet.common.exception.ErrorCode;

public class UserException extends ClosetException {

  // 에러코드만
  public UserException(ErrorCode errorCode) { super(errorCode); }

  // 에러코드 + 이유 (위에만 사용해도 무관)
  public UserException(ErrorCode errorCode, Throwable cause) { super(errorCode, cause);}
}
