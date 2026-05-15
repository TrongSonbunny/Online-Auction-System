package com.auction.factory;

import com.auction.models.Electronics;
import com.auction.models.Item;

/**
 * Lớp Factory chịu trách nhiệm khởi tạo các đối tượng Item.
 */
public class ItemFactory {

  /**
   * Private constructor để ngăn chặn việc khởi tạo class tiện ích này.
   */
  private ItemFactory() {
  }

  /**
   * Tạo một sản phẩm mới dựa trên loại sản phẩm được cung cấp.
   *
   * @param type      Loại sản phẩm (ví dụ: "electronics")
   * @param name      Tên của sản phẩm
   * @param price     Giá khởi điểm của sản phẩm
   * @param extraInfo Thông tin bổ sung (ví dụ: thời gian bảo hành)
   * @return Đối tượng Item tương ứng
   * @throws IllegalArgumentException Nếu loại sản phẩm không được hỗ trợ
   */
  public static Item createItem(String type, String name, double price, int extraInfo) {
    switch (type.toLowerCase()) {
      case "electronics":
        return new Electronics(name, price, extraInfo);
      default:
        throw new IllegalArgumentException("Không hỗ trợ loại sản phẩm này: " + type);
    }
  }
}