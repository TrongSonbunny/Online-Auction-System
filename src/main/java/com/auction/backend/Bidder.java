package com.auction.backend;

/**
 * Lớp đại diện cho người mua (Bidder) trong hệ thống đấu giá.
 * Bidder có thể đặt giá và nhận thông báo khi có người khác trả giá cao hơn.
 */
public class Bidder extends User {

  // Tổng số lần đấu giá thành công (đã thắng)
  private int totalWins;

  // Tổng số tiền đã chi qua các phiên đấu giá
  private double totalSpent;

  /** Constructor khởi tạo Bidder. */
  public Bidder(String userId, String name, String email, String passwordHash) {
    super(userId, name, email, passwordHash);
    this.totalWins = 0;
    this.totalSpent = 0.0;
  }

  /**
   * Đặt giá vào một phiên đấu giá.
   * Tạo BidTransaction và gửi yêu cầu lên phiên đấu giá.
   *
   * @param auction Phiên đấu giá muốn tham gia
   * @param bidAmount Số tiền muốn đặt
   * @return BidTransaction nếu đặt giá thành công
   * @throws IllegalArgumentException nếu giá đặt không hợp lệ
   * @throws IllegalStateException nếu phiên đấu giá không còn mở
   */
  public BidTransaction placeBid(Auction auction, double bidAmount) {
    if (auction.getStatus() != AuctionStatus.RUNNING) {
      throw new IllegalStateException(
          "Không thể đặt giá: phiên đấu giá không ở trạng thái RUNNING. "
              + "Trạng thái hiện tại: "
              + auction.getStatus());
    }

    if (bidAmount <= auction.getCurrentHighestBid()) {
      throw new IllegalArgumentException(
          "Giá đặt (" + bidAmount + ") phải cao hơn giá hiện tại ("
              + auction.getCurrentHighestBid()
              + ")");
    }

    return auction.placeBid(this, bidAmount);
  }

  /**
   * Nhận thông báo từ phiên đấu giá đang theo dõi.
   * Cập nhật GUI khi có người khác trả giá cao hơn.
   */
  @Override
  public void update(AuctionEvent event) {
    switch (event.getEventType()) {
      case NEW_BID:
        if (!event.getCurrentLeader().equals(getUserId())) {
          System.out.println(
              "[BIDDER " + getName() + "] Bạn vừa bị vượt qua!"
                  + " Giá mới: "
                  + event.getCurrentHighestBid()
                  + " bởi "
                  + event.getCurrentLeader());
        } else {
          System.out.println(
              "[BIDDER " + getName() + "] Bạn đang dẫn đầu với giá: "
                  + event.getCurrentHighestBid());
        }
        break;

      case AUCTION_FINISHED:
        if (event.getCurrentLeader().equals(getUserId())) {
          System.out.println(
              "[BIDDER " + getName() + "] Chúc mừng! Bạn đã thắng phiên "
                  + event.getAuctionId()
                  + " với giá: "
                  + event.getCurrentHighestBid());
        } else {
          System.out.println(
              "[BIDDER " + getName() + "] Phiên "
                  + event.getAuctionId()
                  + " đã kết thúc. Người thắng: "
                  + event.getCurrentLeader());
        }
        break;

      case AUCTION_CANCELED:
        System.out.println(
            "[BIDDER " + getName() + "] Phiên "
                + event.getAuctionId()
                + " đã bị hủy.");
        break;

      default:
        break;
    }
  }

  @Override
  public String getRole() {
    return "BIDDER";
  }

  // ==================== Getter & Setter ====================

  public int getTotalWins() {
    return totalWins;
  }

  public void incrementTotalWins() {
    this.totalWins++;
  }

  public double getTotalSpent() {
    return totalSpent;
  }

  public void addToTotalSpent(double amount) {
    this.totalSpent += amount;
  }
}