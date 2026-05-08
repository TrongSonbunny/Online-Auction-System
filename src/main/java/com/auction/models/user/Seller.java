package com.auction.models.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.auction.backend.core.Auction;
import com.auction.backend.core.AuctionManager;
import com.auction.backend.observer.AuctionEvent;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemFactory;

/**
 * Lớp đại diện cho người bán (Seller).
 * Có khả năng tạo sản phẩm, mở phiên đấu giá và theo dõi doanh thu từ các phiên đã thanh toán.
 */
public class Seller extends User {

  private final List<String> auctionIds;
  private final List<AuctionItem> items;
  private double totalRevenue;

  /**
   * Khởi tạo một người bán mới.
   *
   * @param userId mã định danh người dùng
   * @param name tên người bán
   * @param email địa chỉ email
   * @param passwordHash mã băm mật khẩu
   */
  public Seller(String userId, String name, String email, String passwordHash) {
    super(userId, name, email, passwordHash);
    this.auctionIds = new ArrayList<>();
    this.items = new ArrayList<>();
    this.totalRevenue = 0.0;
  }

  /**
   * Đăng ký một phiên đấu giá mới do Seller này tạo.
   *
   * @param auctionId ID phiên đấu giá vừa tạo
   */
  public void addAuction(String auctionId) {
    auctionIds.add(auctionId);
  }

  /**
   * Tạo một sản phẩm mới thông qua ItemFactory và lưu vào danh sách của người bán.
   *
   * @return đối tượng AuctionItem vừa tạo
   */
  public AuctionItem createItem(
      String type,
      String id,
      String name,
      String description,
      double startPrice,
      LocalDateTime startTime,
      LocalDateTime endTime) {

    AuctionItem item = ItemFactory.createItem(
        type, id, name, description, startPrice, startTime, endTime, this);

    items.add(item);
    return item;
  }

  /**
   * Tạo một phiên đấu giá mới cho sản phẩm và đăng ký người bán làm người quan sát.
   *
   * @return đối tượng Auction vừa tạo
   */
  public Auction createAuction(
      String auctionId,
      AuctionItem item,
      double startingPrice,
      LocalDateTime endTime) {

    Auction auction = new Auction(
        auctionId, item, getUserId(), startingPrice, endTime);

    AuctionManager.getInstance().addAuction(auction);
    auctionIds.add(auctionId);
    auction.registerObserver(this);

    return auction;
  }

  /**
   * Cập nhật doanh thu khi nhận được thông báo phiên đấu giá đã thanh toán.
   *
   * @param event sự kiện đấu giá
   */
  @Override
  public void update(AuctionEvent event) {
    if (event.getEventType() == AuctionEvent.EventType.AUCTION_PAID) {
      totalRevenue += event.getCurrentHighestBid();
    }
  }

  @Override
  public String getRole() {
    return "SELLER";
  }

  /*
  * @return danh sách ID các phiên đấu giá của người bán (chỉ đọc). */
  public List<String> getAuctionIds() {
    return Collections.unmodifiableList(auctionIds);
  }

  /*
  * @return danh sách các sản phẩm của người bán (chỉ đọc). */
  public List<AuctionItem> getItems() {
    return Collections.unmodifiableList(items);
  }

  /*
  * @return tổng doanh thu tích lũy từ các phiên đã hoàn tất thanh toán. */
  public double getTotalRevenue() {
    return totalRevenue;
  }
}