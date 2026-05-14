package com.auction.models.user;

import com.auction.models.user.permission.AdminPermission;

/**
 * User quản trị hệ thống (admin).
 *
 * <p>Admin có toàn bộ quyền: tạo/xóa auction, đặt giá, khóa user, quản lý hệ thống.
 * Không có trường thêm — quyền xác định bởi
 * {@link com.auction.models.user.permission.AdminPermission}.
 */
public class Admin extends User {

  /**
   * Constructor admin.
   *
   * @param userId mã admin
   * @param name tên admin
   * @param email email admin
   */
  public Admin(
      String userId,
      String name,
      String email) {

    super(
        userId,
        name,
        email,
        UserRole.ADMIN,
        new AdminPermission());
  }
}