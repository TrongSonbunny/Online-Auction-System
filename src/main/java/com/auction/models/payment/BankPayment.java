package com.auction.models.payment;

/**
 * Thanh toán qua ngân hàng.
 */
public class BankPayment implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Pay by Bank: " + amount);
    }
}