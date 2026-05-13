package com.auction.exceptions;

/**
 * Exception ném khi dữ liệu bid không hợp lệ (bidder null, amount âm/bằng 0,
 * amount không cao hơn giá hiện tại, hoặc bidder thiếu quyền đặt giá).
 */
public class BidException
    extends AuctionException {

  /**
   * Constructor bid exception.
   *
   * @param message nội dung lỗi
   */
  public BidException(
      String message) {

    super(message);
  }

  /**
   * Constructor bid exception.
   *
   * @param message nội dung lỗi
   * @param cause nguyên nhân lỗi
   */
  public BidException(
      String message,
      Throwable cause) {

    super(message, cause);
  }
}