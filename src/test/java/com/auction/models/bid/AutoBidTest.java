package com.auction.models.bid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.exceptions.BidException;
import com.auction.models.payment.BankPayment;
import com.auction.models.user.Bidder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho AutoBid constructor và getters.
 *
 * <p>EP: autoBidId/auctionId null/blank; maxBid/increment âm hoặc bằng 0.
 * BVA: maxBid = 0 (invalid), 0.01 (valid); increment = 0 (invalid), 0.01 (valid).
 */
@DisplayName("AutoBid Tests")
class AutoBidTest {

  private Bidder bidder;

  @BeforeEach
  void setUp() {
    bidder = new Bidder("B-001", "Bidder", "b@test.com",
        new BankPayment("VCB", "123", "A"));
  }

  private AutoBid validAutoBid() {
    return new AutoBid("AB-001", bidder, "AUC-001", 5000.0, 100.0);
  }

  // ──────── autoBidId ────────

  @Nested
  @DisplayName("AutoBidId validation (EP + BVA)")
  class AutoBidIdValidation {

    @Test
    @DisplayName("EP-Invalid: autoBidId null → BidException")
    void constructor_nullAutoBidId_throws() {
      assertThrows(BidException.class,
          () -> new AutoBid(null, bidder, "AUC-001", 5000.0, 100.0));
    }

    @Test
    @DisplayName("BVA-Boundary: autoBidId blank → BidException")
    void constructor_blankAutoBidId_throws() {
      assertThrows(BidException.class,
          () -> new AutoBid("   ", bidder, "AUC-001", 5000.0, 100.0));
    }
  }

  // ──────── bidder ────────

  @Nested
  @DisplayName("Bidder validation (EP)")
  class BidderValidation {

    @Test
    @DisplayName("EP-Invalid: bidder null → NullPointerException")
    void constructor_nullBidder_throws() {
      assertThrows(NullPointerException.class,
          () -> new AutoBid("AB-001", null, "AUC-001", 5000.0, 100.0));
    }
  }

  // ──────── auctionId ────────

  @Nested
  @DisplayName("AuctionId validation (EP + BVA)")
  class AuctionIdValidation {

    @Test
    @DisplayName("EP-Invalid: auctionId null → BidException")
    void constructor_nullAuctionId_throws() {
      assertThrows(BidException.class,
          () -> new AutoBid("AB-001", bidder, null, 5000.0, 100.0));
    }

    @Test
    @DisplayName("BVA-Boundary: auctionId blank → BidException")
    void constructor_blankAuctionId_throws() {
      assertThrows(BidException.class,
          () -> new AutoBid("AB-001", bidder, "  ", 5000.0, 100.0));
    }
  }

  // ──────── maxBid ────────

  @Nested
  @DisplayName("MaxBid validation (EP + BVA)")
  class MaxBidValidation {

    @Test
    @DisplayName("BVA-Boundary: maxBid = 0 → BidException")
    void constructor_zeroMaxBid_throws() {
      assertThrows(BidException.class,
          () -> new AutoBid("AB-001", bidder, "AUC-001", 0.0, 100.0));
    }

    @Test
    @DisplayName("EP-Invalid: maxBid âm → BidException")
    void constructor_negativeMaxBid_throws() {
      assertThrows(BidException.class,
          () -> new AutoBid("AB-001", bidder, "AUC-001", -500.0, 100.0));
    }

    @Test
    @DisplayName("BVA-Boundary: maxBid = 0.01 → hợp lệ")
    void constructor_minPositiveMaxBid_valid() {
      final AutoBid ab = new AutoBid("AB-001", bidder, "AUC-001", 0.01, 0.01);
      assertEquals(0.01, ab.getMaxBid(), 0.001);
    }
  }

  // ──────── increment ────────

  @Nested
  @DisplayName("Increment validation (EP + BVA)")
  class IncrementValidation {

    @Test
    @DisplayName("BVA-Boundary: increment = 0 → BidException")
    void constructor_zeroIncrement_throws() {
      assertThrows(BidException.class,
          () -> new AutoBid("AB-001", bidder, "AUC-001", 5000.0, 0.0));
    }

    @Test
    @DisplayName("EP-Invalid: increment âm → BidException")
    void constructor_negativeIncrement_throws() {
      assertThrows(BidException.class,
          () -> new AutoBid("AB-001", bidder, "AUC-001", 5000.0, -50.0));
    }

    @Test
    @DisplayName("BVA-Boundary: increment = 0.01 → hợp lệ")
    void constructor_minPositiveIncrement_valid() {
      final AutoBid ab = new AutoBid("AB-001", bidder, "AUC-001", 5000.0, 0.01);
      assertEquals(0.01, ab.getIncrement(), 0.001);
    }
  }

  // ──────── Getters và toString ────────

  @Nested
  @DisplayName("Getters và toString (EP-Valid)")
  class GettersAndToString {

    @Test
    @DisplayName("getAutoBidId trả về đúng")
    void getAutoBidId_returnsCorrect() {
      assertEquals("AB-001", validAutoBid().getAutoBidId());
    }

    @Test
    @DisplayName("getBidder trả về đúng")
    void getBidder_returnsCorrect() {
      assertEquals(bidder, validAutoBid().getBidder());
    }

    @Test
    @DisplayName("getAuctionId trả về đúng")
    void getAuctionId_returnsCorrect() {
      assertEquals("AUC-001", validAutoBid().getAuctionId());
    }

    @Test
    @DisplayName("getMaxBid trả về đúng")
    void getMaxBid_returnsCorrect() {
      assertEquals(5000.0, validAutoBid().getMaxBid());
    }

    @Test
    @DisplayName("getIncrement trả về đúng")
    void getIncrement_returnsCorrect() {
      assertEquals(100.0, validAutoBid().getIncrement());
    }

    @Test
    @DisplayName("getRegisteredAt không null")
    void getRegisteredAt_notNull() {
      assertNotNull(validAutoBid().getRegisteredAt());
    }

    @Test
    @DisplayName("toString không null")
    void toString_notNull() {
      assertNotNull(validAutoBid().toString());
    }
  }
}
