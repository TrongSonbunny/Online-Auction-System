package com.auction.backend;

/**
 * Tác vụ chạy ngầm (background thread) đếm ngược thời gian cho một phiên đấu giá.
 * Khi hết giờ, tự động gọi endAuction() để khóa phiên.
 *
 * <p>Triển khai Runnable thay vì extends Thread để linh hoạt hơn
 * (có thể chạy trong ThreadPool nếu cần mở rộng).
 */
public class AuctionTimerTask implements Runnable {

  private final Auction targetAuction; // Phiên đấu giá cần đếm ngược
  private final long durationInSeconds; // Thời gian đếm ngược (giây)

  /**
   * Constructor khởi tạo TimerTask cho một phiên đấu giá.
   * targetAuction Phiên đấu giá cần kết thúc tự động
   * durationInSeconds Số giây còn lại từ lúc bắt đầu đến khi kết thúc
   */
  public AuctionTimerTask(Auction targetAuction, long durationInSeconds) {
    this.targetAuction = targetAuction;
    this.durationInSeconds = durationInSeconds;
  }

  /**
   * Logic đếm ngược:
   * Thread ngủ đúng số giây còn lại.</li>
   * Sau khi thức dậy, gọi endAuction() để tự động khóa phiên.
   * Nếu thread bị interrupt (ví dụ JVM tắt đột ngột),
   * vẫn cố gắng kết thúc phiên để đảm bảo dữ liệu nhất quán.
   */
  @Override
  public void run() {
    System.out.println(
        "[TIMER] Bắt đầu đếm ngược "
            + durationInSeconds
            + "s cho phiên: "
            + targetAuction.getAuctionId());

    try {
      Thread.sleep(durationInSeconds * 1000L);

      System.out.println(
          "[TIMER] Hết giờ! Đang kết thúc phiên: " + targetAuction.getAuctionId());
      targetAuction.endAuction();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println(
          "[TIMER] Timer bị dừng sớm cho phiên: "
              + targetAuction.getAuctionId()
              + " | Lý do: "
              + e.getMessage());
    }
  }

  // ==================== Getter ====================

  public Auction getTargetAuction() {
    return targetAuction;
  }

  public long getDurationInSeconds() {
    return durationInSeconds;
  }
}
