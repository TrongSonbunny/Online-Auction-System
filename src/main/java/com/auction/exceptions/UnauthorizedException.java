package com.auction.exceptions;

/**
 * Exception ném khi user cố thực hiện hành động vượt quyền được phép.
 *
 * <p>Ví dụ: Seller cố đặt giá, Bidder cố tạo auction.
 */
public class UnauthorizedException
    extends AuctionException {

  /**
   * Constructor unauthorized exception.
   *
   * @param message nội dung lỗi
   */
  public UnauthorizedException(
      String message) {

    super(message);
  }

  /**
   * Constructor unauthorized exception.
   *
   * @param message nội dung lỗi
   * @param cause nguyên nhân lỗi
   */
  public UnauthorizedException(
      String message,
      Throwable cause) {

    super(message, cause);
  }
}