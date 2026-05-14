package com.auction.models.item;

import java.util.UUID;

/**
 * Factory tạo {@link AuctionItem} với ID sinh tự động.
 *
 * <p>Dùng thay vì gọi constructor trực tiếp để đảm bảo itemId luôn có định dạng
 * {@code ITEM-XXXXXXXX} (UUID 8 ký tự viết hoa).
 */
public final class ItemFactory {

  /**
   * Private constructor để tránh tạo object.
   */
  private ItemFactory() {
  }

  /**
   * Tạo item mới.
   *
   * @param name tên item
   * @param description mô tả item
   * @param category category item
   * @param itemCondition tình trạng item
   * @param estimatedPrice giá ước tính
   * @return AuctionItem mới
   */
  public static AuctionItem createItem(
      String name,
      String description,
      ItemCategory category,
      String itemCondition,
      double estimatedPrice) {

    return new AuctionItem(
        generateItemId(),
        name,
        description,
        category,
        itemCondition,
        estimatedPrice);
  }

  /**
   * Sinh itemId ngẫu nhiên.
   *
   * @return itemId
   */
  private static String generateItemId() {

    return "ITEM-"
        + UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .toUpperCase();
  }
}