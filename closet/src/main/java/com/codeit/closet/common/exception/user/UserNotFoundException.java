package com.codeit.closet.common.exception.user;


import com.codeit.closet.common.exception.ErrorCode;
import java.util.UUID;

public class UserNotFoundException extends UserException {

  public UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND);
  }

  public UserNotFoundException(ErrorCode errorCode, Throwable cause) { super(errorCode, cause); }

  public static UserNotFoundException withUsername(String username) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail("username", username);
    return exception;
  }

  public static UserNotFoundException withId(UUID userId) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail("userId", userId);
    return exception;
  }

  public static UserNotFoundException withEmail(String email) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail("email", email);
    return exception;
  }

  public static UserNotFoundException withMessage(String message) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail("message", message);
    return exception;
  }
}
