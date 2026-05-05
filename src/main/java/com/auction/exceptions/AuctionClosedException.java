package com.auction.exceptions;

/**
 * Ngoại lệ được ném ra khi người dùng cố gắng tương tác
 * với một phiên đấu giá đã kết thúc.
 */
public class AuctionClosedException extends Exception {

  /**
   * Khởi tạo ngoại lệ với thông báo lỗi.
   *
   * @param message Thông báo chi tiết về lỗi
   */
  public AuctionClosedException(String message) {
    super(message);
  }
}