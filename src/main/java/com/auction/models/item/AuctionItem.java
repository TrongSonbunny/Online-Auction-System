package com.auction.models.item;

import com.auction.models.user.User;
import java.time.LocalDateTime;

/**
  * Lớp trừu tượng đại diện cho sản phẩm đấu giá.
  */
public abstract class AuctionItem {
  protected String id;
  protected String name;
  protected String description;
  protected double startPrice;
  protected double currentPrice;
  protected LocalDateTime startTime;
  protected LocalDateTime endTime;
  protected User seller;

  /**
   * Khởi tạo sản phẩm đấu giá.
   */
  public AuctionItem(String id, String name, double startPrice, User seller) {
    this.id = id;
    this.name = name;
    this.startPrice = startPrice;
    this.currentPrice = startPrice;
    this.seller = seller;
  }

  public User getSeller() {
    return seller;
  }

  public String  getName() {
    return name;
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(double price) {
    this.currentPrice = price;
  }
}