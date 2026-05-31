package com.auction.models.user;

/**
 * Enum liệt kê các role người dùng trong hệ thống.
 *
 * <p>Role xác định quyền hạn của user:
 * <ul>
 *   <li>{@link #ADMIN} có quyền quản trị toàn hệ thống.
 *   <li>{@link #SELLER} có quyền tạo và quản lý auction của mình.
 *   <li>{@link #BIDDER} có quyền đặt giá và đăng ký auto-bid.
 * </ul>
 */
public enum UserRole {

  /** Quản trị viên — có toàn quyền trên mọi auction và user. */
  ADMIN,

  /** Người bán — tạo auction, hủy hoặc kết thúc auction của chính mình. */
  SELLER,

  /** Người mua — đặt giá thủ công hoặc đăng ký auto-bid cho auction đang ACTIVE. */
  BIDDER
}