package com.auction.models.item;

import com.auction.models.user.User;

/**
 * Lớp đại diện cho sản phẩm thời trang.
 */
public class FashionItem extends AuctionItem {
  public FashionItem(String id, String name, double price, User seller) {
    super(id, name, price, seller);
  }
}