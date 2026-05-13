package com.auction.models.auction;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.BidException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.item.AuctionItem;
import com.auction.models.user.Bidder;
import com.auction.models.user.Seller;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Đại diện cho một phiên đấu giá trong hệ thống.
 *
 * <p>Lifecycle: {@code PENDING → ACTIVE → FINISHED} hoặc {@code PENDING/ACTIVE → CANCELLED}.
 * Mọi getter/setter trên trạng thái mutable ({@code status}, {@code currentHighestBid},
 * {@code currentHighestBidder}, {@code scheduledEndTime}) đều {@code synchronized}
 * để đảm bảo visibility trong môi trường đa luồng.
 */
public class Auction {

  private final String auctionId;

  private final Seller seller;

  private final AuctionItem item;

  private final double startingPrice;

  private double currentHighestBid;

  private Bidder currentHighestBidder;

  private AuctionStatus status;

  private final LocalDateTime createdAt;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private LocalDateTime scheduledEndTime;

  /**
   * Constructor auction.
   *
   * @param auctionId mã auction
   * @param seller seller tạo auction
   * @param item item đấu giá
   * @param startingPrice giá khởi điểm
   */
  public Auction(
      String auctionId,
      Seller seller,
      AuctionItem item,
      double startingPrice) {

    validateAuctionId(auctionId);
    validateStartingPrice(startingPrice);

    this.auctionId = auctionId;

    this.seller = Objects.requireNonNull(
        seller,
        "Seller không được null.");

    this.item = Objects.requireNonNull(
        item,
        "Item không được null.");

    this.startingPrice = startingPrice;
    this.currentHighestBid = startingPrice;

    this.status = AuctionStatus.PENDING;

    this.createdAt = LocalDateTime.now();
  }

  /**
   * Bắt đầu auction.
   */
  public synchronized void start() {

    if (status != AuctionStatus.PENDING) {

      throw new IllegalStateException(
          "Chỉ auction pending mới được start.");
    }

    status = AuctionStatus.ACTIVE;
    startTime = LocalDateTime.now();
  }

  /**
   * Kết thúc auction.
   */
  public synchronized void finish() {

    if (status != AuctionStatus.ACTIVE) {

      throw new IllegalStateException(
          "Chỉ auction active mới được finish.");
    }

    status = AuctionStatus.FINISHED;
    endTime = LocalDateTime.now();
  }

  /**
   * Hủy auction.
   */
  public synchronized void cancel() {

    if (status == AuctionStatus.FINISHED) {

      throw new IllegalStateException(
          "Không thể hủy auction đã finish.");
    }

    status = AuctionStatus.CANCELLED;
    endTime = LocalDateTime.now();
  }

  /**
   * Cập nhật bid cao nhất.
   *
   * @param bidder bidder mới
   * @param amount số tiền bid
   */
  public synchronized void updateHighestBid(
      Bidder bidder,
      double amount) {

    validateBidder(bidder);
    validateBidAmount(amount);

    if (!isActive()) {

      throw new AuctionClosedException(
          "Auction không hoạt động.");
    }

    if (amount <= currentHighestBid) {

      throw new InvalidBidException(
          "Bid phải lớn hơn giá hiện tại.");
    }

    currentHighestBid = amount;
    currentHighestBidder = bidder;
  }

  /**
   * Kiểm tra auction có active không.
   *
   * @return true nếu active
   */
  public synchronized boolean isActive() {
    return status == AuctionStatus.ACTIVE;
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
   * Validate starting price.
   *
   * @param price giá khởi điểm
   */
  private void validateStartingPrice(
      double price) {

    if (price < 0) {

      throw new BidException(
          "Starting price không được âm.");
    }
  }

  /**
   * Validate bidder.
   *
   * @param bidder bidder
   */
  private void validateBidder(
      Bidder bidder) {

    if (bidder == null) {

      throw new BidException(
          "Bidder không được null.");
    }
  }

  /**
   * Validate bid amount.
   *
   * @param amount số tiền bid
   */
  private void validateBidAmount(
      double amount) {

    if (amount <= 0) {

      throw new BidException(
          "Bid amount phải lớn hơn 0.");
    }
  }

  public String getAuctionId() {
    return auctionId;
  }

  public Seller getSeller() {
    return seller;
  }

  public AuctionItem getItem() {
    return item;
  }

  public double getStartingPrice() {
    return startingPrice;
  }

  public synchronized double getCurrentHighestBid() {
    return currentHighestBid;
  }

  public synchronized Bidder getCurrentHighestBidder() {
    return currentHighestBidder;
  }

  public synchronized AuctionStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public synchronized LocalDateTime getScheduledEndTime() {
    return scheduledEndTime;
  }

  public synchronized void setScheduledEndTime(
      LocalDateTime time) {

    this.scheduledEndTime = time;
  }

  /**
   * Gia hạn thời gian kết thúc dự kiến thêm số giây chỉ định.
   *
   * @param seconds số giây gia hạn
   */
  public synchronized void extendScheduledEndTime(
      long seconds) {

    if (scheduledEndTime == null) {
      scheduledEndTime = LocalDateTime.now();
    }

    scheduledEndTime =
        scheduledEndTime.plusSeconds(seconds);
  }

  @Override
  public String toString() {

    return "Auction{"
        + "auctionId='"
        + auctionId
        + '\''
        + ", seller="
        + seller.getName()
        + ", item="
        + item.getName()
        + ", startingPrice="
        + startingPrice
        + ", currentHighestBid="
        + currentHighestBid
        + ", currentHighestBidder="
        + (currentHighestBidder == null
            ? "null"
            : currentHighestBidder.getName())
        + ", status="
        + status
        + '}';
  }
}