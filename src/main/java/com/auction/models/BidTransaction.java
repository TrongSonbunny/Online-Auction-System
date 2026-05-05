package com.auction.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Lớp đại diện cho một giao dịch đặt giá trong phiên đấu giá.
 */
public class BidTransaction extends Entity implements Serializable {

  private Bidder bidder;
  private double bidAmount;
  private LocalDateTime timestamp;

  /**
   * Khởi tạo một giao dịch đặt giá mới.
   *
   * @param bidder    Người thực hiện đặt giá
   * @param bidAmount Số tiền đặt giá
   */
  public BidTransaction(Bidder bidder, double bidAmount) {
    super();
    this.bidder = bidder;
    this.bidAmount = bidAmount;
    this.timestamp = LocalDateTime.now();
  }

  /**
   * Lấy thông tin người đặt giá.
   *
   * @return Đối tượng Bidder
   */
  public Bidder getBidder() {
    return bidder;
  }

  /**
   * Lấy số tiền đặt giá trong giao dịch này.
   *
   * @return Số tiền đặt giá
   */
  public double getBidAmount() {
    return bidAmount;
  }

  /**
   * Lấy thời gian thực hiện giao dịch đặt giá.
   *
   * @return Thời gian giao dịch (LocalDateTime)
   */
  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}