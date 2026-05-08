package com.auction.backend.bid;

import java.util.UUID;

import com.auction.models.user.Bidder;

/**
 * Lớp đại diện cho một giao dịch đặt giá cụ thể.
 * Mỗi lần một Bidder đặt giá hợp lệ, một BidTransaction sẽ được tạo
 * và lưu vào lịch sử của phiên đấu giá.
 */
public class BidTransaction extends Transaction {

  private final Bidder bidder;
  private final double bidAmount;
  private final String auctionId;

  /**
   * Constructor khởi tạo BidTransaction, tự động sinh transactionId.
   */
  public BidTransaction(Bidder bidder, double bidAmount, String auctionId) {
    super(UUID.randomUUID().toString());
    this.bidder = bidder;
    this.bidAmount = bidAmount;
    this.auctionId = auctionId;
  }

  /**
   * Mô tả chi tiết giao dịch đặt giá.
   */
  @Override
  public String getTransactionDetails() {
    return "BidTransaction{"
        + "transactionId='" + getTransactionId() + '\''
        + ", auctionId='" + auctionId + '\''
        + ", bidder='" + bidder.getName()
        + " (" + bidder.getUserId() + ")'"
        + ", bidAmount=" + bidAmount
        + ", time=" + getTimestamp()
        + '}';
  }

  public Bidder getBidder() {
    return bidder;
  }

  public double getBidAmount() {
    return bidAmount;
  }

  public String getAuctionId() {
    return auctionId;
  }

  @Override
  public String toString() {
    return getTransactionDetails();
  }
}