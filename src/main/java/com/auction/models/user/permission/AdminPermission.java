package com.auction.models.user.permission;

/**
 * Permission strategy cho admin: toàn bộ quyền đều trả {@code true}.
 *
 * <p>Admin có thể tạo/xóa auction, đặt giá, khóa user và quản lý hệ thống.
 */
public class AdminPermission implements PermissionStrategy {

  @Override
  public boolean canCreateAuction() {
    return true;
  }

  @Override
  public boolean canPlaceBid() {
    return true;
  }

  @Override
  public boolean canDeleteAuction() {
    return true;
  }

  @Override
  public boolean canBanUser() {
    return true;
  }

  @Override
  public boolean canManageSystem() {
    return true;
  }
}