package com.auction.models.item;

import com.auction.models.user.User;


/**
 * Lớp đại diện cho sản phẩm nghệ thuật.
 */
public class ArtItem extends AuctionItem {
  public ArtItem(String id, String name, double price, User seller) {
    super(id, name, price, seller);
  }
}