package com.auction.models.user;

import java.util.List;

import com.auction.backend.core.Auction;
import com.auction.backend.core.AuctionManager;
import com.auction.backend.observer.AuctionEvent;

/**
 * Lớp đại diện cho quản trị viên (Admin).
 * Admin có quyền giám sát toàn bộ các phiên đấu giá và can thiệp khi cần thiết.
 */
public class Admin extends User {

  /**
   * Khởi tạo một quản trị viên mới.
   *
   * @param userId mã định danh Admin
   * @param name tên Admin
   * @param email địa chỉ email
   * @param passwordHash mã băm mật khẩu
   */
  public Admin(String userId, String name, String email, String passwordHash) {
    super(userId, name, email, passwordHash);
  }

  /**
   * Lấy danh sách toàn bộ các phiên đấu giá đang có trong hệ thống.
   *
   * @return danh sách các đối tượng Auction
   */
  public List<Auction> getAllAuctions() {
    return AuctionManager.getInstance().getAuctions();
  }

  /**
   * Hủy một phiên đấu giá dựa trên mã ID.
   *
   * @param auctionId ID của phiên cần hủy
   * @throws IllegalArgumentException nếu không tìm thấy phiên đấu giá tương ứng
   */
  public void cancelAuction(String auctionId) {
    Auction auction = AuctionManager.getInstance().findById(auctionId);

    if (auction == null) {
      throw new IllegalArgumentException("Không tìm thấy phiên đấu giá với ID: " + auctionId);
    }

    auction.cancelAuction();
  }

  /**
   * Gỡ bỏ hoàn toàn một phiên đấu giá khỏi bộ quản lý.
   *
   * @param auctionId ID của phiên cần gỡ bỏ
   */
  public void removeAuction(String auctionId) {
    AuctionManager.getInstance().removeAuction(auctionId);
  }

  /**
   * Ghi log lại mọi sự kiện diễn ra trong hệ thống đấu giá.
   * Admin đóng vai trò là một Logger tổng quát.
   *
   * @param event đối tượng sự kiện chứa thông tin cập nhật
   */
  @Override
  public void update(AuctionEvent event) {
    System.out.println("[ADMIN LOG] Sự kiện hệ thống: " + event);
  }

  @Override
  public String getRole() {
    return "ADMIN";
  }
}