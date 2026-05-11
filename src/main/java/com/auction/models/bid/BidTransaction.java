package com.auction.models.bid;

import com.auction.models.user.Bidder;
import java.util.Objects;

/**
 * Transaction cho hành động đặt giá.
 */
public class BidTransaction extends Transaction {

  private final Bidder bidder;

  private final String auctionId;

  private final double bidAmount;

  /**
   * Constructor bid transaction.
   *
   * @param transactionId mã transaction
   * @param bidder bidder thực hiện
   * @param auctionId mã auction
   * @param bidAmount số tiền bid
   */
  public BidTransaction(
      String transactionId,
      Bidder bidder,
      String auctionId,
      double bidAmount) {

    super(transactionId);

    validateAuctionId(auctionId);
    validateBidAmount(bidAmount);

    this.bidder = Objects.requireNonNull(
        bidder,
        "Bidder không được null.");

    this.auctionId = auctionId;
    this.bidAmount = bidAmount;
  }

  /**
   * Validate auctionId.
   *
   * @param id auctionId
   */
  private void validateAuctionId(String id) {

    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException(
          "AuctionId không hợp lệ.");
    }
  }

  /**
   * Validate bid amount.
   *
   * @param amount số tiền bid
   */
  private void validateBidAmount(double amount) {

    if (amount <= 0) {
      throw new IllegalArgumentException(
          "Bid amount phải lớn hơn 0.");
    }
  }

  public Bidder getBidder() {
    return bidder;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public double getBidAmount() {
    return bidAmount;
  }

  @Override
  public String toString() {

    return "BidTransaction{"
        + "bidder="
        + bidder.getName()
        + ", auctionId='"
        + auctionId
        + '\''
        + ", bidAmount="
        + bidAmount
        + "} "
        + super.toString();
  }
}