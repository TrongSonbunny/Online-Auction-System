package com.auction.models.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.BidException;
import com.auction.models.payment.BankPayment;
import com.auction.models.payment.MomoPayment;
import com.auction.models.payment.PaymentStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho Bidder.
 *
 * <p>EP: dữ liệu hợp lệ / null / blank; paymentStrategy null
 * BVA: totalBidsPlaced từ 0 → 1; email có @ / không
 */
@DisplayName("Bidder Tests")
class BidderTest {

  private PaymentStrategy defaultPayment() {
    return new BankPayment("Vietcombank", "123456789", "Nguyễn A");
  }

  private Bidder validBidder() {
    return new Bidder("BIDDER-01", "Lê Bidder", "bidder@example.com", defaultPayment());
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor Validation (EP + BVA)")
  class ConstructorValidation {

    @Test
    @DisplayName("EP-Valid: tạo bidder với dữ liệu hợp lệ")
    void constructor_validData_createsBidder() {
      Bidder bidder = validBidder();
      assertNotNull(bidder);
      assertEquals("BIDDER-01", bidder.getUserId());
      assertEquals("Lê Bidder", bidder.getName());
      assertEquals("bidder@example.com", bidder.getEmail());
      assertEquals(UserRole.BIDDER, bidder.getRole());
    }

    @Test
    @DisplayName("BVA-Boundary: totalBidsPlaced ban đầu là 0")
    void constructor_initialBidsPlaced_isZero() {
      assertEquals(0, validBidder().getTotalBidsPlaced());
    }

    @Test
    @DisplayName("EP-Invalid: paymentStrategy null ném NullPointerException")
    void constructor_nullPaymentStrategy_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> new Bidder("B-01", "Bidder", "b@t.com", null));
    }

    @Test
    @DisplayName("EP-Invalid: userId null ném BidException")
    void constructor_nullUserId_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Bidder(null, "Bidder", "b@t.com", defaultPayment()));
    }

    @Test
    @DisplayName("BVA-Boundary: userId blank ném BidException")
    void constructor_blankUserId_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Bidder("  ", "Bidder", "b@t.com", defaultPayment()));
    }

    @Test
    @DisplayName("EP-Invalid: name null ném BidException")
    void constructor_nullName_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Bidder("B-01", null, "b@t.com", defaultPayment()));
    }

    @Test
    @DisplayName("BVA-Boundary: name 1 ký tự hợp lệ")
    void constructor_singleCharName_isValid() {
      Bidder bidder = new Bidder("B-01", "B", "b@t.com", defaultPayment());
      assertEquals("B", bidder.getName());
    }

    @Test
    @DisplayName("EP-Invalid: email không có @ ném BidException")
    void constructor_emailWithoutAt_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Bidder("B-01", "Bidder", "invalidemail", defaultPayment()));
    }

    @Test
    @DisplayName("BVA-Boundary: email rỗng ném BidException")
    void constructor_emptyEmail_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Bidder("B-01", "Bidder", "", defaultPayment()));
    }
  }

  // ──────── Permissions ────────

  @Nested
  @DisplayName("Bidder Permissions")
  class BidderPermissions {

    @Test
    @DisplayName("Bidder không có quyền canCreateAuction")
    void bidder_cannotCreateAuction() {
      assertFalse(validBidder().canCreateAuction());
    }

    @Test
    @DisplayName("Bidder có quyền canPlaceBid")
    void bidder_canPlaceBid() {
      assertTrue(validBidder().canPlaceBid());
    }

    @Test
    @DisplayName("Bidder không có quyền canDeleteAuction")
    void bidder_cannotDeleteAuction() {
      assertFalse(validBidder().canDeleteAuction());
    }

    @Test
    @DisplayName("Bidder không có quyền canBanUser")
    void bidder_cannotBanUser() {
      assertFalse(validBidder().canBanUser());
    }

    @Test
    @DisplayName("Bidder không có quyền canManageSystem")
    void bidder_cannotManageSystem() {
      assertFalse(validBidder().canManageSystem());
    }
  }

  // ──────── Bid Counter ────────

  @Nested
  @DisplayName("Bid Counter (BVA)")
  class BidCounter {

    @Test
    @DisplayName("BVA-Boundary: increment từ 0 -> 1")
    void incrementTotalBidsPlaced_fromZeroToOne() {
      Bidder bidder = validBidder();
      bidder.incrementTotalBidsPlaced();
      assertEquals(1, bidder.getTotalBidsPlaced());
    }

    @Test
    @DisplayName("EP-Valid: increment 10 lần đúng")
    void incrementTotalBidsPlaced_multipleTimes_accumulates() {
      Bidder bidder = validBidder();
      for (int i = 0; i < 10; i++) {
        bidder.incrementTotalBidsPlaced();
      }
      assertEquals(10, bidder.getTotalBidsPlaced());
    }
  }

  // ──────── Payment Strategy ────────

  @Nested
  @DisplayName("Payment Strategy (EP + BVA)")
  class PaymentStrategyTests {

    @Test
    @DisplayName("EP-Valid: getPaymentStrategy trả về strategy ban đầu")
    void getPaymentStrategy_returnsInitialStrategy() {
      PaymentStrategy payment = defaultPayment();
      Bidder bidder = new Bidder("B-01", "Bidder", "b@t.com", payment);
      assertEquals(payment, bidder.getPaymentStrategy());
    }

    @Test
    @DisplayName("EP-Valid: setPaymentStrategy cập nhật strategy mới")
    void setPaymentStrategy_newStrategy_updates() {
      Bidder bidder = validBidder();
      PaymentStrategy momo = new MomoPayment("0123456789", "Momo Owner");
      bidder.setPaymentStrategy(momo);
      assertEquals(momo, bidder.getPaymentStrategy());
    }

    @Test
    @DisplayName("EP-Invalid: setPaymentStrategy null ném NullPointerException")
    void setPaymentStrategy_null_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> validBidder().setPaymentStrategy(null));
    }
  }
}
