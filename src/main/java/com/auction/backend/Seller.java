package com.auction.backend;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho người bán (Seller) trong hệ thống đấu giá.
 * Seller đăng sản phẩm lên hệ thống và nhận thông báo khi phiên kết thúc.
 */
public class Seller extends User {

  // Danh sách ID các phiên đấu giá mà Seller đã tạo
  private final List<String> auctionIds;

  // Tổng doanh thu từ các phiên đấu giá đã hoàn tất
  private double totalRevenue;

  /** Constructor khởi tạo Seller. */
  public Seller(String userId, String name, String email, String passwordHash) {
    super(userId, name, email, passwordHash);
    this.auctionIds = new ArrayList<>();
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
   * Nhận thông báo từ phiên đấu giá của mình.
   * Seller quan tâm chủ yếu đến khi phiên kết thúc hoặc bị hủy.
   */
  @Override
  public void update(AuctionEvent event) {
    switch (event.getEventType()) {
      case AUCTION_FINISHED:
        System.out.println(
            "[SELLER " + getName() + "] Phiên "
                + event.getAuctionId()
                + " đã kết thúc! Người mua: "
                + event.getCurrentLeader()
                + " | Giá cuối: "
                + event.getCurrentHighestBid()
                + " | Chờ thanh toán...");
        break;

      case AUCTION_PAID:
        totalRevenue += event.getCurrentHighestBid();
        System.out.println(
            "[SELLER " + getName() + "] Phiên "
                + event.getAuctionId()
                + " đã được thanh toán! Doanh thu nhận được: "
                + event.getCurrentHighestBid()
                + " | Tổng doanh thu: "
                + totalRevenue);
        break;

      case AUCTION_CANCELED:
        System.out.println(
            "[SELLER " + getName() + "] Phiên "
                + event.getAuctionId()
                + " đã bị hủy.");
        break;

      case NEW_BID:
        System.out.println(
            "[SELLER " + getName() + "] Giá mới tại phiên "
                + event.getAuctionId()
                + ": "
                + event.getCurrentHighestBid()
                + " (bởi "
                + event.getCurrentLeader()
                + ")");
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
    return new ArrayList<>(auctionIds); // Trả bản sao để bảo vệ dữ liệu gốc
  }

  public double getTotalRevenue() {
    return totalRevenue;
  }
}
