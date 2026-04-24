package com.auction.backend;

/**
 * Enum định nghĩa các trạng thái của một phiên đấu giá.
 * Luồng chuyển đổi hợp lệ:
 *   OPEN → RUNNING → FINISHED → PAID
 *                             → CANCELED
 */
public enum AuctionStatus {

  /** Phiên vừa được tạo, đang chờ bắt đầu. Chưa nhận bid, chưa chạy timer. */
  OPEN,

  /** Phiên đang diễn ra: nhận bid và AuctionTimerTask đang chạy. */
  RUNNING,

  /** Hết giờ - placeBid() bị khóa, đang chờ xử lý kết quả. */
  FINISHED,

  /** Người thắng đã hoàn tất thanh toán. */
  PAID,

  /** Phiên bị hủy (không ai đấu giá hoặc vi phạm quy định). */
  CANCELED;

  /**
   * Kiểm tra xem có thể chuyển sang trạng thái mới không.
   * Đảm bảo luồng chuyển đổi hợp lệ.
   *
   * @param nextStatus Trạng thái muốn chuyển sang
   * @return true nếu chuyển đổi hợp lệ
   */
  public boolean canTransitionTo(AuctionStatus nextStatus) {
    switch (this) {
      case OPEN:
        return nextStatus == RUNNING || nextStatus == CANCELED;
      case RUNNING:
        return nextStatus == FINISHED || nextStatus == CANCELED;
      case FINISHED:
        return nextStatus == PAID || nextStatus == CANCELED;
      case PAID:
        return false;
      case CANCELED:
        return false;
      default:
        return false;
    }
  }
}
