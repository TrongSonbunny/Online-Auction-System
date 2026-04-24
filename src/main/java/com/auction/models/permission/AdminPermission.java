package com.auction.models.permission;

import com.auction.models.item.AuctionItem;
import com.auction.models.user.User;

/**
 * Quyền của Admin (toàn quyền).
 */
public class AdminPermission implements PermissionStrategy {
  @Override
  public boolean hasPermission(Permission permission, User user, AuctionItem item) {
    return true;
  }
}