package com.auction.models.user.permission;

/**
 * Permission strategy cho seller: chỉ có quyền tạo auction ({@code canCreateAuction = true}).
 *
 * <p>Seller không thể đặt giá, xóa auction, khóa user hay quản lý hệ thống.
 */
public class SellerPermission implements PermissionStrategy {

  @Override
  public boolean canCreateAuction() {
    return true;
  }

  @Override
  public boolean canPlaceBid() {
    return false;
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