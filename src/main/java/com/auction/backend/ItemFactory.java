package com.auction.backend;

import java.time.LocalDateTime;

/**
 * Factory tạo các loại AuctionItem.
 * Áp dụng Factory Method Pattern — client không cần biết lớp cụ thể nào được tạo.
 */
public final class ItemFactory {

  /** Ngăn khởi tạo lớp tiện ích này. */
  private ItemFactory() {}

  /**
   * Tạo một AuctionItem theo loại chỉ định.
   *
   * @param type        Loại sản phẩm: "art", "book", "collectible",
   *                    "electronics", "fashion", "food"
   * @param id          Mã sản phẩm
   * @param name        Tên sản phẩm
   * @param description Mô tả chi tiết
   * @param startPrice  Giá khởi điểm
   * @param startTime   Thời gian bắt đầu phiên đấu giá
   * @param endTime     Thời gian kết thúc phiên đấu giá
   * @param seller      Người bán (chủ sở hữu sản phẩm)
   * @return AuctionItem tương ứng với loại được chỉ định
   * @throws IllegalArgumentException nếu loại sản phẩm không hợp lệ
   */
  public static AuctionItem createItem(
      String type,
      String id,
      String name,
      String description,
      double startPrice,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Seller seller) {

    return switch (type.toLowerCase()) {
      case "art" -> new ArtItem(
          id, name, description, startPrice, startTime, endTime, seller);
      case "book" -> new BookItem(
          id, name, description, startPrice, startTime, endTime, seller);
      case "collectible" -> new CollectibleItem(
          id, name, description, startPrice, startTime, endTime, seller);
      case "electronics" -> new ElectronicsItem(
          id, name, description, startPrice, startTime, endTime, seller);
      case "fashion" -> new FashionItem(
          id, name, description, startPrice, startTime, endTime, seller);
      case "food" -> new FoodItem(
          id, name, description, startPrice, startTime, endTime, seller);
      default -> throw new IllegalArgumentException(
          "Loại sản phẩm không hợp lệ: \"" + type + "\". "
          + "Các loại hợp lệ: art, book, collectible, electronics, fashion, food.");
    };
  }
}