package com.auction.backend;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton quản lý toàn bộ phiên đấu giá trong hệ thống.
 * Cung cấp các chức năng thêm, xóa, tìm kiếm và lọc phiên đấu giá.
 *
 * <p>Thread-safe nhờ:
 * - Khởi tạo sớm (eager initialization) tránh race condition.
 * - CopyOnWriteArrayList cho phép đọc đồng thời không cần khóa.
 */
public final class AuctionManager {

  // Khởi tạo sớm — thread-safe, không cần synchronized
  private static final AuctionManager INSTANCE = new AuctionManager();

  // CopyOnWriteArrayList: an toàn khi nhiều thread đọc đồng thời
  private final List<Auction> auctions = new CopyOnWriteArrayList<>();

  /** Ngăn khởi tạo từ bên ngoài. */
  private AuctionManager() {}

  /**
   * Trả về instance duy nhất của AuctionManager.
   *
   * @return instance AuctionManager
   */
  public static AuctionManager getInstance() {
    return INSTANCE;
  }

  // ==================== Thêm / Xóa ====================

  /**
   * Đăng ký một phiên đấu giá mới vào hệ thống.
   *
   * @param auction Phiên đấu giá cần thêm
   * @throws IllegalArgumentException nếu auction null hoặc ID đã tồn tại
   */
  public void addAuction(Auction auction) {
    if (auction == null) {
      throw new IllegalArgumentException("Phiên đấu giá không được null.");
    }

    if (findById(auction.getAuctionId()) != null) {
      throw new IllegalArgumentException(
          "Phiên đấu giá với ID '" + auction.getAuctionId() + "' đã tồn tại.");
    }

    auctions.add(auction);
    System.out.println("[QUẢN LÝ] Đã thêm phiên: " + auction.getAuctionId());
  }

  /**
   * Xóa một phiên đấu giá khỏi hệ thống theo ID.
   *
   * @param auctionId ID phiên cần xóa
   * @throws IllegalArgumentException nếu không tìm thấy phiên
   */
  public void removeAuction(String auctionId) {
    boolean removed = auctions.removeIf(
        a -> a.getAuctionId().equals(auctionId));

    if (!removed) {
      throw new IllegalArgumentException(
          "Không tìm thấy phiên đấu giá với ID: " + auctionId);
    }

    System.out.println("[QUẢN LÝ] Đã xóa phiên: " + auctionId);
  }

  // ==================== Tìm kiếm ====================

  /**
   * Tìm phiên đấu giá theo ID.
   *
   * @param auctionId ID phiên cần tìm
   * @return Auction nếu tìm thấy, null nếu không có
   */
  public Auction findById(String auctionId) {
    return auctions.stream()
        .filter(a -> a.getAuctionId().equals(auctionId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Tìm tất cả phiên đấu giá của một người bán.
   *
   * @param sellerId ID người bán
   * @return Danh sách phiên của người bán đó
   */
  public List<Auction> findBySeller(String sellerId) {
    return auctions.stream()
        .filter(a -> a.getSellerId().equals(sellerId))
        .toList();
  }

  // ==================== Lọc theo trạng thái ====================

  /**
   * Lấy tất cả phiên đang mở (OPEN) — chưa bắt đầu.
   *
   * @return Danh sách phiên OPEN
   */
  public List<Auction> getOpenAuctions() {
    return auctions.stream()
        .filter(a -> a.getStatus() == AuctionStatus.OPEN)
        .toList();
  }

  /**
   * Lấy tất cả phiên đang diễn ra (RUNNING).
   *
   * @return Danh sách phiên RUNNING
   */
  public List<Auction> getRunningAuctions() {
    return auctions.stream()
        .filter(a -> a.getStatus() == AuctionStatus.RUNNING)
        .toList();
  }

  /**
   * Lấy tất cả phiên đã kết thúc (FINISHED) — chờ thanh toán.
   *
   * @return Danh sách phiên FINISHED
   */
  public List<Auction> getFinishedAuctions() {
    return auctions.stream()
        .filter(a -> a.getStatus() == AuctionStatus.FINISHED)
        .toList();
  }

  /**
   * Lấy tất cả phiên đã thanh toán (PAID).
   *
   * @return Danh sách phiên PAID
   */
  public List<Auction> getPaidAuctions() {
    return auctions.stream()
        .filter(a -> a.getStatus() == AuctionStatus.PAID)
        .toList();
  }

  /**
   * Lấy tất cả phiên đã bị hủy (CANCELED).
   *
   * @return Danh sách phiên CANCELED
   */
  public List<Auction> getCanceledAuctions() {
    return auctions.stream()
        .filter(a -> a.getStatus() == AuctionStatus.CANCELED)
        .toList();
  }

  // ==================== Getter ====================

  /**
   * Lấy toàn bộ danh sách phiên đấu giá — chỉ đọc.
   *
   * @return Danh sách không thể sửa đổi
   */
  public List<Auction> getAuctions() {
    return Collections.unmodifiableList(auctions);
  }

  /**
   * Trả về số lượng phiên đấu giá hiện có.
   *
   * @return Số phiên
   */
  public int getAuctionCount() {
    return auctions.size();
  }

  @Override
  public String toString() {
    return "AuctionManager{"
        + "tổng số phiên=" + auctions.size()
        + ", đang mở=" + getOpenAuctions().size()
        + ", đang chạy=" + getRunningAuctions().size()
        + ", đã kết thúc=" + getFinishedAuctions().size()
        + "}";
  }
}