package com.auction.models.item;

import java.time.LocalDateTime;

import com.auction.models.user.Seller;

/** Sản phẩm loại: Sách. */
public class BookItem extends AuctionItem {

  /**
   * Khởi tạo sản phẩm sách cho phiên đấu giá.
   */
  public BookItem(
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
   * In thông tin chi tiết của sản phẩm sách.
   */
  @Override
  public void printInfo() {
    System.out.println("[Sách]"
        + " | Tên: " + getName()
        + " | Mô tả: " + getDescription()
        + " | Giá khởi điểm: " + getStartPrice()
        + " | Giá hiện tại: " + getCurrentPrice()
        + " | Người bán: " + getSeller().getName()
        + " | Bắt đầu: " + getStartTime()
        + " | Kết thúc: " + getEndTime());
  }
}