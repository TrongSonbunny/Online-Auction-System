package com.auction.backend.auction;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;

/**
 * Validate business logic cho auction.
 */
public class AuctionValidator {

  /**
   * Validate auction trước khi tạo.
   *
   * @param auction auction cần validate
   */
  public void validateAuctionCreation(
      Auction auction) {

    if (auction == null) {

      throw new AuctionException(
          "Auction không được null.");
    }
  }

  /**
   * Validate auction active.
   *
   * @param auction auction cần validate
   */
  public void validateAuctionActive(
      Auction auction) {

    validateAuctionCreation(auction);

    if (!auction.isActive()) {

      throw new AuctionClosedException(
          "Auction không active.");
    }
  }
}