package com.auction.models.item;

import java.time.LocalDateTime;

import com.auction.models.user.Seller;

/** Sản phẩm loại: Nghệ thuật. */
public class ArtItem extends AuctionItem {

  /**
   * Khởi tạo sản phẩm nghệ thuật.
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

  /**
   * In thông tin chi tiết sản phẩm nghệ thuật.
   */
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