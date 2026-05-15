package com.auction.backend.payment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.exceptions.PaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho PaymentValidator.
 *
 * <p>EP: amount hợp lệ / 0 / âm; balance >= amount / balance < amount
 * BVA: amount = 0 (invalid), 0.01 (valid)
 * balance = amount (valid), balance = amount-0.01 (invalid)
 */
@DisplayName("PaymentValidator Tests")
class PaymentValidatorTest {

  private PaymentValidator validator;

  @BeforeEach
  void setUp() {
    validator = new PaymentValidator();
  }

  // ──────── validatePaymentAmount ────────

  @Nested
  @DisplayName("validatePaymentAmount (EP + BVA)")
  class ValidatePaymentAmount {

    @Test
    @DisplayName("EP-Valid: amount dương không ném exception")
    void validate_positiveAmount_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validatePaymentAmount(100.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = 0.01 không ném exception")
    void validate_minPositiveAmount_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validatePaymentAmount(0.01));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = 0 ném PaymentException")
    void validate_zeroAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> validator.validatePaymentAmount(0.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = -0.01 ném PaymentException")
    void validate_slightlyNegative_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> validator.validatePaymentAmount(-0.01));
    }

    @Test
    @DisplayName("EP-Invalid: amount âm lớn ném PaymentException")
    void validate_largeNegativeAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> validator.validatePaymentAmount(-1_000_000.0));
    }

    @Test
    @DisplayName("EP-Valid: amount rất lớn không ném exception")
    void validate_largePositiveAmount_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validatePaymentAmount(1_000_000_000.0));
    }
  }

  // ──────── validateBalance ────────

  @Nested
  @DisplayName("validateBalance (EP + BVA)")
  class ValidateBalance {

    @Test
    @DisplayName("EP-Valid: balance > amount không ném exception")
    void validate_balanceGreaterThanAmount_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validateBalance(1000.0, 500.0));
    }

    @Test
    @DisplayName("BVA-Boundary: balance = amount không ném exception")
    void validate_balanceEqualsAmount_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validateBalance(500.0, 500.0));
    }

    @Test
    @DisplayName("BVA-Boundary: balance = amount - 0.01 ném PaymentException")
    void validate_balanceJustBelowAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> validator.validateBalance(499.99, 500.0));
    }

    @Test
    @DisplayName("EP-Invalid: balance nhỏ hơn amount ném PaymentException")
    void validate_insufficientBalance_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> validator.validateBalance(100.0, 500.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = 0 ném PaymentException (từ validatePaymentAmount)")
    void validate_zeroAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> validator.validateBalance(1000.0, 0.0));
    }

    @Test
    @DisplayName("EP-Invalid: balance = 0, amount = 0.01 ném PaymentException")
    void validate_zeroBalance_positiveAmount_throwsPaymentException() {
      assertThrows(PaymentException.class,
          () -> validator.validateBalance(0.0, 0.01));
    }
  }
}
