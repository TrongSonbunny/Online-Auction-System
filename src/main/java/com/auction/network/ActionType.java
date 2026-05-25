package com.auction.network;

/**
 * Enum liệt kê tất cả action mà frontend có thể gửi lên server.
 *
 * <pre>
 * - LOGIN: Đăng nhập bằng email và password.
 * </pre>
 *
 * <p>Mỗi action tương ứng với một
 * {@link com.auction.network.command.ClientCommand}
 * được ánh xạ trong {@link com.auction.network.command.ClientCommandFactory}.
 * {@link com.auction.network.ClientActionHandler#doAction} dùng enum này
 * để dispatch đến đúng command.
 */
public enum ActionType {

  /** Đăng nhập bằng email và password. */
  LOGIN,

  /** Đăng ký tài khoản mới với role, tên, email và password. */
  REGISTER,

  /** Đặt giá thủ công cho một auction đang ACTIVE. */
  BID,

  /** Tạo auction mới với thông tin item và giá khởi điểm. */
  CREATE_AUCTION,

  /** Cập nhật thông tin auction khi chưa mở (PENDING). */
  UPDATE_AUCTION,

  /** Bắt đầu phiên đấu giá (chuyển từ PENDING sang ACTIVE). */
  START_AUCTION,

  /** Hủy auction đang PENDING hoặc ACTIVE (chỉ seller sở hữu hoặc admin). */
  CANCEL_AUCTION,

  /**
   * Kết thúc auction đang ACTIVE trước thời hạn (chỉ seller sở hữu hoặc admin).
   */
  FINISH_AUCTION,

  /** Lấy danh sách toàn bộ auction đang trong RAM. */
  GET_ALL_AUCTIONS,

  /** Đăng ký Auto-bid. */
  REGISTER_AUTO_BID,

  /** Hủy Auto-bid. */
  CANCEL_AUTO_BID,

  /** Lấy lịch sử giao dịch của một phiên đấu giá. */
  GET_BID_HISTORY
}