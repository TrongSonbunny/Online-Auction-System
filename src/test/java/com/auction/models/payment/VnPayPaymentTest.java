package com.auction.models.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.PaymentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho VnPayPayment.
 *
 * <p>EP: email hợp lệ / null / blank / không có @; amount hợp lệ / 0 / âm
 * BVA: email = "@" (valid boundary), rỗng (invalid), null (invalid)
 *      amount = 0 (invalid boundary), 0.01 (valid boundary)
 */
@DisplayName("VnPayPayment Tests")
class VnPayPaymentTest {

  private VnPayPayment validPayment() {
    return new VnPayPayment("user@vnpay.com");
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor - Email (EP + BVA)")
  class EmailValidation {

    @Test
    @DisplayName("EP-Valid: email hợp lệ lưu đúng")
    void constructor_validEmail_stores() {
      assertEquals("user@vnpay.com", validPayment().getAccountEmail());
    }

    @Test
    @DisplayName("EP-Valid: getPaymentMethodName trả 'VNPay Payment'")
    void getPaymentMethodName_returnsVnPay() {
      assertEquals("VNPay Payment", validPayment().getPaymentMethodName());
    }

    @Test
    @DisplayName("EP-Invalid: email null ném PaymentException")
    void constructor_nullEmail_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> new VnPayPayment(null));
    }

    @Test
    @DisplayName("BVA-Boundary: email rỗng ném PaymentException")
    void constructor_emptyEmail_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> new VnPayPayment(""));
    }

    @Test
    @DisplayName("BVA-Boundary: email blank ném PaymentException")
    void constructor_blankEmail_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> new VnPayPayment("   "));
    }

    @Test
    @DisplayName("EP-Invalid: email không có @ ném PaymentException")
    void constructor_emailWithoutAt_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> new VnPayPayment("emailwithnoat.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: email chỉ có '@' hợp lệ")
    void constructor_atOnlyEmail_isValid() {
      VnPayPayment payment = new VnPayPayment("@");
      assertEquals("@", payment.getAccountEmail());
    }
  }

  // ──────── pay() ────────

  @Nested
  @DisplayName("pay() (EP + BVA)")
  class PayMethod {

    @Test
    @DisplayName("EP-Valid: pay dương trả true")
    void pay_positiveAmount_returnsTrue() {
      assertTrue(validPayment().pay(1_000_000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: pay(0.01) trả true")
    void pay_minPositive_returnsTrue() {
      assertTrue(validPayment().pay(0.01));
    }

    @Test
    @DisplayName("BVA-Boundary: pay(0) ném PaymentException")
    void pay_zero_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().pay(0.0));
    }

    @Test
    @DisplayName("BVA-Boundary: pay(-0.01) ném PaymentException")
    void pay_slightlyNegative_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().pay(-0.01));
    }

    @Test
    @DisplayName("EP-Invalid: pay âm lớn ném PaymentException")
    void pay_largeNegative_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().pay(-999.0));
    }
  }

  // ──────── refund() ────────

  @Nested
  @DisplayName("refund() (EP + BVA)")
  class RefundMethod {

    @Test
    @DisplayName("EP-Valid: refund dương trả true")
    void refund_positiveAmount_returnsTrue() {
      assertTrue(validPayment().refund(500_000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: refund(0.01) trả true")
    void refund_minPositive_returnsTrue() {
      assertTrue(validPayment().refund(0.01));
    }

    @Test
    @DisplayName("BVA-Boundary: refund(0) ném PaymentException")
    void refund_zero_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().refund(0.0));
    }

    @Test
    @DisplayName("EP-Invalid: refund âm ném PaymentException")
    void refund_negative_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().refund(-100.0));
    }
  }
}
