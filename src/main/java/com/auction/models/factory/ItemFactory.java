package com.auction.models.factory;

import com.auction.models.item.ArtItem;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.BookItem;
import com.auction.models.item.CollectibleItem;
import com.auction.models.item.ElectronicsItem;
import com.auction.models.item.FashionItem;
import com.auction.models.item.FoodItem;
import com.auction.models.user.User;


/**
 * Factory tạo các loại AuctionItem.
 */
public final class ItemFactory {

  private ItemFactory() {
  }

  /**
   * Tạo item theo loại.
   */
  public static AuctionItem createItem(
      String type,
      String id,
      String name,
      double price,
      User seller) {

    switch (type.toLowerCase()) {
      case "art":
        return new ArtItem(id, name, price, seller);

      case "book":
        return new BookItem(id, name, price, seller);

      case "collectible":
        return new CollectibleItem(id, name, price, seller);

      case "fashion":
        return new FashionItem(id, name, price, seller);

      case "electronics":
        return new ElectronicsItem(id, name, price, seller);

      case "food":
        return new FoodItem(id, name, price, seller);

      default:
        throw new IllegalArgumentException("Unknown item type: " + type);
    }
  }
}