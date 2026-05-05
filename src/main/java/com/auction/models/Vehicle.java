package com.auction.models;

/**
 * Lớp đại diện cho sản phẩm xe cộ trong hệ thống đấu giá.
 */
public class Vehicle extends Item {

  private String brand;
  private int mileage;

  /**
   * Khởi tạo một sản phẩm xe cộ mới.
   *
   * @param name          Tên của xe
   * @param startingPrice Giá khởi điểm
   * @param brand         Hãng sản xuất
   * @param mileage       Số km đã đi (ODO)
   */
  public Vehicle(String name, double startingPrice, String brand, int mileage) {
    super(name, startingPrice);
    this.brand = brand;
    this.mileage = mileage;
  }

  /**
   * Lấy hãng sản xuất của xe.
   *
   * @return Hãng sản xuất
   */
  public String getBrand() {
    return brand;
  }

  /**
   * Thiết lập hãng sản xuất cho xe.
   *
   * @param brand Hãng sản xuất mới
   */
  public void setBrand(String brand) {
    this.brand = brand;
  }

  /**
   * Lấy số km đã đi của xe.
   *
   * @return Số km đã đi
   */
  public int getMileage() {
    return mileage;
  }

  /**
   * Thiết lập số km đã đi cho xe.
   *
   * @param mileage Số km đã đi mới
   */
  public void setMileage(int mileage) {
    this.mileage = mileage;
  }

  /**
   * Lấy thông tin chi tiết của xe.
   *
   * @return Chuỗi chứa thông tin xe cộ
   */
  @Override
  public String getItemDetails() {
    return "Xe cộ: " + name + " (Hãng: " + brand + ", ODO: " + mileage
        + "km) - Giá khởi điểm: $" + startingPrice;
  }
}