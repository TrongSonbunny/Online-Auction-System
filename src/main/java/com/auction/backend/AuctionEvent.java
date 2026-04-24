package com.auction.backend;

/**
 * Đối tượng chứa thông tin sự kiện được gửi đến các Observer.
 * Khi có bid mới hoặc trạng thái phiên thay đổi, một AuctionEvent sẽ được tạo
 * và broadcast đến tất cả Observer đang đăng ký.
 */
public class AuctionEvent {

  /** Các loại sự kiện có thể xảy ra trong phiên đấu giá. */
  public enum EventType {
    NEW_BID, // Có giá đặt mới
    AUCTION_STARTED, // Phiên đấu giá bắt đầu
    AUCTION_FINISHED, // Phiên đấu giá kết thúc
    AUCTION_CANCELED, // Phiên đấu giá bị hủy
    AUCTION_PAID // Thanh toán hoàn tất
  }

  private final String auctionId; // ID của phiên đấu giá liên quan
  private final EventType eventType; // Loại sự kiện
  private final double currentHighestBid; // Giá cao nhất tại thời điểm sự kiện
  private final String currentLeader; // Tên người đang dẫn đầu
  private final AuctionStatus newStatus; // Trạng thái mới của phiên
  private final long timestamp; // Thời điểm xảy ra sự kiện

  /** Constructor tạo một sự kiện đấu giá. */
  public AuctionEvent(
      String auctionId,
      EventType eventType,
      double currentHighestBid,
      String currentLeader,
      AuctionStatus newStatus) {
    this.auctionId = auctionId;
    this.eventType = eventType;
    this.currentHighestBid = currentHighestBid;
    this.currentLeader = currentLeader;
    this.newStatus = newStatus;
    this.timestamp = System.currentTimeMillis();
  }

  // ==================== Getter ====================

  public String getAuctionId() {
    return auctionId;
  }

  public EventType getEventType() {
    return eventType;
  }

  public double getCurrentHighestBid() {
    return currentHighestBid;
  }

  public String getCurrentLeader() {
    return currentLeader;
  }

  public AuctionStatus getNewStatus() {
    return newStatus;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "AuctionEvent{"
        + "auctionId='" + auctionId + '\''
        + ", eventType=" + eventType
        + ", currentHighestBid=" + currentHighestBid
        + ", currentLeader='" + currentLeader + '\''
        + ", newStatus=" + newStatus
        + ", timestamp=" + timestamp
        + '}';
  }
}
