package com.auction.models.payment;

/**VnPayPayments
 * Thanh toán qua VnPay.
 */
public class VnPayPayments implements PaymentStrategy {
  public void pay(double amount) {
    System.out.println("Pay by VNPay: " + amount);
  }
}
