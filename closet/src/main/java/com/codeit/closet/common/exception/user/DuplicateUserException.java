package com.codeit.closet.common.exception.user;


import com.codeit.closet.common.exception.ErrorCode;

public class DuplicateUserException extends UserException {

  public DuplicateUserException() {
    super(ErrorCode.DUPLICATE_USER);
  }

  public static DuplicateUserException withEmail(String email) {
    DuplicateUserException exception = new DuplicateUserException();
    exception.addDetail("email", email);
    return exception;
  }

  public static DuplicateUserException withMessage(String message) {
    DuplicateUserException exception = new DuplicateUserException();
    exception.addDetail("message", message);
    return exception;
  }
} 