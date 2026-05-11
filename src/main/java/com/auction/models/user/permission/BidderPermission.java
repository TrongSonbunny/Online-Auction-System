package com.auction.models.user.permission;

/**
 * Permission dành cho bidder.
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