package com.auction.exceptions;

/** Exception do lỗi kết nối. */
public class ConnectionException extends Exception {

  public ConnectionException(String message) {
    super(message);
  }
}