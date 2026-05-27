package com.auction.models.payment;

/**
 * Factory tạo payment mặc định cho bidder.
 */
public final class DefaultPaymentFactory {

  /**
   * Private constructor để tránh tạo object.
   */
  private DefaultPaymentFactory() {
  }

  /**
   * Tạo payment mặc định cho bidder.
   *
   * @return payment strategy mặc định
   */
  public static PaymentStrategy createDefaultPayment() {

    return new MomoPayment(
        "0000000000",
        "Default Bidder");
  }
}