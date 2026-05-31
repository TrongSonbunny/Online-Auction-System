package com.auction.backend.bid;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.BidException;
import com.auction.models.auction.Auction;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.payment.BankPayment;
import com.auction.models.user.Bidder;
import com.auction.models.user.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho BidValidator.
 *
 * <p>EP: auction null/pending/active/finished; bidder null/valid;
 *      amount 0/âm/bằng giá hiện tại/cao hơn.
 * BVA: amount = currentHighestBid (invalid boundary), currentHighestBid+0.01 (valid boundary)
 */
@DisplayName("BidValidator Tests")
class BidValidatorTest {

  private BidValidator validator;
  private Auction activeAuction;
  private Auction pendingAuction;
  private Auction finishedAuction;
  private Bidder bidder;

  @BeforeEach
  void setUp() {
    validator = new BidValidator();
    bidder = new Bidder("B-001", "Bidder", "b@test.com",
        new BankPayment("VCB", "123456", "A"));

    Seller seller = new Seller("S-001", "Seller", "s@test.com");
    AuctionItem item = new AuctionItem("I-001", "Item", "Desc", ItemCategory.ART, "New", 100.0);

    pendingAuction = new Auction("AUC-P", seller, item, 1000.0);

    activeAuction = new Auction("AUC-A", seller, item, 1000.0);
    activeAuction.start();

    finishedAuction = new Auction("AUC-F", seller, item, 1000.0);
    finishedAuction.start();
    finishedAuction.finish();
  }

  // ──────── validateBid ────────

  @Nested
  @DisplayName("validateBid - Auction Validation (EP + BVA)")
  class AuctionValidation {

    @Test
    @DisplayName("EP-Valid: bid hợp lệ không ném exception")
    void validateBid_validBid_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validateBid(activeAuction, bidder, 1500.0));
    }

    @Test
    @DisplayName("EP-Invalid: auction null ném AuctionException")
    void validateBid_nullAuction_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> validator.validateBid(null, bidder, 1500.0));
    }

    @Test
    @DisplayName("BVA-Boundary: auction PENDING ném AuctionClosedException")
    void validateBid_pendingAuction_throwsAuctionClosedException() {
      assertThrows(AuctionClosedException.class,
          () -> validator.validateBid(pendingAuction, bidder, 1500.0));
    }

    @Test
    @DisplayName("EP-Invalid: auction FINISHED ném AuctionClosedException")
    void validateBid_finishedAuction_throwsAuctionClosedException() {
      assertThrows(AuctionClosedException.class,
          () -> validator.validateBid(finishedAuction, bidder, 1500.0));
    }
  }

  @Nested
  @DisplayName("validateBid - Bidder Validation (EP)")
  class BidderValidation {

    @Test
    @DisplayName("EP-Invalid: bidder null ném BidException")
    void validateBid_nullBidder_throwsBidException() {
      assertThrows(BidException.class,
          () -> validator.validateBid(activeAuction, null, 1500.0));
    }
  }

  @Nested
  @DisplayName("validateBid - Amount Validation (EP + BVA)")
  class AmountValidation {

    @Test
    @DisplayName("BVA-Boundary: amount = 0 ném BidException")
    void validateBid_zeroAmount_throwsBidException() {
      assertThrows(BidException.class,
          () -> validator.validateBid(activeAuction, bidder, 0.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount âm ném BidException")
    void validateBid_negativeAmount_throwsBidException() {
      assertThrows(BidException.class,
          () -> validator.validateBid(activeAuction, bidder, -1.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = currentHighestBid (1000) ném BidException")
    void validateBid_equalToCurrentHighest_throwsBidException() {
      assertThrows(BidException.class,
          () -> validator.validateBid(activeAuction, bidder, 1000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = currentHighestBid + 0.01 hợp lệ")
    void validateBid_justAboveCurrentHighest_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validateBid(activeAuction, bidder, 1000.01));
    }

    @Test
    @DisplayName("EP-Valid: amount cao hơn nhiều hợp lệ")
    void validateBid_largeAmount_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validateBid(activeAuction, bidder, 999_999.0));
    }
  }

  @Nested
  @DisplayName("validateBid - Permission (EP)")
  class PermissionValidation {

    @Test
    @DisplayName("EP-Valid: bidder có quyền canPlaceBid không ném exception")
    void validateBid_bidderWithPermission_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validateBid(activeAuction, bidder, 1500.0));
    }
  }
}
