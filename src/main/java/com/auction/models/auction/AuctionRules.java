package com.auction.models.auction;

/**
 * Chứa các hằng số business rule của hệ thống đấu giá.
 *
 * <p>Các hằng số anti-snipe: khi bid đặt trong {@link #ANTI_SNIPE_WINDOW_SECONDS}
 * giây cuối, auction tự động gia hạn thêm {@link #ANTI_SNIPE_EXTENSION_SECONDS} giây.
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
   * Khoảng thời gian cuối (giây) kích hoạt anti-snipe.
   */
  public static final long
      ANTI_SNIPE_WINDOW_SECONDS = 30;

  /**
   * Thời gian gia hạn auction khi anti-snipe kích hoạt (giây).
   */
  public static final long
      ANTI_SNIPE_EXTENSION_SECONDS = 60;

  /**
   * Private constructor.
   */
  private AuctionRules() {
  }
}