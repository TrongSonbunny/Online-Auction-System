package com.auction.models.user;

import com.auction.backend.bid.BidTransaction;
import com.auction.backend.core.Auction;
import com.auction.backend.core.AuctionStatus;
import com.auction.backend.observer.AuctionEvent;
import com.auction.models.payment.PaymentStrategy;

/**
 * Lớp đại diện cho người mua (Bidder).
 * Có khả năng tham gia đặt giá, theo dõi trạng thái đấu giá và thực hiện thanh toán.
 */
public class Bidder extends User {

  private int totalWins;
  private double totalSpent;
  private final PaymentStrategy paymentStrategy;

  /**
   * Khởi tạo một người mua mới.
   *
   * @param userId mã định danh người dùng
   * @param name tên hiển thị
   * @param email địa chỉ email
   * @param passwordHash mã băm mật khẩu
   * @param paymentStrategy phương thức thanh toán của người mua
   */
  public Bidder(
      String userId,
      String name,
      String email,
      String passwordHash,
      PaymentStrategy paymentStrategy) {
    super(userId, name, email, passwordHash);
    this.paymentStrategy = paymentStrategy;
    this.totalWins = 0;
    this.totalSpent = 0.0;
  }

  /**
   * Thực hiện đặt giá cho một phiên đấu giá cụ thể.
   *
   * @param auction phiên đấu giá mục tiêu
   * @param bidAmount số tiền đặt giá
   * @return đối tượng BidTransaction chứa thông tin lượt đặt giá
   * @throws IllegalStateException nếu phiên không trong trạng thái RUNNING
   * @throws IllegalArgumentException nếu giá đặt không cao hơn giá hiện tại
   */
  public BidTransaction placeBid(Auction auction, double bidAmount) {
    if (auction.getStatus() != AuctionStatus.RUNNING) {
      throw new IllegalStateException("Phiên đấu giá không ở trạng thái RUNNING");
    }

    if (bidAmount <= auction.getCurrentHighestBid()) {
      throw new IllegalArgumentException("Giá đặt không hợp lệ");
    }

    return auction.placeBid(this, bidAmount);
  }

  /**
   * Xử lý các sự kiện từ phiên đấu giá mà Bidder đang quan sát.
   *
   * @param event đối tượng sự kiện chứa thông tin cập nhật
   */
  @Override
  public void update(AuctionEvent event) {
    switch (event.getEventType()) {
      case NEW_BID:
        if (!event.getCurrentLeader().equals(getUserId())) {
          System.out.println("[BIDDER " + getName() + "] Bạn đã bị vượt giá!");
        }
        break;

      case AUCTION_FINISHED:
        if (event.getCurrentLeader().equals(getUserId())) {
          System.out.println("[BIDDER " + getName() + "] Chúc mừng! Bạn đã thắng phiên đấu giá!");
        }
        break;

      default:
        break;
    }
  }

  @Override
  public String getRole() {
    return "BIDDER";
  }

  /*
  * @return tổng số phiên đấu giá đã thắng. */
  public int getTotalWins() {
    return totalWins;
  }

  /** Tăng số lượng phiên thắng lên 1. */
  public void incrementTotalWins() {
    totalWins++;
  }

  /*
  * @return tổng số tiền đã chi trả cho các phiên đấu giá. */
  public double getTotalSpent() {
    return totalSpent;
  }

  /*
  * @param amount số tiền cộng thêm vào tổng chi tiêu. */
  public void addToTotalSpent(double amount) {
    totalSpent += amount;
  }

  /*
  * @return chiến lược thanh toán hiện tại của Bidder. */
  public PaymentStrategy getPaymentStrategy() {
    return paymentStrategy;
  }
}