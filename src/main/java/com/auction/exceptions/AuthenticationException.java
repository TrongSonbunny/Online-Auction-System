package com.auction.exceptions;

/** Exception đối với các lỗi xác thực. */
public class AuthenticationException extends Exception {

  public AuthenticationException(String message) {
    super(message);
  }
}