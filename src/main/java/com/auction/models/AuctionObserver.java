package com.auction.models;

/**
 * Interface đại diện cho một observer theo dõi các sự kiện của phiên đấu giá.
 * Áp dụng mẫu thiết kế Observer.
 */
public interface AuctionObserver {

  /**
   * Phương thức được gọi khi có một lượt đặt giá mới trong phiên đấu giá.
   *
   * @param auction Phiên đấu giá đang diễn ra
   */
  void onBidPlaced(Auction auction);

  /**
   * Phương thức được gọi khi phiên đấu giá chính thức khép lại.
   *
   * @param auction Phiên đấu giá vừa kết thúc
   */
  void onAuctionClosed(Auction auction);
}