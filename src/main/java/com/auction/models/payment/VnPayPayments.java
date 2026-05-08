package com.auction.models.payment;

/**
 * Thanh toán qua VnPay.
 */
public class VnPayPayments implements PaymentStrategy {

  @Override
  public void pay(double amount) {
    System.out.println("Pay by VNPay: " + amount);
  }
}