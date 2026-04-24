package com.auction.models.permission;

import com.auction.models.item.AuctionItem;
import com.auction.models.user.User;

/**
 * Strategy kiểm tra quyền của người dùng.
 */
public interface PermissionStrategy {

  boolean hasPermission(Permission permission, User user, AuctionItem item);
}
