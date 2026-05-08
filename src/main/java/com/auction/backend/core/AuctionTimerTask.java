package com.auction.backend.core;

/**
 * Nhiệm vụ quản lý thời gian đếm ngược cho một phiên đấu giá.
 * Được thực thi trên một thread riêng biệt để không làm treo hệ thống chính.
 */
public class AuctionTimerTask implements Runnable {

  private final Auction targetAuction;
  private final long durationInSeconds;

  /**
   * Khởi tạo nhiệm vụ đếm ngược cho một phiên đấu giá cụ thể.
   *
   * @param targetAuction phiên đấu giá cần theo dõi thời gian
   * @param durationInSeconds tổng thời gian (giây) tính từ lúc bắt đầu đến khi kết thúc
   */
  public AuctionTimerTask(Auction targetAuction, long durationInSeconds) {
    this.targetAuction = targetAuction;
    this.durationInSeconds = durationInSeconds;
  }

  /**
   * Thực hiện chờ đợi cho đến khi hết thời gian và tự động kết thúc phiên đấu giá.
   */
  @Override
  public void run() {
    try {
      // Chuyển đổi giây sang miligiây để sử dụng với Thread.sleep
      Thread.sleep(durationInSeconds * 1000L);
      targetAuction.endAuction();
    } catch (InterruptedException e) {
      // Khôi phục trạng thái bị ngắt quãng của thread
      Thread.currentThread().interrupt();
    }
  }
}