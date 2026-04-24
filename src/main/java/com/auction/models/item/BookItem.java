package com.auction.models.item;

import com.auction.models.user.User;

/**
 * Lớp đại diện cho sản phẩm sách.
 */
public class BookItem extends AuctionItem {
  public BookItem(String id, String name, double price, User seller) {
    super(id, name, price, seller);
  }
}