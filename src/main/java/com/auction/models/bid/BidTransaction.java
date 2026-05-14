package com.auction.models.bid;

import com.auction.exceptions.AuctionException;
import com.auction.exceptions.BidException;
import com.auction.models.user.Bidder;
import java.util.Objects;

/**
 * Bản ghi bất biến cho một lần đặt giá thành công.
 *
 * <p>Lưu trữ: bidder thực hiện, auction mục tiêu, số tiền bid và thời điểm tạo
 * (kế thừa từ {@link Transaction}). Được lưu vào {@link com.auction.backend.bid.BidHistoryManager}
 * và dùng làm payload cho event {@code NEW_BID}/{@code AUTO_BID_PLACED}.
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
      throw new AuctionException(
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
      throw new BidException(
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