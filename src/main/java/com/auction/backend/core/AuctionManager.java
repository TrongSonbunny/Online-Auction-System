package com.auction.backend.core;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lớp quản lý danh sách các phiên đấu giá trong hệ thống.
 * Sử dụng Singleton Pattern để đảm bảo chỉ có một bộ quản lý duy nhất.
 */
public final class AuctionManager {

  private static final AuctionManager INSTANCE = new AuctionManager();

  private final List<Auction> auctions = new CopyOnWriteArrayList<>();

  /**
   * Constructor riêng tư để ngăn chặn việc khởi tạo từ bên ngoài.
   */
  private AuctionManager() {}

  /**
   * Lấy thực thể duy nhất của AuctionManager.
   *
   * @return thực thể AuctionManager
   */
  public static AuctionManager getInstance() {
    return INSTANCE;
  }

  /**
   * Thêm một phiên đấu giá mới vào hệ thống.
   *
   * @param auction phiên đấu giá cần thêm
   * @throws IllegalArgumentException nếu auction là null
   */
  public void addAuction(Auction auction) {
    if (auction == null) {
      throw new IllegalArgumentException("Phiên đấu giá không được null.");
    }
    auctions.add(auction);
  }

  /**
   * Xóa một phiên đấu giá khỏi hệ thống dựa trên ID.
   *
   * @param auctionId ID của phiên đấu giá cần xóa
   */
  public void removeAuction(String auctionId) {
    auctions.removeIf(a -> a.getAuctionId().equals(auctionId));
  }

  /**
   * Tìm kiếm một phiên đấu giá theo ID.
   *
   * @param auctionId ID phiên đấu giá cần tìm
   * @return đối tượng Auction hoặc null nếu không tìm thấy
   */
  public Auction findById(String auctionId) {
    return auctions.stream()
        .filter(a -> a.getAuctionId().equals(auctionId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Lấy danh sách tất cả các phiên đấu giá (không thể sửa đổi trực tiếp).
   *
   * @return danh sách các phiên đấu giá
   */
  public List<Auction> getAuctions() {
    return Collections.unmodifiableList(auctions);
  }
}