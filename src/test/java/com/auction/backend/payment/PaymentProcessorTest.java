package com.auction.backend.payment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.PaymentException;
import com.auction.models.payment.BankPayment;
import com.auction.models.payment.MomoPayment;
import com.auction.models.payment.PaymentStrategy;
import com.auction.models.payment.VnPayPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho PaymentProcessor - processPayment và processRefund.
 *
 * <p>EP: strategy null / hợp lệ (Bank/Momo/VnPay); amount 0 / âm / dương
 * BVA: amount = 0 (invalid boundary), 0.01 (valid boundary)
 */
@DisplayName("PaymentProcessor Tests")
class PaymentProcessorTest {

  private PaymentProcessor processor;
  private PaymentStrategy bankPayment;
  private PaymentStrategy momoPayment;
  private PaymentStrategy vnPayPayment;

  @BeforeEach
  void setUp() {
    processor = new PaymentProcessor();
    bankPayment = new BankPayment("VCB", "123456", "Nguyễn A");
    momoPayment = new MomoPayment("0123456789", "Momo Owner");
    vnPayPayment = new VnPayPayment("user@vnpay.com");
  }

  // ──────── processPayment ────────

  @Nested
  @DisplayName("processPayment (EP + BVA)")
  class ProcessPayment {

    @Test
    @DisplayName("EP-Valid: BankPayment amount dương trả true")
    void processPayment_bankPayment_positiveAmount_returnsTrue() {
      assertTrue(processor.processPayment(bankPayment, 500_000.0));
    }

    @Test
    @DisplayName("EP-Valid: MomoPayment amount dương trả true")
    void processPayment_momoPayment_positiveAmount_returnsTrue() {
      assertTrue(processor.processPayment(momoPayment, 100_000.0));
    }

    @Test
    @DisplayName("EP-Valid: VnPayPayment amount dương trả true")
    void processPayment_vnPayPayment_positiveAmount_returnsTrue() {
      assertTrue(processor.processPayment(vnPayPayment, 200_000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = 0.01 trả true")
    void processPayment_minPositiveAmount_returnsTrue() {
      assertTrue(processor.processPayment(bankPayment, 0.01));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = 0 ném PaymentException")
    void processPayment_zeroAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> processor.processPayment(bankPayment, 0.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = -0.01 ném PaymentException")
    void processPayment_slightlyNegativeAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> processor.processPayment(bankPayment, -0.01));
    }

    @Test
    @DisplayName("EP-Invalid: amount âm lớn ném PaymentException")
    void processPayment_largeNegativeAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> processor.processPayment(bankPayment, -999.0));
    }

    @Test
    @DisplayName("EP-Invalid: strategy null ném NullPointerException")
    void processPayment_nullStrategy_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> processor.processPayment(null, 100.0));
    }
  }

  // ──────── processRefund ────────

  @Nested
  @DisplayName("processRefund (EP + BVA)")
  class ProcessRefund {

    @Test
    @DisplayName("EP-Valid: BankPayment refund dương trả true")
    void processRefund_bankPayment_positiveAmount_returnsTrue() {
      assertTrue(processor.processRefund(bankPayment, 100_000.0));
    }

    @Test
    @DisplayName("EP-Valid: MomoPayment refund dương trả true")
    void processRefund_momoPayment_positiveAmount_returnsTrue() {
      assertTrue(processor.processRefund(momoPayment, 50_000.0));
    }

    @Test
    @DisplayName("EP-Valid: VnPayPayment refund dương trả true")
    void processRefund_vnPayPayment_positiveAmount_returnsTrue() {
      assertTrue(processor.processRefund(vnPayPayment, 75_000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = 0.01 trả true")
    void processRefund_minPositiveAmount_returnsTrue() {
      assertTrue(processor.processRefund(bankPayment, 0.01));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = 0 ném PaymentException")
    void processRefund_zeroAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> processor.processRefund(bankPayment, 0.0));
    }

    @Test
    @DisplayName("EP-Invalid: amount âm ném PaymentException")
    void processRefund_negativeAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> processor.processRefund(momoPayment, -500.0));
    }

    @Test
    @DisplayName("EP-Invalid: strategy null ném NullPointerException")
    void processRefund_nullStrategy_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> processor.processRefund(null, 100.0));
    }
  }
}
