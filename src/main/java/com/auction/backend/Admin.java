package com.auction.backend;

/**
 * Lớp đại diện cho quản trị viên (Admin) trong hệ thống.
 * Admin có quyền quản lý toàn bộ hệ thống: duyệt phiên, hủy phiên, quản lý user.
 */

public class Admin extends User {

  /**
   * Constructor khởi tạo Admin.
   */
  public Admin(String userId, String name, String email, String passwordHash) {
    super(userId, name, email, passwordHash);
  }

  /**
   * Admin nhận log tất cả sự kiện trong hệ thống để giám sát.
   */
  @Override
  public void update(AuctionEvent event) {
    // Admin ghi log toàn bộ sự kiện để giám sát hệ thống
    System.out.println("[ADMIN LOG] " + event.toString());
  }

  @Override
  public String getRole() {
    return "ADMIN";
  }
}