package com.auction.models;

/**
 * Lớp đại diện cho sản phẩm điện tử trong hệ thống đấu giá.
 */
public class Electronics extends Item {

  private int warrantyMonths;

  /**
   * Khởi tạo một sản phẩm điện tử mới.
   *
   * @param name           Tên sản phẩm
   * @param startingPrice  Giá khởi điểm
   * @param warrantyMonths Số tháng bảo hành
   */
  public Electronics(String name, double startingPrice, int warrantyMonths) {
    super(name, startingPrice);
    this.warrantyMonths = warrantyMonths;
  }

  /**
   * Lấy thông tin chi tiết của sản phẩm điện tử.
   *
   * @return Chuỗi chứa thông tin sản phẩm
   */
  @Override
  public String getItemDetails() {
    return "Điện tử: " + name + " - Giá khởi điểm: $" + startingPrice
        + " - Bảo hành: " + warrantyMonths + " tháng";
  }
}