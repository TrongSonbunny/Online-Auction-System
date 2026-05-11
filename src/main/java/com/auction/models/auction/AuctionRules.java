package com.auction.models.auction;

/**
 * Chứa các business rule liên quan đến auction.
 */
public final class AuctionRules {

  /**
   * Bid increment tối thiểu.
   */
  public static final double
      MINIMUM_BID_INCREMENT = 1.0;

  /**
   * Thời lượng auction tối đa.
   */
  public static final long
      MAX_AUCTION_DURATION_HOURS = 72;

  /**
   * Private constructor.
   */
  private AuctionRules() {
  }
}