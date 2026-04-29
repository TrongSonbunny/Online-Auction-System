package com.auction.exceptions;

/** Exception đối với các lỗi đọc/ghi dữ liệu. */
public class DataException extends Exception {

  public DataException(String message, Throwable cause) {
    super(message, cause);
  }
}