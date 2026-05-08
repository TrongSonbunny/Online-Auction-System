package com.auction.backend.observer;

import com.auction.backend.core.AuctionStatus;

/**
 * Đối tượng chứa thông tin sự kiện được gửi đến các Observer.
 * Khi có bid mới hoặc trạng thái phiên thay đổi,
 * một AuctionEvent sẽ được tạo và broadcast.
 */
public class AuctionEvent {

  /**
   * Các loại sự kiện có thể xảy ra trong phiên đấu giá.
   */
  public enum EventType {
    NEW_BID,
    AUCTION_STARTED,
    AUCTION_FINISHED,
    AUCTION_CANCELED,
    AUCTION_PAID
  }

  private final String auctionId;
  private final EventType eventType;
  private final double currentHighestBid;
  private final String currentLeader;
  private final AuctionStatus newStatus;
  private final long timestamp;

  /**
   * Constructor tạo một sự kiện đấu giá.
   *
   * @param auctionId mã định danh phiên đấu giá
   * @param eventType loại sự kiện xảy ra
   * @param currentHighestBid giá cao nhất hiện tại
   * @param currentLeader ID người đang dẫn đầu
   * @param newStatus trạng thái mới của phiên
   */
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

  /*
  * @return ID của phiên đấu giá liên quan. */
  public String getAuctionId() {
    return auctionId;
  }

  /*
  * @return loại sự kiện (BID, START, FINISH, ...). */
  public EventType getEventType() {
    return eventType;
  }

  /*
  * @return mức giá cao nhất tại thời điểm xảy ra sự kiện. */
  public double getCurrentHighestBid() {
    return currentHighestBid;
  }

  /*
  * @return ID của người dùng đang giữ giá cao nhất. */
  public String getCurrentLeader() {
    return currentLeader;
  }

  /*
  * @return trạng thái mới của phiên đấu giá sau sự kiện. */
  public AuctionStatus getNewStatus() {
    return newStatus;
  }

  /*
  * @return thời điểm (miligiây) sự kiện được tạo. */
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