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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Đại diện cho một phiên đấu giá trong hệ thống.
 *
 * <p>Lifecycle: {@code PENDING → ACTIVE → FINISHED} hoặc {@code PENDING/ACTIVE → CANCELLED}.
 * Mọi getter/setter trên trạng thái mutable ({@code status}, {@code currentHighestBid},
 * {@code currentHighestBidder}, {@code scheduledEndTime}) đều dùng {@link ReentrantLock}
 * thay vì {@code synchronized} để tương thích với virtual thread mà không pin carrier thread.
 */
public class Auction {

  private final String auctionId;
  private final Seller seller;
  private final AuctionItem item;
  private double startingPrice;
  private double currentHighestBid;
  private Bidder currentHighestBidder;
  private AuctionStatus status;
  private final LocalDateTime createdAt;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private LocalDateTime scheduledEndTime;
  private long durationSeconds;

  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Constructor auction.
   *
   * @param auctionId     mã auction
   * @param seller        seller tạo auction
   * @param item          item đấu giá
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
    this.seller = Objects.requireNonNull(seller, "Seller không được null.");
    this.item = Objects.requireNonNull(item, "Item không được null.");

    this.startingPrice = startingPrice;
    this.currentHighestBid = startingPrice;
    this.status = AuctionStatus.PENDING;
    this.createdAt = LocalDateTime.now();
  }

  /**
   * Lấy lock để caller bao nhóm nhiều thao tác thành một critical section duy nhất.
   *
   * @return ReentrantLock của auction
   */
  public ReentrantLock getLock() {
    return lock;
  }

  /**
   * Bắt đầu auction.
   */
  public void start() {

    lock.lock();
    try {

      if (status != AuctionStatus.PENDING) {
        throw new IllegalStateException(
            "Chỉ auction pending mới được start.");
      }

      status = AuctionStatus.ACTIVE;
      startTime = LocalDateTime.now();

    } finally {
      lock.unlock();
    }
  }

  /**
   * Kết thúc auction.
   */
  public void finish() {

    lock.lock();
    try {

      if (status != AuctionStatus.ACTIVE) {
        throw new IllegalStateException(
            "Chỉ auction active mới được finish.");
      }

      status = AuctionStatus.FINISHED;
      endTime = LocalDateTime.now();

    } finally {
      lock.unlock();
    }
  }

  /**
   * Hủy auction.
   */
  public void cancel() {

    lock.lock();
    try {

      if (status == AuctionStatus.FINISHED) {
        throw new IllegalStateException(
            "Không thể hủy auction đã finish.");
      }

      status = AuctionStatus.CANCELLED;
      endTime = LocalDateTime.now();

    } finally {
      lock.unlock();
    }
  }

  /**
   * Cập nhật bid cao nhất.
   *
   * @param bidder bidder mới
   * @param amount số tiền bid
   */
  public void updateHighestBid(
      Bidder bidder,
      double amount) {

    lock.lock();
    try {

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

    } finally {
      lock.unlock();
    }
  }

  /**
   * Kiểm tra xem auction có đang hoạt động hay không.
   *
   * @return true nếu trạng thái là ACTIVE
   */
  public boolean isActive() {

    lock.lock();
    try {
      return status == AuctionStatus.ACTIVE;
    } finally {
      lock.unlock();
    }
  }

  private void validateAuctionId(String id) {
    if (id == null || id.isBlank()) {
      throw new AuctionException(
          "AuctionId không hợp lệ.");
    }
  }

  private void validateStartingPrice(double price) {
    if (price < 0) {
      throw new BidException(
          "Starting price không được âm.");
    }
  }

  private void validateBidder(Bidder bidder) {
    if (bidder == null) {
      throw new BidException(
          "Bidder không được null.");
    }
  }

  private void validateBidAmount(double amount) {
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

  /**
   * Lấy giá bid cao nhất hiện tại.
   *
   * @return currentHighestBid
   */
  public double getCurrentHighestBid() {

    lock.lock();
    try {
      return currentHighestBid;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Lấy bidder đang dẫn đầu.
   *
   * @return currentHighestBidder
   */
  public Bidder getCurrentHighestBidder() {

    lock.lock();
    try {
      return currentHighestBidder;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Lấy trạng thái auction.
   *
   * @return AuctionStatus
   */
  public AuctionStatus getStatus() {

    lock.lock();
    try {
      return status;
    } finally {
      lock.unlock();
    }
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

  /**
   * Lấy thời gian kết thúc dự kiến.
   *
   * @return scheduledEndTime
   */
  public LocalDateTime getScheduledEndTime() {

    lock.lock();
    try {
      return scheduledEndTime;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Đặt thời gian kết thúc dự kiến.
   *
   * @param time thời gian mới
   */
  public void setScheduledEndTime(
      LocalDateTime time) {

    lock.lock();
    try {
      this.scheduledEndTime = time;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Lấy thời lượng đấu giá (giây).
   *
   * @return durationSeconds
   */
  public long getDurationSeconds() {

    lock.lock();
    try {
      return durationSeconds;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Đặt thời lượng đấu giá (giây).
   *
   * @param durationSeconds thời lượng tính bằng giây
   */
  public void setDurationSeconds(long durationSeconds) {

    lock.lock();
    try {
      this.durationSeconds = durationSeconds;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Cập nhật giá khởi điểm (chỉ khi PENDING).
   *
   * @param startingPrice giá khởi điểm mới
   */
  public void setStartingPrice(double startingPrice) {

    lock.lock();
    try {
      validateStartingPrice(startingPrice);
      this.startingPrice = startingPrice;
      if (status == AuctionStatus.PENDING) {
        this.currentHighestBid = startingPrice;
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Gia hạn thêm thời gian cho phiên đấu giá.
   *
   * @param seconds số giây cần cộng thêm
   */
  public void extendScheduledEndTime(
      long seconds) {

    lock.lock();
    try {

      if (scheduledEndTime == null) {
        scheduledEndTime = LocalDateTime.now();
      }

      scheduledEndTime =
          scheduledEndTime.plusSeconds(seconds);

    } finally {
      lock.unlock();
    }
  }

  @Override
  public String toString() {
    return "Auction{"
        + "auctionId='" + auctionId + '\''
        + ", seller=" + seller.getName()
        + ", item=" + item.getName()
        + ", startingPrice=" + startingPrice
        + ", currentHighestBid=" + currentHighestBid
        + ", currentHighestBidder="
        + (currentHighestBidder == null ? "null" : currentHighestBidder.getName())
        + ", status=" + status
        + '}';
  }
}
