package com.auction.models.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.PaymentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho MomoPayment.
 *
 * <p>EP: phoneNumber hợp lệ / null / blank / ngắn hơn 10 ký tự
 * BVA: phoneNumber = 9 ký tự (invalid boundary), 10 ký tự (valid boundary), 11 ký tự (valid)
 *      amount = 0 (invalid), 0.01 (valid)
 */
@DisplayName("MomoPayment Tests")
class MomoPaymentTest {

  private MomoPayment validPayment() {
    return new MomoPayment("0123456789", "Nguyễn Momo");
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor - PhoneNumber (EP + BVA)")
  class PhoneNumberValidation {

    @Test
    @DisplayName("EP-Valid: phone 10 ký tự hợp lệ")
    void constructor_tenCharPhone_isValid() {
      MomoPayment payment = validPayment();
      assertEquals("0123456789", payment.getPhoneNumber());
    }

    @Test
    @DisplayName("EP-Valid: phone 11 ký tự hợp lệ")
    void constructor_elevenCharPhone_isValid() {
      MomoPayment payment = new MomoPayment("01234567890", "Owner");
      assertEquals("01234567890", payment.getPhoneNumber());
    }

    @Test
    @DisplayName("BVA-Boundary: phone 9 ký tự ném PaymentException")
    void constructor_nineCharPhone_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new MomoPayment("012345678", "Owner"));
    }

    @Test
    @DisplayName("BVA-Boundary: phone rỗng ném PaymentException")
    void constructor_emptyPhone_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new MomoPayment("", "Owner"));
    }

    @Test
    @DisplayName("EP-Invalid: phone null ném PaymentException")
    void constructor_nullPhone_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new MomoPayment(null, "Owner"));
    }

    @Test
    @DisplayName("BVA-Boundary: phone blank (< 10) ném PaymentException")
    void constructor_blankPhone_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new MomoPayment("   ", "Owner"));
    }
  }

  @Nested
  @DisplayName("Constructor - WalletOwnerName (EP + BVA)")
  class WalletOwnerNameValidation {

    @Test
    @DisplayName("EP-Valid: walletOwnerName hợp lệ")
    void constructor_validOwnerName_stores() {
      assertEquals("Nguyễn Momo", validPayment().getWalletOwnerName());
    }

    @Test
    @DisplayName("EP-Invalid: walletOwnerName null ném PaymentException")
    void constructor_nullOwnerName_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new MomoPayment("0123456789", null));
    }

    @Test
    @DisplayName("BVA-Boundary: walletOwnerName rỗng ném PaymentException")
    void constructor_emptyOwnerName_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new MomoPayment("0123456789", ""));
    }

    @Test
    @DisplayName("BVA-Boundary: walletOwnerName 1 ký tự hợp lệ")
    void constructor_singleCharOwnerName_isValid() {
      MomoPayment payment = new MomoPayment("0123456789", "A");
      assertEquals("A", payment.getWalletOwnerName());
    }

    @Test
    @DisplayName("EP-Valid: getPaymentMethodName trả 'Momo Payment'")
    void getPaymentMethodName_returnsMomoPayment() {
      assertEquals("Momo Payment", validPayment().getPaymentMethodName());
    }
  }

  // ──────── pay() ────────

  @Nested
  @DisplayName("pay() (EP + BVA)")
  class PayMethod {

    @Test
    @DisplayName("EP-Valid: pay dương trả true")
    void pay_positiveAmount_returnsTrue() {
      assertTrue(validPayment().pay(100_000.0));
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
    @DisplayName("EP-Invalid: pay âm ném PaymentException")
    void pay_negative_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().pay(-1.0));
    }
  }

  // ──────── refund() ────────

  @Nested
  @DisplayName("refund() (EP + BVA)")
  class RefundMethod {

    @Test
    @DisplayName("EP-Valid: refund dương trả true")
    void refund_positiveAmount_returnsTrue() {
      assertTrue(validPayment().refund(50_000.0));
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
