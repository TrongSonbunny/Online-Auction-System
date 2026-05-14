package com.auction.models.bid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.exceptions.AuctionException;
import com.auction.exceptions.BidException;
import com.auction.models.payment.BankPayment;
import com.auction.models.user.Bidder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho BidTransaction.
 *
 * <p>EP: bidAmount hợp lệ / 0 / âm; auctionId null / blank / valid; bidder null
 * BVA: bidAmount = 0 (invalid), 0.01 (valid boundary), âm (invalid)
 */
@DisplayName("BidTransaction Tests")
class BidTransactionTest {

  private Bidder bidder;

  @BeforeEach
  void setUp() {
    bidder = new Bidder("B-001", "Trần Bidder", "b@test.com",
        new BankPayment("VCB", "123456", "Trần A"));
  }

  private BidTransaction validTransaction() {
    return new BidTransaction("TRANS-001", bidder, "AUC-001", 1500.0);
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor - BidAmount (EP + BVA)")
  class BidAmountValidation {

    @Test
    @DisplayName("EP-Valid: bidAmount dương hợp lệ")
    void constructor_positiveBidAmount_stores() {
      BidTransaction tx = validTransaction();
      assertEquals(1500.0, tx.getBidAmount());
    }

    @Test
    @DisplayName("BVA-Boundary: bidAmount = 0.01 hợp lệ")
    void constructor_minPositiveBidAmount_isValid() {
      BidTransaction tx = new BidTransaction("T-001", bidder, "AUC-001", 0.01);
      assertEquals(0.01, tx.getBidAmount(), 0.001);
    }

    @Test
    @DisplayName("BVA-Boundary: bidAmount = 0 ném BidException")
    void constructor_zeroBidAmount_throwsBidException() {
      assertThrows(BidException.class,
          () -> new BidTransaction("T-001", bidder, "AUC-001", 0.0));
    }

    @Test
    @DisplayName("BVA-Boundary: bidAmount = -0.01 ném BidException")
    void constructor_slightlyNegativeBidAmount_throwsBidException() {
      assertThrows(BidException.class,
          () -> new BidTransaction("T-001", bidder, "AUC-001", -0.01));
    }

    @Test
    @DisplayName("EP-Invalid: bidAmount âm lớn ném BidException")
    void constructor_largeNegativeBidAmount_throwsBidException() {
      assertThrows(BidException.class,
          () -> new BidTransaction("T-001", bidder, "AUC-001", -1000.0));
    }

    @Test
    @DisplayName("EP-Valid: bidAmount rất lớn hợp lệ")
    void constructor_largeBidAmount_isValid() {
      BidTransaction tx = new BidTransaction("T-001", bidder, "AUC-001", 1_000_000_000.0);
      assertEquals(1_000_000_000.0, tx.getBidAmount());
    }
  }

  @Nested
  @DisplayName("Constructor - AuctionId (EP + BVA)")
  class AuctionIdValidation {

    @Test
    @DisplayName("EP-Valid: auctionId hợp lệ")
    void constructor_validAuctionId_stores() {
      assertEquals("AUC-001", validTransaction().getAuctionId());
    }

    @Test
    @DisplayName("EP-Invalid: auctionId null ném AuctionException")
    void constructor_nullAuctionId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new BidTransaction("T-001", bidder, null, 100.0));
    }

    @Test
    @DisplayName("BVA-Boundary: auctionId rỗng ném AuctionException")
    void constructor_emptyAuctionId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new BidTransaction("T-001", bidder, "", 100.0));
    }

    @Test
    @DisplayName("BVA-Boundary: auctionId blank ném AuctionException")
    void constructor_blankAuctionId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new BidTransaction("T-001", bidder, "   ", 100.0));
    }
  }

  @Nested
  @DisplayName("Constructor - Bidder và TransactionId (EP)")
  class BidderAndTransactionIdValidation {

    @Test
    @DisplayName("EP-Invalid: bidder null ném NullPointerException")
    void constructor_nullBidder_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> new BidTransaction("T-001", null, "AUC-001", 100.0));
    }

    @Test
    @DisplayName("EP-Valid: bidder đúng lưu đúng")
    void constructor_validBidder_stores() {
      assertEquals(bidder, validTransaction().getBidder());
    }

    @Test
    @DisplayName("EP-Valid: transactionId lưu đúng")
    void constructor_storesTransactionId() {
      BidTransaction tx = validTransaction();
      assertEquals("TRANS-001", tx.getTransactionId());
    }

    @Test
    @DisplayName("EP-Valid: createdAt không null")
    void constructor_createdAt_isNotNull() {
      assertNotNull(validTransaction().getCreatedAt());
    }

    @Test
    @DisplayName("toString không null")
    void toString_isNotNull() {
      assertNotNull(validTransaction().toString());
    }
  }
}
