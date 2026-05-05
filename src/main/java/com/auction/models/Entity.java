package com.auction.models;

import java.util.UUID;

/**
 * Lớp cơ sở trừu tượng (abstract base class) cho các thực thể trong hệ thống,
 * cung cấp thuộc tính định danh duy nhất (ID).
 */
public abstract class Entity {

  protected String id;

  /**
   * Khởi tạo một thực thể mới.
   * Thuộc tính ID sẽ tự động được tạo ngẫu nhiên bằng UUID.
   */
  public Entity() {
    this.id = UUID.randomUUID().toString();
  }

  /**
   * Lấy mã định danh (ID) của thực thể.
   *
   * @return Chuỗi chứa ID của thực thể
   */
  public String getId() {
    return id;
  }

  /**
   * Thiết lập mã định danh (ID) cho thực thể.
   *
   * @param id Mã định danh mới cần thiết lập
   */
  public void setId(String id) {
    this.id = id;
  }
}