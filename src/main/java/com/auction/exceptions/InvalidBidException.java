package com.auction.exceptions;

/**
 * Ngoại lệ được ném ra khi người dùng đặt giá không hợp lệ
 * (ví dụ: giá thấp hơn hoặc bằng giá hiện tại).
 */
public class InvalidBidException extends Exception {

  /**
   * Khởi tạo ngoại lệ với thông báo lỗi.
   *
   * @param message Thông báo chi tiết về lỗi
   */
  public InvalidBidException(String message) {
    super(message);
  }
}