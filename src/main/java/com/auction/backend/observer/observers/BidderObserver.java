package com.auction.backend.observer.observers;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionObserver;

/**
 * Observer dành cho bidder.
 */
public class BidderObserver
    implements AuctionObserver {

  private final String bidderName;

  /**
   * Constructor bidder observer.
   *
   * @param bidderName tên bidder
   */
  public BidderObserver(
      String bidderName) {

    if (bidderName == null
        || bidderName.isBlank()) {

      throw new IllegalArgumentException(
          "Bidder name không hợp lệ.");
    }

    this.bidderName = bidderName;
  }

  @Override
  public void update(
      AuctionEvent event) {

    System.out.println(
        "[BIDDER NOTIFICATION] "
            + bidderName
            + " nhận event: "
            + event.getMessage());
  }

  @Override
  public String toString() {

    return "BidderObserver{"
        + "bidderName='"
        + bidderName
        + '\''
        + '}';
  }
}