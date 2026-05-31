package com.auction.backend.observer;

/**
 * Interface observer cho auction event.
 */
public interface AuctionObserver {

  /**
   * Nhận event từ publisher.
   *
   * @param event event được publish
   */
  void update(AuctionEvent event);
}