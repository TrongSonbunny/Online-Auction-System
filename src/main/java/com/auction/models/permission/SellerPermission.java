package com.auction.models.permission;

import com.auction.models.item.AuctionItem;
import com.auction.models.user.User;

/**
 * Quyền của Seller (đấu giá và quản lý sản phẩm của mình).
 */
public class SellerPermission implements PermissionStrategy {
  @Override
  public boolean hasPermission(Permission permission, User user, AuctionItem item) {
    switch (permission) {
      case BID:
      case CREATE_ITEM:
        return true;

      case UPDATE_OWN_ITEM:
      case DELETE_OWN_ITEM:
        return item != null && item.getSeller().equals(user);
      default:
        return false;
    }
  }
}