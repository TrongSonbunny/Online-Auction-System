package com.auction.backend.auction;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;

/**
 * Kiểm tra business rule cho auction trước khi thực hiện hành động.
 *
 * <p>Cung cấp hai mức kiểm tra:
 * <ul>
 *   <li>{@link #validateAuctionCreation} — chỉ kiểm tra auction không null.
 *   <li>{@link #validateAuctionActive} — kiểm tra không null và đang ACTIVE.
 * </ul>
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