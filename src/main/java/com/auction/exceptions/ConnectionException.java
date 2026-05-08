package com.auction.exceptions;

/**
 * Exception khi lỗi kết nối DB.
 */
public class ConnectionException extends DataException {

  public ConnectionException(String message) {
    super(message);
  }

  public ConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}