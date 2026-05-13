package com.auction.models.item;

/**
 * Lớp đại diện cho một sản phẩm đấu giá (Dựa trên thiết kế constructor của
 * bạn).
 */
public class AuctionItem {

  private String itemId;
  private String name;
  private String description;
  private String category; // Đơn giản hóa ItemCategory thành String cho JSON
  private String itemCondition;
  private double estimatedPrice;

  /**
   * Constructor tạo item đấu giá.
   *
   * @param itemId         Mã item
   * @param name           Tên item
   * @param description    Mô tả item
   * @param category       Danh mục item
   * @param itemCondition  Tình trạng item
   * @param estimatedPrice Giá ước tính
   */
  public AuctionItem(String itemId, String name, String description, String category,
      String itemCondition, double estimatedPrice) {
    this.itemId = itemId;
    this.name = name;
    this.description = description;
    this.category = category;
    this.itemCondition = itemCondition;
    this.estimatedPrice = estimatedPrice;
  }

  // Các Getter để Gson có thể truy cập và chuyển đổi thành JSON
  public String getItemId() {
    return itemId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public String getItemCondition() {
    return itemCondition;
  }

  public double getEstimatedPrice() {
    return estimatedPrice;
  }
}