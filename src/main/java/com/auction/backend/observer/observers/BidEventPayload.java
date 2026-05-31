package com.auction.backend.observer.observers;

import com.auction.models.auction.Auction;
import com.auction.models.bid.BidTransaction;
import java.util.Objects;

/**
 * Payload cho event liên quan đến bid.
 *
 * <p>Cần chứa cả transaction và auction để observer có thể lưu bid transaction
 * rồi cập nhật current highest bid của auction xuống SQLite.
 */
public class BidEventPayload {

  private final Auction auction;

  private final BidTransaction transaction;

  /**
   * Constructor bid event payload.
   *
   * @param auction auction vừa nhận bid
   * @param transaction transaction vừa được tạo
   */
  public BidEventPayload(
      Auction auction,
      BidTransaction transaction) {

    this.auction =
        Objects.requireNonNull(
            auction,
            "Auction không được null.");

    this.transaction =
        Objects.requireNonNull(
            transaction,
            "Transaction không được null.");
  }

  /**
   * Lấy auction.
   *
   * @return auction
   */
  public Auction getAuction() {
    return auction;
  }

  /**
   * Lấy transaction.
   *
   * @return bid transaction
   */
  public BidTransaction getTransaction() {
    return transaction;
  }
}