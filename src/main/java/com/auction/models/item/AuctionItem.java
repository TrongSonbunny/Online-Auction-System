package com.auction.models.item;

import java.time.LocalDateTime;

import com.auction.models.user.Seller;

/**
 * Lớp trừu tượng đại diện cho sản phẩm đấu giá.
 * Là lớp cơ sở cho các loại mặt hàng cụ thể trong hệ thống.
 */
public abstract class AuctionItem {

  protected String id;
  protected String name;
  protected String description;
  protected double startPrice;
  protected double currentPrice;
  protected LocalDateTime startTime;
  protected LocalDateTime endTime;
  protected Seller seller;

  /**
   * Khởi tạo sản phẩm đấu giá.
   *
   * @param id mã định danh duy nhất của sản phẩm
   * @param name tên sản phẩm
   * @param description mô tả chi tiết sản phẩm
   * @param startPrice giá khởi điểm
   * @param startTime thời điểm bắt đầu đấu giá
   * @param endTime thời điểm kết thúc đấu giá
   * @param seller đối tượng người bán sở hữu sản phẩm
   */
  public AuctionItem(
      String id,
      String name,
      String description,
      double startPrice,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Seller seller) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.startPrice = startPrice;
    this.currentPrice = startPrice;
    this.startTime = startTime;
    this.endTime = endTime;
    this.seller = seller;
  }

  /*
  * @return đối tượng người bán sản phẩm. */
  public Seller getSeller() {
    return seller;
  }

  /*
  * @return tên của sản phẩm. */
  public String getName() {
    return name;
  }

  /*
  * @return giá hiện tại của sản phẩm (giá cao nhất đã đặt). */
  public double getCurrentPrice() {
    return currentPrice;
  }

  /*
  * @return mã định danh của sản phẩm. */
  public String getId() {
    return id;
  }

  /*
   @return giá khởi điểm ban đầu. */
  public double getStartPrice() {
    return startPrice;
  }

  /*
  * @return mô tả chi tiết về sản phẩm. */
  public String getDescription() {
    return description;
  }

  /*
  * @return thời gian bắt đầu phiên đấu giá sản phẩm. */
  public LocalDateTime getStartTime() {
    return startTime;
  }

  /*
  * @return thời gian kết thúc phiên đấu giá sản phẩm. */
  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public void setCurrentPrice(double price) {
    this.currentPrice = price;
  }

  /**
   * In thông tin chi tiết của sản phẩm ra console.
   */
  public void printInfo() {
    System.out.println("Sản phẩm: " + name
        + " | Giá khởi điểm: " + startPrice
        + " | Giá hiện tại: " + currentPrice
        + " | Người bán: " + seller.getName());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{id='" + id + '\''
        + ", name='" + name + '\''
        + ", currentPrice=" + currentPrice + '}';
  }
}