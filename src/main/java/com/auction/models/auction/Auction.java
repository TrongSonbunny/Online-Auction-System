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
 * <p>Mỗi auction có một mã duy nhất (auctionId), một người bán (Seller),
 *
 * <p>Lifecycle: {@code PENDING → ACTIVE → FINISHED} hoặc
 * {@code PENDING/ACTIVE → CANCELLED}.
 * Mọi getter/setter trên trạng thái mutable ({@code status},
 * {@code currentHighestBid},
 * {@code currentHighestBidder}, {@code scheduledEndTime}) đều
 * {@code synchronized}
 * để đảm bảo visibility trong môi trường đa luồng.
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

  // Lưu trữ thời lượng đấu giá
  private long durationSeconds;

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
   * Bắt đầu auction.
   */
  public synchronized void start() {
    if (status != AuctionStatus.PENDING) {
      throw new IllegalStateException("Chỉ auction pending mới được start.");
    }
    status = AuctionStatus.ACTIVE;
    startTime = LocalDateTime.now();
  }

  /**
   * Kết thúc auction.
   */
  public synchronized void finish() {
    if (status != AuctionStatus.ACTIVE) {
      throw new IllegalStateException("Chỉ auction active mới được finish.");
    }
    status = AuctionStatus.FINISHED;
    endTime = LocalDateTime.now();
  }

  /**
   * Hủy auction.
   */
  public synchronized void cancel() {
    if (status == AuctionStatus.FINISHED) {
      throw new IllegalStateException("Không thể hủy auction đã finish.");
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
  public synchronized void updateHighestBid(Bidder bidder, double amount) {
    validateBidder(bidder);
    validateBidAmount(amount);

    if (!isActive()) {
      throw new AuctionClosedException("Auction không hoạt động.");
    }

    if (amount <= currentHighestBid) {
      throw new InvalidBidException("Bid phải lớn hơn giá hiện tại.");
    }

    currentHighestBid = amount;
    currentHighestBidder = bidder;
  }

  /**
   * Kiểm tra xem auction có đang hoạt động hay không.
   *
   * @return true nếu trạng thái là ACTIVE, false nếu ngược lại
   */
  public synchronized boolean isActive() {
    return status == AuctionStatus.ACTIVE;
  }

  private void validateAuctionId(String id) {
    if (id == null || id.isBlank()) {
      throw new AuctionException("AuctionId không hợp lệ.");
    }
  }

  private void validateStartingPrice(double price) {
    if (price < 0) {
      throw new BidException("Starting price không được âm.");
    }
  }

  private void validateBidder(Bidder bidder) {
    if (bidder == null) {
      throw new BidException("Bidder không được null.");
    }
  }

  private void validateBidAmount(double amount) {
    if (amount <= 0) {
      throw new BidException("Bid amount phải lớn hơn 0.");
    }
  }

  /**
   * Lấy mã của phiên đấu giá.
   *
   * @return mã auction
   */
  public String getAuctionId() {
    return auctionId;
  }

  /**
   * Lấy thông tin người bán.
   *
   * @return người bán (Seller)
   */
  public Seller getSeller() {
    return seller;
  }

  /**
   * Lấy vật phẩm đấu giá.
   *
   * @return vật phẩm (AuctionItem)
   */
  public AuctionItem getItem() {
    return item;
  }

  /**
   * Lấy giá khởi điểm.
   *
   * @return giá khởi điểm
   */
  public double getStartingPrice() {
    return startingPrice;
  }

  /**
   * Lấy giá đặt cao nhất hiện tại.
   *
   * @return mức giá cao nhất
   */
  public synchronized double getCurrentHighestBid() {
    return currentHighestBid;
  }

  /**
   * Lấy người đặt giá cao nhất hiện tại.
   *
   * @return Bidder đang giữ giá cao nhất
   */
  public synchronized Bidder getCurrentHighestBidder() {
    return currentHighestBidder;
  }

  /**
   * Lấy trạng thái hiện tại của phiên đấu giá.
   *
   * @return trạng thái (AuctionStatus)
   */
  public synchronized AuctionStatus getStatus() {
    return status;
  }

  /**
   * Lấy thời điểm tạo phiên đấu giá.
   *
   * @return thời điểm tạo (LocalDateTime)
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Lấy thời điểm bắt đầu phiên đấu giá.
   *
   * @return thời điểm bắt đầu (LocalDateTime)
   */
  public LocalDateTime getStartTime() {
    return startTime;
  }

  /**
   * Lấy thời điểm kết thúc phiên đấu giá.
   *
   * @return thời điểm kết thúc (LocalDateTime)
   */
  public LocalDateTime getEndTime() {
    return endTime;
  }

  /**
   * Lấy thời điểm dự kiến kết thúc.
   *
   * @return thời điểm dự kiến kết thúc (LocalDateTime)
   */
  public synchronized LocalDateTime getScheduledEndTime() {
    return scheduledEndTime;
  }

  /**
   * Lấy thời lượng của phiên đấu giá tính bằng giây.
   *
   * @return thời lượng (giây)
   */
  public long getDurationSeconds() {
    return durationSeconds;
  }

  /**
   * Cập nhật thời lượng của phiên đấu giá.
   *
   * @param durationSeconds thời lượng tính bằng giây
   */
  public synchronized void setDurationSeconds(long durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  /**
   * Cập nhật giá khởi điểm.
   *
   * @param startingPrice giá khởi điểm mới
   */
  public synchronized void setStartingPrice(double startingPrice) {
    validateStartingPrice(startingPrice);
    this.startingPrice = startingPrice;
    // Đồng bộ giá hiện tại với giá khởi điểm khi chưa mở đấu giá
    if (status == AuctionStatus.PENDING) {
      this.currentHighestBid = startingPrice;
    }
  }

  /**
   * Thiết lập thời điểm dự kiến kết thúc.
   *
   * @param time thời điểm dự kiến kết thúc
   */
  public synchronized void setScheduledEndTime(LocalDateTime time) {
    this.scheduledEndTime = time;
  }

  /**
   * Gia hạn thêm thời gian cho phiên đấu giá.
   *
   * @param seconds số giây cần cộng thêm
   */
  public synchronized void extendScheduledEndTime(long seconds) {
    if (scheduledEndTime == null) {
      scheduledEndTime = LocalDateTime.now();
    }
    scheduledEndTime = scheduledEndTime.plusSeconds(seconds);
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