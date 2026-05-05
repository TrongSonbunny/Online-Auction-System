package com.auction.backend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp đại diện cho người bán (Seller) trong hệ thống đấu giá.
 * Seller đăng sản phẩm, tạo phiên đấu giá và nhận thông báo khi phiên kết thúc.
 */
public class Seller extends User {

  // Danh sách ID các phiên đấu giá mà Seller đã tạo
  private final List<String> auctionIds;

  // Danh sách sản phẩm mà Seller sở hữu
  private final List<AuctionItem> items;

  // Tổng doanh thu từ các phiên đấu giá đã hoàn tất
  private double totalRevenue;

  /**
   * Khởi tạo Seller.
   *
   * @param userId       Mã định danh
   * @param name         Họ tên
   * @param email        Email
   * @param passwordHash Mật khẩu đã hash
   */
  public Seller(String userId, String name, String email, String passwordHash) {
    super(userId, name, email, passwordHash);
    this.auctionIds = new ArrayList<>();
    this.items = new ArrayList<>();
    this.totalRevenue = 0.0;
  }

  // ==================== Quản lý sản phẩm ====================

  /**
   * Tạo một sản phẩm đấu giá và lưu vào danh sách của Seller.
   *
   * @param type        Loại sản phẩm: art, book, collectible, electronics, fashion, food
   * @param id          Mã sản phẩm
   * @param name        Tên sản phẩm
   * @param description Mô tả chi tiết
   * @param startPrice  Giá khởi điểm
   * @param startTime   Thời gian bắt đầu
   * @param endTime     Thời gian kết thúc
   * @return AuctionItem vừa tạo
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
    System.out.println("[SELLER] " + getName()
        + " đã tạo sản phẩm: " + name);
    return item;
  }

  /**
   * Xóa sản phẩm khỏi danh sách theo ID.
   *
   * @param itemId ID sản phẩm cần xóa
   */
  public void removeItem(String itemId) {
    boolean removed = items.removeIf(i -> i.getId().equals(itemId));
    if (!removed) {
      throw new IllegalArgumentException(
          "Không tìm thấy sản phẩm với ID: " + itemId);
    }
    System.out.println("[SELLER] Đã xóa sản phẩm: " + itemId);
  }

  // ==================== Quản lý phiên đấu giá ====================

  /**
   * Tạo một phiên đấu giá mới và đăng ký vào AuctionManager.
   *
   * @param auctionId    Mã phiên đấu giá
   * @param item         Sản phẩm đưa ra đấu giá
   * @param startingPrice Giá khởi điểm
   * @param endTime      Thời gian kết thúc dự kiến
   * @return Auction vừa tạo
   */
  public Auction createAuction(
      String auctionId,
      AuctionItem item,
      double startingPrice,
      LocalDateTime endTime) {
    Auction auction = new Auction(
        auctionId, item, this.getUserId(), startingPrice, endTime);

    AuctionManager.getInstance().addAuction(auction);
    auctionIds.add(auctionId);

    // Seller tự đăng ký theo dõi phiên của mình
    auction.registerObserver(this);

    System.out.println("[SELLER] " + getName()
        + " đã tạo phiên đấu giá: " + auctionId);
    return auction;
  }

  /**
   * Đăng ký một ID phiên đấu giá đã tạo từ trước.
   *
   * @param auctionId ID phiên cần đăng ký
   */
  public void addAuction(String auctionId) {
    auctionIds.add(auctionId);
  }

  // ==================== Observer ====================

  /**
   * Nhận thông báo từ phiên đấu giá của mình.
   * Seller quan tâm khi phiên kết thúc, được thanh toán hoặc bị hủy.
   */
  @Override
  public void update(AuctionEvent event) {
    switch (event.getEventType()) {
      case NEW_BID:
        System.out.println("[SELLER " + getName() + "] Giá mới tại phiên "
            + event.getAuctionId() + ": " + event.getCurrentHighestBid()
            + " (bởi " + event.getCurrentLeader() + ")");
        break;

      case AUCTION_FINISHED:
        System.out.println("[SELLER " + getName() + "] Phiên "
            + event.getAuctionId() + " đã kết thúc!"
            + " Người mua: " + event.getCurrentLeader()
            + " | Giá cuối: " + event.getCurrentHighestBid()
            + " | Chờ thanh toán...");
        break;

      case AUCTION_PAID:
        totalRevenue += event.getCurrentHighestBid();
        System.out.println("[SELLER " + getName() + "] Phiên "
            + event.getAuctionId() + " đã được thanh toán!"
            + " Doanh thu: " + event.getCurrentHighestBid()
            + " | Tổng doanh thu: " + totalRevenue);
        break;

      case AUCTION_CANCELED:
        System.out.println("[SELLER " + getName() + "] Phiên "
            + event.getAuctionId() + " đã bị hủy.");
        break;

      default:
        break;
    }
  }

  @Override
  public String getRole() {
    return "SELLER";
  }

  // ==================== Getter ====================

  public List<String> getAuctionIds() {
    return Collections.unmodifiableList(auctionIds);
  }

  public List<AuctionItem> getItems() {
    return Collections.unmodifiableList(items);
  }

  public double getTotalRevenue() {
    return totalRevenue;
  }
}