package com.auction.models.user.permission;

/**
 * Strategy xác định quyền của user trong hệ thống.
 */
public interface PermissionStrategy {

  /**
   * Kiểm tra quyền tạo auction.
   *
   * @return true nếu được phép
   */
  boolean canCreateAuction();

  /**
   * Kiểm tra quyền đặt giá.
   *
   * @return true nếu được phép
   */
  boolean canPlaceBid();

  /**
   * Kiểm tra quyền xóa auction.
   *
   * @return true nếu được phép
   */
  boolean canDeleteAuction();

  /**
   * Kiểm tra quyền khóa user.
   *
   * @return true nếu được phép
   */
  boolean canBanUser();

  /**
   * Kiểm tra quyền quản lý hệ thống.
   *
   * @return true nếu được phép
   */
  boolean canManageSystem();
}