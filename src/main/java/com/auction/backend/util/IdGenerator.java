package com.auction.backend.util;

import java.util.UUID;

/**
 * Utility sinh ID cho hệ thống.
 */
public final class IdGenerator {

  /**
   * Private constructor.
   */
  private IdGenerator() {
  }

  /**
   * Sinh auction ID.
   *
   * @return auction ID
   */
  public static String generateAuctionId() {

    return "AUC-"
        + UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .toUpperCase();
  }

  /**
   * Sinh transaction ID.
   *
   * @return transaction ID
   */
  public static String generateTransactionId() {

    return "TRANS-"
        + UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .toUpperCase();
  }

  /**
   * Sinh auto-bid ID.
   *
   * @return auto-bid ID
   */
  public static String generateAutoBidId() {

    return "ABID-"
        + UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .toUpperCase();
  }

  /**
   * Sinh user ID.
   *
   * @return user ID
   */
  public static String generateUserId() {
    return "USER-" 
        + UUID.randomUUID()
          .toString()
          .substring(0, 8)
          .toUpperCase();
  }

  /**
  * Sinh mã item.
  *
  * @return item id
  */
  public static String generateItemId() {
    return "ITEM-" 
        + UUID.randomUUID()
          .toString()
          .substring(0, 8)
          .toUpperCase();
  }
}