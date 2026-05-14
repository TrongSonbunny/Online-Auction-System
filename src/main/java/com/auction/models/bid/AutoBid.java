package com.auction.models.bid;

import com.auction.exceptions.BidException;
import com.auction.models.user.Bidder;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Bản ghi bất biến cho một auto-bid mà bidder đăng ký.
 *
 * <p>Hệ thống dùng {@code maxBid} và {@code increment} để tự động đặt giá thay bidder:
 * mỗi lần auto-bid, giá tăng thêm {@code increment}, tối đa đến {@code maxBid}.
 * {@code registeredAt} dùng làm tie-breaker khi hai auto-bid có cùng {@code maxBid}.
 */
public class AutoBid {

  private final String autoBidId;

  private final Bidder bidder;

  private final String auctionId;

  private final double maxBid;

  private final double increment;

  private final LocalDateTime registeredAt;

  /**
   * Constructor auto-bid.
   *
   * @param autoBidId mã auto-bid
   * @param bidder bidder đăng ký
   * @param auctionId mã auction
   * @param maxBid giá tối đa bidder chấp nhận trả
   * @param increment bước giá tối thiểu mỗi lần tự động bid
   */
  public AutoBid(
      String autoBidId,
      Bidder bidder,
      String auctionId,
      double maxBid,
      double increment) {

    if (autoBidId == null
        || autoBidId.isBlank()) {

      throw new BidException(
          "AutoBidId không hợp lệ.");
    }

    this.autoBidId = autoBidId;

    this.bidder = Objects.requireNonNull(
        bidder,
        "Bidder không được null.");

    if (auctionId == null
        || auctionId.isBlank()) {

      throw new BidException(
          "AuctionId không hợp lệ.");
    }

    this.auctionId = auctionId;

    if (maxBid <= 0) {

      throw new BidException(
          "MaxBid phải lớn hơn 0.");
    }

    this.maxBid = maxBid;

    if (increment <= 0) {

      throw new BidException(
          "Increment phải lớn hơn 0.");
    }

    this.increment = increment;

    this.registeredAt = LocalDateTime.now();
  }

  public String getAutoBidId() {
    return autoBidId;
  }

  public Bidder getBidder() {
    return bidder;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public double getMaxBid() {
    return maxBid;
  }

  public double getIncrement() {
    return increment;
  }

  public LocalDateTime getRegisteredAt() {
    return registeredAt;
  }

  @Override
  public String toString() {

    return "AutoBid{"
        + "autoBidId='"
        + autoBidId
        + '\''
        + ", bidder="
        + bidder.getName()
        + ", auctionId='"
        + auctionId
        + '\''
        + ", maxBid="
        + maxBid
        + ", increment="
        + increment
        + ", registeredAt="
        + registeredAt
        + '}';
  }
}