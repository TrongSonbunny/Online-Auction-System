package com.auction.backend;

import java.time.LocalDateTime;
 
/** Sản phẩm loại: Nghệ thuật. */
public class ArtItem extends AuctionItem {
 
  /**
   * Khởi tạo sản phẩm Nghệ thuật.
   *
   * @param id          Mã sản phẩm
   * @param name        Tên sản phẩm
   * @param description Mô tả chi tiết
   * @param startPrice  Giá khởi điểm
   * @param startTime   Thời gian bắt đầu
   * @param endTime     Thời gian kết thúc
   * @param seller      Người bán
   */
  public ArtItem(
      String id,
      String name,
      String description,
      double startPrice,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Seller seller) {
    super(id, name, description, startPrice, startTime, endTime, seller);
  }
 
  @Override
  public void printInfo() {
    System.out.println("[Nghệ thuật]"
        + " | Tên: " + getName()
        + " | Mô tả: " + getDescription()
        + " | Giá khởi điểm: " + getStartPrice()
        + " | Giá hiện tại: " + getCurrentPrice()
        + " | Người bán: " + getSeller().getName()
        + " | Bắt đầu: " + getStartTime()
        + " | Kết thúc: " + getEndTime());
  }
}
 
