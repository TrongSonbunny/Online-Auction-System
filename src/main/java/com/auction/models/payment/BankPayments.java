package com.auction.models.payment;

/**
 * Thanh toán qua ngân hàng.
 */
public class BankPayments implements PaymentStrategy {
  public void pay(double amount) {
    System.out.println("Pay by Bank: " + amount);
  }
}
