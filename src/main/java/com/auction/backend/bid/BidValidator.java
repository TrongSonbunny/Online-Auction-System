package com.auction.backend.bid;

import com.auction.models.auction.Auction;
import com.auction.models.user.Bidder;

/**
 * Validate logic cho bid.
 */
public class BidValidator {

  /**
   * Validate toàn bộ bid request.
   *
   * @param auction auction cần bid
   * @param bidder bidder thực hiện
   * @param amount số tiền bid
   */
  public void validateBid(
      Auction auction,
      Bidder bidder,
      double amount) {

    validateAuction(auction);
    validateBidder(bidder);
    validateBidAmount(amount);
    validateAuctionActive(auction);
    validateBidHigherThanCurrent(auction, amount);
    validateBidPermission(bidder);
  }

  /**
   * Validate auction object.
   *
   * @param auction auction
   */
  private void validateAuction(
      Auction auction) {

    if (auction == null) {
      throw new IllegalArgumentException(
          "Auction không được null.");
    }
  }

  /**
   * Validate bidder object.
   *
   * @param bidder bidder
   */
  private void validateBidder(
      Bidder bidder) {

    if (bidder == null) {
      throw new IllegalArgumentException(
          "Bidder không được null.");
    }
  }

  /**
   * Validate số tiền bid.
   *
   * @param amount số tiền bid
   */
  private void validateBidAmount(
      double amount) {

    if (amount <= 0) {
      throw new IllegalArgumentException(
          "Bid amount phải lớn hơn 0.");
    }
  }

  /**
   * Kiểm tra auction còn active hay không.
   *
   * @param auction auction cần kiểm tra
   */
  private void validateAuctionActive(
      Auction auction) {

    if (!auction.isActive()) {
      throw new IllegalStateException(
          "Auction không còn hoạt động.");
    }
  }

  /**
   * Kiểm tra bid phải cao hơn giá hiện tại.
   *
   * @param auction auction
   * @param amount số tiền bid
   */
  private void validateBidHigherThanCurrent(
      Auction auction,
      double amount) {

    if (amount <= auction.getCurrentHighestBid()) {

      throw new IllegalArgumentException(
          "Bid phải lớn hơn giá hiện tại.");
    }
  }

  /**
   * Kiểm tra quyền bid.
   *
   * @param bidder bidder
   */
  private void validateBidPermission(
      Bidder bidder) {

    if (!bidder.canPlaceBid()) {

      throw new IllegalStateException(
          "User không có quyền đặt giá.");
    }
  }
}