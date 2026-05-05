package com.auction.exceptions;

/**
 * Ngoại lệ được ném ra khi thông tin đăng nhập sai hoặc không có quyền truy
 * cập.
 */
public class AuthenticationException extends Exception {

  /**
   * Khởi tạo ngoại lệ với thông báo lỗi.
   *
   * @param message Thông báo chi tiết về lỗi
   */
  public AuthenticationException(String message) {
    super(message);
  }
}