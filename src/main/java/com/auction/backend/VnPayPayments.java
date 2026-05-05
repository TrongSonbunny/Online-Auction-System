package com.auction.backend;

/**VnPayPayments
 * Thanh toán qua VnPay.
 */
public class VnPayPayments implements PaymentStrategy {
  @Override
  public void pay(double amount) {
    System.out.println("Pay by VNPay: " + amount);
  }
}