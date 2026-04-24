package com.auction.models.item;

import com.auction.models.user.User;

/**
 * Lớp đại diện cho sản phẩm đồ điện tử.
 */
public class ElectronicsItem extends AuctionItem {
  public ElectronicsItem(String id, String name, double price, User seller) {
    super(id, name, price, seller);
  }
}