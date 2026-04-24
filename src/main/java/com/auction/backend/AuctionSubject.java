package com.auction.backend;

/**
 * Interface định nghĩa hành động của Subject trong Observer Pattern.
 * Lớp Auction sẽ triển khai interface này để quản lý danh sách Observer
 * và gửi thông báo khi có sự kiện mới.
 */
public interface AuctionSubject {

  /**
   * Đăng ký một Observer vào danh sách của phiên đấu giá.
   *
   * @param observer Observer muốn đăng ký
   */
  void registerObserver(AuctionObserver observer);

  /**
   * Hủy đăng ký một Observer khỏi danh sách.
   *
   * @param observer Observer muốn hủy đăng ký
   */
  void removeObserver(AuctionObserver observer);

  /**
   * Gửi thông báo sự kiện đến tất cả Observer đang đăng ký.
   *
   * @param event Sự kiện đấu giá cần broadcast
   */
  void notifyObservers(AuctionEvent event);
}
