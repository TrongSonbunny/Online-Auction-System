package com.auction.exceptions;

/**
 * Exception ném khi bid amount không vượt qua giá cao nhất hiện tại
 * (bằng hoặc thấp hơn {@code currentHighestBid}).
 *
 * <p>Phân biệt với {@link BidException}: InvalidBidException chỉ dùng
 * cho trường hợp bid thua về giá, còn BidException dùng cho lỗi validation chung.
 */
public class InvalidBidException
    extends BidException {

  /**
   * Constructor invalid bid exception.
   *
   * @param message nội dung lỗi
   */
  public InvalidBidException(
      String message) {

    super(message);
  }

  /**
   * Constructor invalid bid exception.
   *
   * @param message nội dung lỗi
   * @param cause nguyên nhân lỗi
   */
  public InvalidBidException(
      String message,
      Throwable cause) {

    super(message, cause);
  }
}