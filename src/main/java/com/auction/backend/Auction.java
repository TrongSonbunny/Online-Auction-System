package com.auction.backend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lớp trung tâm quản lý một phiên đấu giá.
 * Triển khai IAuctionSubject để broadcast sự kiện đến tất cả Observer.
 * Điểm quan trọng về Concurrency:
 *   - placeBid() được đánh dấu synchronized để tránh race condition.
 *   - Danh sách observer dùng CopyOnWriteArrayList để thread-safe khi duyệt.
 */
public class Auction implements AuctionSubject {

  private final String auctionId; // ID phiên đấu giá
  private final String itemName; // Tên sản phẩm đấu giá
  private final String itemDescription; // Mô tả sản phẩm
  private final String sellerId; // ID người bán tạo phiên
  private final double startingPrice; // Giá khởi điểm

  private double currentHighestBid; // Giá cao nhất hiện tại
  private String currentLeaderId; // ID người đang dẫn đầu
  private AuctionStatus status; // Trạng thái hiện tại của phiên

  private final LocalDateTime startTime; // Thời điểm bắt đầu
  private LocalDateTime endTime; // Thời điểm kết thúc (có thể gia hạn - anti-sniping)

  // Lịch sử toàn bộ các giao dịch đặt giá hợp lệ
  private final List<BidTransaction> bidHistory;

  // Danh sách Observer đang theo dõi phiên này (CopyOnWriteArrayList = thread-safe)
  private final List<AuctionObserver> observers;

  /** Constructor khởi tạo phiên đấu giá. */
  public Auction(
      String auctionId,
      String itemName,
      String itemDescription,
      String sellerId,
      double startingPrice,
      LocalDateTime endTime) {
    this.auctionId = auctionId;
    this.itemName = itemName;
    this.itemDescription = itemDescription;
    this.sellerId = sellerId;
    this.startingPrice = startingPrice;
    this.currentHighestBid = startingPrice;
    this.currentLeaderId = null; // Chưa có ai đặt giá
    this.status = AuctionStatus.OPEN;
    this.startTime = LocalDateTime.now();
    this.endTime = endTime;
    this.bidHistory = new ArrayList<>();
    this.observers = new CopyOnWriteArrayList<>(); // Thread-safe list
  }

  // ==================== Observer Pattern ====================

