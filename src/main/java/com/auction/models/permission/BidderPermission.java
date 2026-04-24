package com.auction.models.permission;

import com.auction.models.item.AuctionItem;
import com.auction.models.user.User;

/**
 * Quyền của Bidder (chỉ được đấu giá).
 */
public class BidderPermission implements PermissionStrategy {

  @Override
  public boolean hasPermission(Permission permission, User user, AuctionItem item) {
    return permission == Permission.BID;
  }
}
