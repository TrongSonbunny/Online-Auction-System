package com.auction.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho một phiên đấu giá.
 */
public class Auction extends Entity {

  private Item item;
  private Seller seller;
  private List<BidTransaction> bidHistory;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String status;

  private transient List<AuctionObserver> observers;

  /**
   * Khởi tạo một phiên đấu giá mới.
   *
   * @param item      Sản phẩm được đấu giá
   * @param seller    Người bán sản phẩm
   * @param startTime Thời gian bắt đầu
   * @param endTime   Thời gian kết thúc
   */
  public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
    super();
    this.item = item;
    this.seller = seller;
    this.bidHistory = new ArrayList<>();
    this.startTime = startTime;
    this.endTime = endTime;
    this.status = "OPEN";
    this.observers = new ArrayList<>();
  }

  /**
   * Bắt đầu phiên đấu giá, chuyển trạng thái sang RUNNING.
   */
  public synchronized void startAuction() {
    if ("OPEN".equals(this.status)) {
      this.status = "RUNNING";
      System.out.println("Phiên đấu giá đã bắt đầu! Trạng thái: RUNNING");
    }
  }

  /**
   * Kết thúc phiên đấu giá và thông báo cho các observer.
   */
  public synchronized void endAuction() {
    if ("RUNNING".equals(this.status)) {
      this.status = "FINISHED";
      System.out.println("Phiên đấu giá kết thúc! Trạng thái: FINISHED");

      if (observers != null) {
        for (AuctionObserver obs : observers) {
          obs.onAuctionClosed(this);
        }
      }
    }
  }

  /**
   * Thanh toán cho phiên đấu giá nếu có người thắng hợp lệ.
   */
  public synchronized void payAuction() {
    if ("FINISHED".equals(this.status) && getHighestBidder() != null) {
      this.status = "PAID";
      System.out.println("Giao dịch thành công. Trạng thái: PAID");
    }
  }

  /**
   * Hủy bỏ phiên đấu giá.
   */
  public synchronized void cancelAuction() {
    if ("FINISHED".equals(this.status)) {
      this.status = "CANCELED";
      System.out.println("Giao dịch bị hủy. Trạng thái: CANCELED");
    }
  }

  /**
   * Lấy người trả giá cao nhất hiện tại.
   *
   * @return Người trả giá cao nhất, hoặc null nếu chưa có ai đặt giá
   */
  public synchronized Bidder getHighestBidder() {
    if (bidHistory.isEmpty()) {
      return null;
    }
    return bidHistory.get(bidHistory.size() - 1).getBidder();
  }

  /**
   * Xử lý hành động đặt giá của một người tham gia.
   *
   * @param newBid Giao dịch đặt giá mới
   * @return true nếu đặt giá thành công, false nếu thất bại
   */
  public synchronized boolean placeBid(BidTransaction newBid) {
    if (!"RUNNING".equals(this.status)) {
      System.out.println("[Từ chối] Chỉ có thể đặt giá khi phiên đấu giá "
          + "đang mở (RUNNING).");
      return false;
    }

    double currentPrice = this.item.getCurrentPrice();
    if (newBid.getBidAmount() <= currentPrice) {
      System.out.println("[Từ chối] Giá đặt (" + newBid.getBidAmount()
          + ") phải lớn hơn " + currentPrice);
      return false;
    }

    Bidder currentBidder = newBid.getBidder();
    if (currentBidder.getBalance() < newBid.getBidAmount()) {
      System.out.println("[Từ chối] " + currentBidder.getUsername() + " không đủ số dư!");
      return false;
    }

    Bidder previousHighest = getHighestBidder();
    if (previousHighest != null) {
      previousHighest.setBalance(previousHighest.getBalance() + currentPrice);
    }

    currentBidder.setBalance(currentBidder.getBalance() - newBid.getBidAmount());
    this.bidHistory.add(newBid);
    this.item.setCurrentPrice(newBid.getBidAmount());

    System.out.println("[Thành công] " + currentBidder.getUsername()
        + " đặt $" + newBid.getBidAmount());

    notifyObserversBidPlaced();
    return true;
  }

  /**
   * Thêm một observer vào danh sách theo dõi phiên đấu giá.
   *
   * @param observer Người theo dõi cần thêm
   */
  public void addObserver(AuctionObserver observer) {
    if (observers == null) {
      observers = new ArrayList<>();
    }
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  /**
   * Thông báo cho tất cả observers khi có một lượt đặt giá mới.
   */
  private void notifyObserversBidPlaced() {
    if (observers != null) {
      for (AuctionObserver observer : observers) {
        observer.onBidPlaced(this);
      }
    }
  }

  /**
   * Lấy sản phẩm của phiên đấu giá.
   *
   * @return Đối tượng Item
   */
  public Item getItem() {
    return item;
  }

  /**
   * Lấy người bán của phiên đấu giá.
   *
   * @return Đối tượng Seller
   */
  public Seller getSeller() {
    return seller;
  }

  /**
   * Lấy lịch sử đặt giá.
   *
   * @return Danh sách các giao dịch đặt giá
   */
  public List<BidTransaction> getBidHistory() {
    return bidHistory;
  }

  /**
   * Lấy thời gian bắt đầu.
   *
   * @return Thời gian bắt đầu
   */
  public LocalDateTime getStartTime() {
    return startTime;
  }

  /**
   * Lấy thời gian kết thúc.
   *
   * @return Thời gian kết thúc
   */
  public LocalDateTime getEndTime() {
    return endTime;
  }

  /**
   * Lấy trạng thái hiện tại của phiên đấu giá.
   *
   * @return Trạng thái
   */
  public String getStatus() {
    return status;
  }
}