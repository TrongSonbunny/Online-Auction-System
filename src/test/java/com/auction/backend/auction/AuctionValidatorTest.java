package com.auction.backend.auction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.user.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho AuctionValidator.
 *
 * <p>EP: auction null / pending / active / finished
 * BVA: trạng thái ranh giới giữa ACTIVE và không ACTIVE
 */
@DisplayName("AuctionValidator Tests")
class AuctionValidatorTest {

  private AuctionValidator validator;
  private Auction pendingAuction;
  private Auction activeAuction;
  private Auction finishedAuction;
  private Auction cancelledAuction;

  @BeforeEach
  void setUp() {
    validator = new AuctionValidator();

    Seller seller = new Seller("S-001", "Seller", "s@test.com");
    AuctionItem item = new AuctionItem("I-001", "Item", "Desc", ItemCategory.ART, "New", 100.0);

    pendingAuction = new Auction("AUC-P", seller, item, 500.0);

    activeAuction = new Auction("AUC-A", seller, item, 500.0);
    activeAuction.start();

    finishedAuction = new Auction("AUC-F", seller, item, 500.0);
    finishedAuction.start();
    finishedAuction.finish();

    cancelledAuction = new Auction("AUC-C", seller, item, 500.0);
    cancelledAuction.cancel();
  }

  // ──────── validateAuctionCreation ────────

  @Nested
  @DisplayName("validateAuctionCreation (EP + BVA)")
  class ValidateAuctionCreation {

    @Test
    @DisplayName("EP-Valid: auction không null không ném exception")
    void validate_validAuction_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validateAuctionCreation(pendingAuction));
    }

    @Test
    @DisplayName("EP-Invalid: auction null ném AuctionException")
    void validate_nullAuction_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> validator.validateAuctionCreation(null));
    }

    @Test
    @DisplayName("EP-Valid: auction active cũng không ném exception")
    void validate_activeAuction_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validateAuctionCreation(activeAuction));
    }
  }

  // ──────── validateAuctionActive ────────

  @Nested
  @DisplayName("validateAuctionActive (EP + BVA)")
  class ValidateAuctionActive {

    @Test
    @DisplayName("EP-Valid: auction active không ném exception")
    void validate_activeAuction_doesNotThrow() {
      assertDoesNotThrow(() -> validator.validateAuctionActive(activeAuction));
    }

    @Test
    @DisplayName("EP-Invalid: auction null ném AuctionException")
    void validate_nullAuction_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> validator.validateAuctionActive(null));
    }

    @Test
    @DisplayName("BVA-Boundary: auction PENDING (không active) ném AuctionClosedException")
    void validate_pendingAuction_throwsAuctionClosedException() {
      assertThrows(AuctionClosedException.class,
          () -> validator.validateAuctionActive(pendingAuction));
    }

    @Test
    @DisplayName("EP-Invalid: auction FINISHED ném AuctionClosedException")
    void validate_finishedAuction_throwsAuctionClosedException() {
      assertThrows(AuctionClosedException.class,
          () -> validator.validateAuctionActive(finishedAuction));
    }

    @Test
    @DisplayName("EP-Invalid: auction CANCELLED ném AuctionClosedException")
    void validate_cancelledAuction_throwsAuctionClosedException() {
      assertThrows(AuctionClosedException.class,
          () -> validator.validateAuctionActive(cancelledAuction));
    }
  }
}
