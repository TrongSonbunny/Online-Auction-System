package com.auction.models.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.PaymentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho BankPayment.
 *
 * <p>EP: tham số hợp lệ / null / blank; amount hợp lệ / 0 / âm
 * BVA: amount = 0 (invalid), 0.01 (valid boundary), âm (invalid)
 */
@DisplayName("BankPayment Tests")
class BankPaymentTest {

  private BankPayment validPayment() {
    return new BankPayment("Vietcombank", "1234567890", "Nguyễn A");
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor (EP + BVA)")
  class ConstructorValidation {

    @Test
    @DisplayName("EP-Valid: tạo với dữ liệu hợp lệ")
    void constructor_validData_stores() {
      BankPayment payment = validPayment();
      assertEquals("Vietcombank", payment.getBankName());
      assertEquals("1234567890", payment.getBankAccountNumber());
      assertEquals("Nguyễn A", payment.getAccountHolderName());
    }

    @Test
    @DisplayName("EP-Valid: tên phương thức là 'Bank Payment'")
    void getPaymentMethodName_returnsCorrectName() {
      assertEquals("Bank Payment", validPayment().getPaymentMethodName());
    }

    @Test
    @DisplayName("EP-Invalid: bankName null ném PaymentException")
    void constructor_nullBankName_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new BankPayment(null, "123456", "Holder"));
    }

    @Test
    @DisplayName("BVA-Boundary: bankName rỗng ném PaymentException")
    void constructor_emptyBankName_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new BankPayment("", "123456", "Holder"));
    }

    @Test
    @DisplayName("BVA-Boundary: bankName blank ném PaymentException")
    void constructor_blankBankName_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new BankPayment("  ", "123456", "Holder"));
    }

    @Test
    @DisplayName("EP-Invalid: accountNumber null ném PaymentException")
    void constructor_nullAccountNumber_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new BankPayment("VCB", null, "Holder"));
    }

    @Test
    @DisplayName("BVA-Boundary: accountNumber rỗng ném PaymentException")
    void constructor_emptyAccountNumber_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new BankPayment("VCB", "", "Holder"));
    }

    @Test
    @DisplayName("EP-Invalid: holderName null ném PaymentException")
    void constructor_nullHolderName_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new BankPayment("VCB", "123456", null));
    }

    @Test
    @DisplayName("BVA-Boundary: holderName blank ném PaymentException")
    void constructor_blankHolderName_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> new BankPayment("VCB", "123456", "  "));
    }
  }

  // ──────── pay() ────────

  @Nested
  @DisplayName("pay() (EP + BVA)")
  class PayMethod {

    @Test
    @DisplayName("EP-Valid: thanh toán với amount dương trả true")
    void pay_positiveAmount_returnsTrue() {
      assertTrue(validPayment().pay(500_000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: pay(0.01) trả true")
    void pay_minPositiveAmount_returnsTrue() {
      assertTrue(validPayment().pay(0.01));
    }

    @Test
    @DisplayName("BVA-Boundary: pay(0) ném PaymentException")
    void pay_zeroAmount_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().pay(0.0));
    }

    @Test
    @DisplayName("BVA-Boundary: pay(-0.01) ném PaymentException")
    void pay_slightlyNegativeAmount_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().pay(-0.01));
    }

    @Test
    @DisplayName("EP-Invalid: pay(-1000) ném PaymentException")
    void pay_largeNegativeAmount_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().pay(-1000.0));
    }
  }

  // ──────── refund() ────────

  @Nested
  @DisplayName("refund() (EP + BVA)")
  class RefundMethod {

    @Test
    @DisplayName("EP-Valid: hoàn tiền amount dương trả true")
    void refund_positiveAmount_returnsTrue() {
      assertTrue(validPayment().refund(200_000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: refund(0.01) trả true")
    void refund_minPositiveAmount_returnsTrue() {
      assertTrue(validPayment().refund(0.01));
    }

    @Test
    @DisplayName("BVA-Boundary: refund(0) ném PaymentException")
    void refund_zeroAmount_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().refund(0.0));
    }

    @Test
    @DisplayName("EP-Invalid: refund âm ném PaymentException")
    void refund_negativeAmount_throwsPaymentException() {
      assertThrows(PaymentException.class, () -> validPayment().refund(-500.0));
    }
  }
}
