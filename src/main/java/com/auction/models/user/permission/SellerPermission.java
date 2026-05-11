package com.auction.models.user.permission;

/**
 * Permission dành cho seller.
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