package com.auction.backend.database;

import com.auction.backend.observer.AuctionEvent;

/**
 * Interface lưu trữ dữ liệu auction khi có event xảy ra.
 *
 * <p>Database team implement interface này bằng các DAO tương ứng.
 * Observer chỉ gọi vào đây — không biết chi tiết SQL bên trong.
 *
 * <p>Quy ước payload theo eventType:
 * <ul>
 *   <li>{@code AUCTION_CREATED} — lưu mới auction (INSERT).
 *   <li>{@code AUCTION_STARTED}, {@code AUCTION_FINISHED},
 *       {@code AUCTION_CANCELLED}, {@code AUCTION_EXTENDED} — cập nhật auction (UPDATE).
 *   <li>{@code NEW_BID}, {@code AUTO_BID_PLACED} — lưu bid transaction (INSERT)
 *       và cập nhật auction (UPDATE).
 * </ul>
 */
public interface DataManager {

  /**
   * Xử lý persistence khi nhận được auction event.
   *
   * @param event event cần persist (payload chứa Auction hoặc BidTransaction)
   */
  void persist(AuctionEvent event);
}
