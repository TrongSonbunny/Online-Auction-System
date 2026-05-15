package com.auction.models.user.permission;

/**
 * Permission strategy cho bidder: chỉ có quyền đặt giá ({@code canPlaceBid = true}).
 *
 * <p>Bidder không thể tạo/xóa auction, khóa user hay quản lý hệ thống.
 */
public class BidderPermission implements PermissionStrategy {

  @Override
  public boolean canCreateAuction() {
    return false;
  }

  @Override
  public boolean canPlaceBid() {
    return true;
  }

  @Override
  public boolean canDeleteAuction() {
    return false;
  }

  @Override
  public boolean canBanUser() {
    return false;
  }

  @Override
  public boolean canManageSystem() {
    return false;
  }
}