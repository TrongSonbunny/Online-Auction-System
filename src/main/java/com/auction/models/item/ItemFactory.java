package com.auction.models.item;

import java.time.LocalDateTime;

import com.auction.models.user.Seller;

/**
 * Factory tạo các loại AuctionItem.
 * Áp dụng Factory Method Pattern để quản lý việc khởi tạo các loại sản phẩm khác nhau.
 */
public final class ItemFactory {

  /**
   * Constructor riêng tư để ngăn việc khởi tạo lớp tiện ích.
   */
  private ItemFactory() {}

  /**
   * Tạo một đối tượng AuctionItem cụ thể dựa trên loại sản phẩm được cung cấp.
   *
   * @param type loại sản phẩm (art, book, collectible, electronics, fashion, food)
   * @param id mã định danh sản phẩm
   * @param name tên sản phẩm
   * @param description mô tả sản phẩm
   * @param startPrice giá khởi điểm
   * @param startTime thời gian bắt đầu đấu giá
   * @param endTime thời gian kết thúc đấu giá
   * @param seller người bán sản phẩm
   * @return đối tượng AuctionItem tương ứng
   * @throws IllegalArgumentException nếu loại sản phẩm không được hỗ trợ
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
          "Loại sản phẩm không hợp lệ: \"" + type + "\"");
    };
  }
}