package com.auction.models;

import java.io.Serializable;

/**
 * Lớp cơ sở trừu tượng đại diện cho một sản phẩm trong hệ thống đấu giá.
 */
public abstract class Item extends Entity implements Serializable {

  protected String name;
  protected double startingPrice;
  protected double currentPrice;

  /**
   * Khởi tạo một sản phẩm mới.
   *
   * @param name          Tên của sản phẩm
   * @param startingPrice Giá khởi điểm của sản phẩm
   */
  public Item(String name, double startingPrice) {
    super();
    this.name = name;
    this.startingPrice = startingPrice;
    // Sửa lỗi logic: Giá hiện tại ban đầu nên bằng giá khởi điểm
    this.currentPrice = startingPrice;
  }

  /**
   * Lấy tên của sản phẩm.
   *
   * @return Tên sản phẩm
   */
  public String getName() {
    return name;
  }

  /**
   * Thiết lập tên cho sản phẩm.
   *
   * @param name Tên mới của sản phẩm
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Lấy giá khởi điểm của sản phẩm.
   *
   * @return Giá khởi điểm
   */
  public double getStartingPrice() {
    return startingPrice;
  }

  /**
   * Thiết lập giá khởi điểm cho sản phẩm.
   *
   * @param startingPrice Giá khởi điểm mới
   */
  public void setStartingPrice(double startingPrice) {
    this.startingPrice = startingPrice;
  }

  /**
   * Lấy giá hiện tại của sản phẩm.
   *
   * @return Giá hiện tại
   */
  public double getCurrentPrice() {
    return currentPrice;
  }

  /**
   * Thiết lập giá hiện tại cho sản phẩm.
   *
   * @param currentPrice Giá hiện tại mới
   */
  public void setCurrentPrice(double currentPrice) {
    this.currentPrice = currentPrice;
  }

  /**
   * Lấy thông tin chi tiết của sản phẩm.
   * Các lớp con kế thừa phải tự triển khai phương thức này.
   *
   * @return Chuỗi chứa thông tin chi tiết của sản phẩm
   */
  public abstract String getItemDetails();
}