  @Override
  public void registerObserver(AuctionObserver observer) {
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  @Override
  public void removeObserver(AuctionObserver observer) {
    observers.remove(observer);
  }

  /**
   * Gửi thông báo đến tất cả Observer đang đăng ký.
   * CopyOnWriteArrayList đảm bảo an toàn khi có thread thêm/xóa observer.
   */
  @Override
  public void notifyObservers(AuctionEvent event) {
    for (AuctionObserver observer : observers) {
      observer.update(event);
    }
  }

  // ==================== Logic đấu giá ====================

  /**
   * Bắt đầu phiên đấu giá: chuyển trạng thái OPEN → RUNNING
   * và khởi động AuctionTimerTask trên một thread riêng.
   *
   * @throws IllegalStateException nếu phiên không ở trạng thái OPEN
   */
  public synchronized void startAuction() {
    if (!status.canTransitionTo(AuctionStatus.RUNNING)) {
      throw new IllegalStateException(
          "Không thể bắt đầu phiên. Trạng thái hiện tại: " + status);
    }

    status = AuctionStatus.RUNNING;

    long durationInSeconds =
        java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds();

    AuctionTimerTask timerTask = new AuctionTimerTask(this, durationInSeconds);
    Thread timerThread = new Thread(timerTask, "Timer-Auction-" + auctionId);
    timerThread.setDaemon(true);
    timerThread.start();

    notifyObservers(
        new AuctionEvent(
            auctionId,
            AuctionEvent.EventType.AUCTION_STARTED,
            currentHighestBid,
            currentLeaderId,
            AuctionStatus.RUNNING));

    System.out.println(
        "[AUCTION] Phiên " + auctionId + " đã bắt đầu. Kết thúc lúc: " + endTime);
  }

  /**
   * Xử lý yêu cầu đặt giá từ một Bidder.
   *
   * @param bidder Người đặt giá
   * @param bidAmount Số tiền đặt giá
   * @return BidTransaction chứa thông tin giao dịch vừa thực hiện
   * @throws IllegalStateException nếu phiên không ở trạng thái RUNNING
   * @throws IllegalArgumentException nếu giá đặt không cao hơn giá hiện tại
   */
  public synchronized BidTransaction placeBid(Bidder bidder, double bidAmount) {
    if (status != AuctionStatus.RUNNING) {
      throw new IllegalStateException("Phiên đấu giá đã đóng. Trạng thái: " + status);
    }

    if (bidAmount <= currentHighestBid) {
      throw new IllegalArgumentException(
          "Giá đặt (" + bidAmount + ") phải cao hơn giá hiện tại (" + currentHighestBid + ")");
    }

    if (bidder.getUserId().equals(sellerId)) {
      throw new IllegalArgumentException("Người bán không thể tự đặt giá phiên của mình.");
    }

    currentHighestBid = bidAmount;
    currentLeaderId = bidder.getUserId();

    BidTransaction transaction = new BidTransaction(bidder, bidAmount, auctionId);
    bidHistory.add(transaction);

    System.out.println(
        "[AUCTION] Bid mới tại phiên "
            + auctionId
            + ": "
            + bidder.getName()
            + " đặt "
            + bidAmount);

    notifyObservers(
        new AuctionEvent(
            auctionId,
            AuctionEvent.EventType.NEW_BID,
            currentHighestBid,
            currentLeaderId,
            AuctionStatus.RUNNING));

    return transaction;
  }

  /** Kết thúc phiên đấu giá: chuyển trạng thái RUNNING → FINISHED. */
  public synchronized void endAuction() {
    if (!status.canTransitionTo(AuctionStatus.FINISHED)) {
      return;
    }

    status = AuctionStatus.FINISHED;

    System.out.println(
        "[AUCTION] Phiên "
            + auctionId
            + " đã kết thúc!"
            + (currentLeaderId != null
                ? " Người thắng: " + currentLeaderId + " | Giá: " + currentHighestBid
                : " Không có ai đặt giá."));

    notifyObservers(
        new AuctionEvent(
            auctionId,
            AuctionEvent.EventType.AUCTION_FINISHED,
            currentHighestBid,
            currentLeaderId,
            AuctionStatus.FINISHED));

    if (currentLeaderId == null) {
      cancelAuction();
    }
  }

  /** Hủy phiên đấu giá. */
  public synchronized void cancelAuction() {
    if (!status.canTransitionTo(AuctionStatus.CANCELED)) {
      throw new IllegalStateException("Không thể hủy phiên ở trạng thái: " + status);
    }

    status = AuctionStatus.CANCELED;

    notifyObservers(
        new AuctionEvent(
            auctionId,
            AuctionEvent.EventType.AUCTION_CANCELED,
            currentHighestBid,
            currentLeaderId,
            AuctionStatus.CANCELED));

    System.out.println("[AUCTION] Phiên " + auctionId + " đã bị hủy.");
  }

  /** Xác nhận thanh toán hoàn tất: FINISHED → PAID. */
  public synchronized void markAsPaid() {
    if (!status.canTransitionTo(AuctionStatus.PAID)) {
      throw new IllegalStateException("Không thể chuyển sang PAID từ trạng thái: " + status);
    }

    status = AuctionStatus.PAID;

    notifyObservers(
        new AuctionEvent(
            auctionId,
            AuctionEvent.EventType.AUCTION_PAID,
            currentHighestBid,
            currentLeaderId,
            AuctionStatus.PAID));

    System.out.println("[AUCTION] Phiên " + auctionId + " đã được thanh toán.");
  }

  /**
   * Gia hạn thời gian kết thúc phiên (dùng cho Anti-sniping).
   *
   * @param extraSeconds Số giây gia hạn thêm
   */
  public synchronized void extendEndTime(long extraSeconds) {
    this.endTime = this.endTime.plusSeconds(extraSeconds);
    System.out.println(
        "[AUCTION] Phiên "
            + auctionId
            + " được gia hạn thêm "
            + extraSeconds
            + "s. Kết thúc mới: "
            + endTime);
  }

  // ==================== Getter ====================

  public String getAuctionId() {
    return auctionId;
  }

  public String getItemName() {
    return itemName;
  }

  public String getItemDescription() {
    return itemDescription;
  }

  public String getSellerId() {
    return sellerId;
  }

  public double getStartingPrice() {
    return startingPrice;
  }

  public synchronized double getCurrentHighestBid() {
    return currentHighestBid;
  }

  public synchronized String getCurrentLeaderId() {
    return currentLeaderId;
  }

  public synchronized AuctionStatus getStatus() {
    return status;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public synchronized LocalDateTime getEndTime() {
    return endTime;
  }

  public synchronized List<BidTransaction> getBidHistory() {
    return Collections.unmodifiableList(bidHistory); // Chỉ đọc, không sửa
  }

  @Override
  public String toString() {
    return "Auction{"
              + "auctionId='" + auctionId + '\''
              + ", itemName='" + itemName + '\''
              + ", currentHighestBid=" + currentHighestBid
              + ", currentLeaderId='" + currentLeaderId + '\''
              + ", status=" + status
              + ", endTime=" + endTime
              + '}';
  }
}