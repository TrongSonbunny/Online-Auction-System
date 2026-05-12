package com.auction.models.item;

import com.auction.exceptions.AuctionException;
import java.util.Objects;

/**
 * Đại diện cho item được đưa lên đấu giá.
 */
public class AuctionItem {

  private final String itemId;

  private String name;

  private String description;

  private ItemCategory category;

  private String itemCondition;

  private double estimatedPrice;

  /**
   * Constructor tạo item đấu giá.
   *
   * @param itemId mã item
   * @param name tên item
   * @param description mô tả item
   * @param category danh mục item
   * @param itemCondition tình trạng item
   * @param estimatedPrice giá ước tính
   */
  public AuctionItem(
      String itemId,
      String name,
      String description,
      ItemCategory category,
      String itemCondition,
      double estimatedPrice) {

    validateItemId(itemId);
    validateName(name);
    validateDescription(description);
    validateEstimatedPrice(estimatedPrice);

    this.itemId = itemId;
    this.name = name;
    this.description = description;
    this.category = Objects.requireNonNull(
        category,
        "Category không được null.");

    this.itemCondition = Objects.requireNonNull(
        itemCondition,
        "Tình trạng item không được null.");

    this.estimatedPrice = estimatedPrice;
  }

  /**
   * Validate itemId.
   *
   * @param id mã item
   */
  private void validateItemId(String id) {

    if (id == null || id.isBlank()) {
      throw new AuctionException(
          "ItemId không hợp lệ.");
    }
  }

  /**
   * Validate tên item.
   *
   * @param itemName tên item
   */
  private void validateName(String itemName) {

    if (itemName == null || itemName.isBlank()) {
      throw new AuctionException(
          "Tên item không hợp lệ.");
    }
  }

  /**
   * Validate mô tả item.
   *
   * @param itemDescription mô tả item
   */
  private void validateDescription(
      String itemDescription) {

    if (itemDescription == null
        || itemDescription.isBlank()) {

      throw new AuctionException(
          "Mô tả item không hợp lệ.");
    }
  }

  /**
   * Validate giá ước tính.
   *
   * @param price giá ước tính
   */
  private void validateEstimatedPrice(
      double price) {

    if (price < 0) {
      throw new AuctionException(
          "Giá ước tính không được âm.");
    }
  }

  public String getItemId() {
    return itemId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public ItemCategory getCategory() {
    return category;
  }

  public String getItemCondition() {
    return itemCondition;
  }

  public double getEstimatedPrice() {
    return estimatedPrice;
  }

  /**
   * Cập nhật tên item.
   *
   * @param newName tên mới
   */
  public void setName(String newName) {

    validateName(newName);
    this.name = newName;
  }

  /**
   * Cập nhật mô tả item.
   *
   * @param newDescription mô tả mới
   */
  public void setDescription(
      String newDescription) {

    validateDescription(newDescription);
    this.description = newDescription;
  }

  /**
   * Cập nhật category.
   *
   * @param newCategory category mới
   */
  public void setCategory(
      ItemCategory newCategory) {

    this.category = Objects.requireNonNull(
        newCategory,
        "Category không được null.");
  }

  /**
   * Cập nhật tình trạng item.
   *
   * @param newCondition tình trạng mới
   */
  public void setItemCondition(
      String newCondition) {

    if (newCondition == null
        || newCondition.isBlank()) {

      throw new AuctionException(
          "Tình trạng item không hợp lệ.");
    }

    this.itemCondition = newCondition;
  }

  /**
   * Cập nhật giá ước tính.
   *
   * @param newPrice giá mới
   */
  public void setEstimatedPrice(
      double newPrice) {

    validateEstimatedPrice(newPrice);
    this.estimatedPrice = newPrice;
  }

  @Override
  public String toString() {

    return "AuctionItem{"
        + "itemId='"
        + itemId
        + '\''
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", category="
        + category
        + ", itemCondition='"
        + itemCondition
        + '\''
        + ", estimatedPrice="
        + estimatedPrice
        + '}';
  }
}