package com.auction.exceptions;

/**
 * Exception ném khi thao tác yêu cầu auction đang ACTIVE nhưng auction đã đóng
 * (FINISHED hoặc CANCELLED) hoặc không tồn tại.
 */
public class AuctionClosedException
    extends AuctionException {

  /**
   * Constructor auction closed exception.
   *
   * @param message nội dung lỗi
   */
  public AuctionClosedException(
      String message) {

    super(message);
  }

  /**
   * Constructor auction closed exception.
   *
   * @param message nội dung lỗi
   * @param cause nguyên nhân lỗi
   */
  public AuctionClosedException(
      String message,
      Throwable cause) {

    super(message, cause);
  }
}