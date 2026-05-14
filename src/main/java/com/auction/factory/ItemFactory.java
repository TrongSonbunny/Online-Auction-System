package com.auction.factory;

import com.auction.models.Art;
import com.auction.models.Electronics;
import com.auction.models.Item;
import com.auction.models.Vehicle;

/**
 * Lớp Factory chịu trách nhiệm khởi tạo các đối tượng Item.
 * Áp dụng mẫu thiết kế Factory Method kết hợp Nạp chồng phương thức
 * (Overloading).
 */
public class ItemFactory {

  /**
   * Private constructor để ngăn chặn việc khởi tạo class tiện ích này.
   */
  private ItemFactory() {
  }

  /**
   * Tạo một sản phẩm mới (Dành cho đồ điện tử - chỉ cần 1 thông số phụ).
   *
   * @param type      Loại sản phẩm (ví dụ: "electronics")
   * @param name      Tên của sản phẩm
   * @param price     Giá khởi điểm của sản phẩm
   * @param extraInfo Thông tin bổ sung dạng số (ví dụ: thời gian bảo hành)
   * @return Đối tượng Item tương ứng
   * @throws IllegalArgumentException Nếu loại sản phẩm không được hỗ trợ
   */
  public static Item createItem(String type, String name, double price, int extraInfo) {
    if ("electronics".equalsIgnoreCase(type)) {
      return new Electronics(name, price, extraInfo);
    }
    throw new IllegalArgumentException(
        "Vui lòng dùng hàm createItem có chứa tham số chuỗi cho loại: " + type);
  }

  /**
   * Tạo một sản phẩm mới (Dành cho Nghệ thuật và Xe cộ - cần 2 thông số phụ).
   *
   * @param type     Loại sản phẩm ("art", "vehicle")
   * @param name     Tên của sản phẩm
   * @param price    Giá khởi điểm của sản phẩm
   * @param extraStr Thông tin bổ sung dạng chuỗi (Tên tác giả, Hãng sản xuất)
   * @param extraInt Thông tin bổ sung dạng số (Năm sáng tác, Số Km đã đi)
   * @return Đối tượng Item tương ứng
   * @throws IllegalArgumentException Nếu loại sản phẩm không được hỗ trợ
   */
  public static Item createItem(String type, String name, double price, String extraStr,
      int extraInt) {
    switch (type.toLowerCase()) {
      case "art":
        return new Art(name, price, extraStr, extraInt);
      case "vehicle":
        return new Vehicle(name, price, extraStr, extraInt);
      default:
        throw new IllegalArgumentException("Không hỗ trợ loại sản phẩm này: " + type);
    }
  }
}