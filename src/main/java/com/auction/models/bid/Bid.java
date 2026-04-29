package com.auction.models.bid;

import com.auction.models.user.User;
import java.time.LocalDateTime;

/**
 * Lớp đại diện cho một lượt đấu giá.
 */
public class Bid {

  private final User bidder;
  private final double amount;
  private final LocalDateTime time;

  /**
   * Khởi tạo một bid mới.
   */
  public Bid(User bidder, double amount) {
    this.bidder = bidder;
    this.amount = amount;
    this.time = LocalDateTime.now();
  }

  public User getBidder() {
    return bidder;
  }

  public double getAmount() {
    return amount;
  }

  public LocalDateTime getTime() {
    return time;
  }
}