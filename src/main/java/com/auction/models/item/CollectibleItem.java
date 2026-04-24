package com.auction.models.item;

import com.auction.models.user.User;

/**
 * Lớp đại diện cho sản phẩm sưu tầm.
 */
public class CollectibleItem extends AuctionItem {
  public CollectibleItem(String id, String name, double price, User seller) {
    super(id, name, price, seller);
  }
}