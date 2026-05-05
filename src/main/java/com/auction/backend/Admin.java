package com.auction.backend;

import java.util.List;

/**
 * Lớp đại diện cho quản trị viên (Admin) trong hệ thống.
 * Admin có toàn quyền quản lý: giám sát, hủy phiên, xóa phiên.
 */
public class Admin extends User {

  /**
   * Khởi tạo Admin.
   *
   * @param userId       Mã định danh
   * @param name         Họ tên
   * @param email        Email
   * @param passwordHash Mật khẩu đã hash
   */
  public Admin(String userId, String name, String email, String passwordHash) {
    super(userId, name, email, passwordHash);
  }

  // ==================== Giám sát hệ thống ====================

  /**
   * Lấy toàn bộ danh sách phiên đấu giá trong hệ thống.
   *
   * @return Danh sách tất cả phiên
   */
  public List<Auction> getAllAuctions() {
    return AuctionManager.getInstance().getAuctions();
  }

  /**
   * Lấy danh sách phiên đang mở (OPEN).
   *
   * @return Danh sách phiên OPEN
   */
  public List<Auction> getOpenAuctions() {
    return AuctionManager.getInstance().getOpenAuctions();
  }

  /**
   * Lấy danh sách phiên đang diễn ra (RUNNING).
   *
   * @return Danh sách phiên RUNNING
   */
  public List<Auction> getRunningAuctions() {
    return AuctionManager.getInstance().getRunningAuctions();
  }

  /**
   * Lấy danh sách phiên đã kết thúc (FINISHED).
   *
   * @return Danh sách phiên FINISHED
   */
  public List<Auction> getFinishedAuctions() {
    return AuctionManager.getInstance().getFinishedAuctions();
  }

  /**
   * Lấy danh sách phiên đã thanh toán (PAID).
   *
   * @return Danh sách phiên PAID
   */
  public List<Auction> getPaidAuctions() {
    return AuctionManager.getInstance().getPaidAuctions();
  }

  /**
   * Lấy danh sách phiên đã bị hủy (CANCELED).
   *
   * @return Danh sách phiên CANCELED
   */
  public List<Auction> getCanceledAuctions() {
    return AuctionManager.getInstance().getCanceledAuctions();
  }

  /**
   * Tìm một phiên đấu giá theo ID.
   *
   * @param auctionId ID phiên cần tìm
   * @return Auction nếu tìm thấy, null nếu không có
   */
  public Auction findAuction(String auctionId) {
    return AuctionManager.getInstance().findById(auctionId);
  }

  /**
   * Tìm tất cả phiên đấu giá của một người bán.
   *
   * @param sellerId ID người bán
   * @return Danh sách phiên của người bán đó
   */
  public List<Auction> getAuctionsBySeller(String sellerId) {
    return AuctionManager.getInstance().findBySeller(sellerId);
  }

  // ==================== Quản lý phiên ====================

  /**
   * Hủy một phiên đấu giá bất kỳ.
   *
   * @param auctionId ID phiên cần hủy
   * @throws IllegalArgumentException nếu không tìm thấy phiên
   */
  public void cancelAuction(String auctionId) {
    Auction auction = AuctionManager.getInstance().findById(auctionId);
    if (auction == null) {
      throw new IllegalArgumentException(
          "Không tìm thấy phiên đấu giá với ID: " + auctionId);
    }
    auction.cancelAuction();
    System.out.println("[ADMIN " + getName()
        + "] Đã hủy phiên: " + auctionId);
  }

  /**
   * Xóa một phiên đấu giá khỏi hệ thống.
   *
   * @param auctionId ID phiên cần xóa
   */
  public void removeAuction(String auctionId) {
    AuctionManager.getInstance().removeAuction(auctionId);
    System.out.println("[ADMIN " + getName()
        + "] Đã xóa phiên: " + auctionId);
  }

  /**
   * In thống kê tổng quan hệ thống.
   */
  public void printSystemStats() {
    System.out.println("===== THỐNG KÊ HỆ THỐNG =====");
    System.out.println("Tổng số phiên   : "
        + AuctionManager.getInstance().getAuctionCount());
    System.out.println("Đang mở (OPEN)  : "
        + getOpenAuctions().size());
    System.out.println("Đang chạy       : "
        + getRunningAuctions().size());
    System.out.println("Đã kết thúc     : "
        + getFinishedAuctions().size());
    System.out.println("Đã thanh toán   : "
        + getPaidAuctions().size());
    System.out.println("Đã hủy          : "
        + getCanceledAuctions().size());
    System.out.println("==============================");
  }

  // ==================== Observer ====================

  /**
   * Admin nhận log tất cả sự kiện trong hệ thống để giám sát.
   */
  @Override
  public void update(AuctionEvent event) {
    System.out.println("[ADMIN LOG] " + event.toString());
  }

  @Override
  public String getRole() {
    return "ADMIN";
  }
}