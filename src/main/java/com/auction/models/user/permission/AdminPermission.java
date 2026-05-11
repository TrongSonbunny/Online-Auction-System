package com.auction.models.user.permission;

/**
 * Permission dành cho admin.
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