package com.auction.models.item;

import com.auction.models.user.User;

/**
 * Lớp đại diện cho sản phẩm thực phẩm.
 */
public class FoodItem extends AuctionItem {
  public FoodItem(String id, String name, double price, User seller) {
    super(id, name, price, seller);
  }
}