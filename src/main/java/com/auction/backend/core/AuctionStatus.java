package com.auction.backend.core;

/**
 * Enum đại diện cho các trạng thái của một phiên đấu giá.
 * Quản lý logic chuyển đổi trạng thái để đảm bảo tính toàn vẹn của dữ liệu.
 */
public enum AuctionStatus {
  /** Phiên vừa được tạo, chưa bắt đầu. */
  OPEN,
  /** Phiên đang diễn ra và nhận lượt đặt giá. */
  RUNNING,
  /** Phiên đã kết thúc thời gian đấu giá. */
  FINISHED,
  /** Người thắng đã thanh toán thành công. */
  PAID,
  /** Phiên bị hủy bởi hệ thống hoặc người bán. */
  CANCELED;

  /**
   * Kiểm tra xem trạng thái hiện tại có thể chuyển sang trạng thái mục tiêu hay không.
   *
   * @param nextStatus trạng thái muốn chuyển đến
   * @return true nếu việc chuyển đổi là hợp lệ
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
      case CANCELED:
        return false;
      default:
        return false;
    }
  }
}