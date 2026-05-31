package com.auction.models.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.BidException;
import com.auction.exceptions.InvalidBidException;
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
 * Test cho Auction - lifecycle và bidding logic.
 *
 * <p>EP: auctionId hợp lệ/null/blank; startingPrice hợp lệ/âm
 * BVA: startingPrice = 0 (valid), -0.01 (invalid); 
 * bidAmount = startingPrice (invalid), startingPrice+0.01 (valid)
 */
@DisplayName("Auction Tests")
class AuctionTest {

  private Seller seller;
  private AuctionItem item;
  private Bidder bidder;

  @BeforeEach
  void setUp() {
    seller = new Seller("S-001", "Nguyễn Seller", "seller@test.com");
    item = new AuctionItem("I-001", "Laptop", 
    "Laptop Gaming", ItemCategory.ELECTRONICS, "Mới", 10_000_000.0);
    bidder = new Bidder("B-001", "Trần Bidder", "bidder@test.com",
        new BankPayment("VCB", "123456", "Trần A"));
  }

  private Auction createPendingAuction(double startingPrice) {
    return new Auction("AUC-001", seller, item, startingPrice);
  }

  private Auction createActiveAuction(double startingPrice) {
    Auction auction = createPendingAuction(startingPrice);
    auction.start();
    return auction;
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor (EP + BVA)")
  class ConstructorValidation {

    @Test
    @DisplayName("EP-Valid: tạo auction với dữ liệu hợp lệ")
    void constructor_validData_createsAuction() {
      Auction auction = createPendingAuction(1000.0);
      assertEquals("AUC-001", auction.getAuctionId());
      assertEquals(seller, auction.getSeller());
      assertEquals(item, auction.getItem());
      assertEquals(1000.0, auction.getStartingPrice());
      assertEquals(1000.0, auction.getCurrentHighestBid());
      assertEquals(AuctionStatus.PENDING, auction.getStatus());
      assertNotNull(auction.getCreatedAt());
      assertNull(auction.getStartTime());
      assertNull(auction.getEndTime());
      assertNull(auction.getCurrentHighestBidder());
    }

    @Test
    @DisplayName("EP-Invalid: auctionId null ném AuctionException")
    void constructor_nullAuctionId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new Auction(null, seller, item, 1000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: auctionId rỗng ném AuctionException")
    void constructor_emptyAuctionId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new Auction("", seller, item, 1000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: auctionId blank ném AuctionException")
    void constructor_blankAuctionId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new Auction("   ", seller, item, 1000.0));
    }

    @Test
    @DisplayName("EP-Invalid: seller null ném NullPointerException")
    void constructor_nullSeller_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> new Auction("AUC-001", null, item, 1000.0));
    }

    @Test
    @DisplayName("EP-Invalid: item null ném NullPointerException")
    void constructor_nullItem_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> new Auction("AUC-001", seller, null, 1000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: startingPrice = 0 hợp lệ")
    void constructor_zeroPriceIsValid() {
      Auction auction = createPendingAuction(0.0);
      assertEquals(0.0, auction.getStartingPrice());
    }

    @Test
    @DisplayName("BVA-Boundary: startingPrice = -0.01 ném BidException")
    void constructor_slightlyNegativePrice_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Auction("AUC-001", seller, item, -0.01));
    }

    @Test
    @DisplayName("EP-Invalid: startingPrice âm ném BidException")
    void constructor_negativePriceThrowsBidException() {
      assertThrows(BidException.class,
          () -> createPendingAuction(-100.0));
    }

    @Test
    @DisplayName("EP-Valid: startingPrice lớn hợp lệ")
    void constructor_largePrice_isValid() {
      Auction auction = createPendingAuction(1_000_000_000.0);
      assertEquals(1_000_000_000.0, auction.getStartingPrice());
    }
  }

  // ──────── Lifecycle: start() ────────

  @Nested
  @DisplayName("start() - Lifecycle (EP + BVA)")
  class StartLifecycle {

    @Test
    @DisplayName("EP-Valid: start() từ PENDING chuyển sang ACTIVE")
    void start_fromPending_becomesActive() {
      Auction auction = createPendingAuction(1000.0);
      auction.start();
      assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
      assertTrue(auction.isActive());
      assertNotNull(auction.getStartTime());
    }

    @Test
    @DisplayName("EP-Invalid: start() từ ACTIVE ném IllegalStateException")
    void start_fromActive_throwsIllegalState() {
      Auction auction = createActiveAuction(1000.0);
      assertThrows(IllegalStateException.class, auction::start);
    }

    @Test
    @DisplayName("EP-Invalid: start() từ CANCELLED ném IllegalStateException")
    void start_fromCancelled_throwsIllegalState() {
      Auction auction = createPendingAuction(1000.0);
      auction.cancel();
      assertThrows(IllegalStateException.class, auction::start);
    }
  }

  // ──────── Lifecycle: finish() ────────

  @Nested
  @DisplayName("finish() - Lifecycle (EP)")
  class FinishLifecycle {

    @Test
    @DisplayName("EP-Valid: finish() từ ACTIVE chuyển sang FINISHED")
    void finish_fromActive_becomesFinished() {
      Auction auction = createActiveAuction(1000.0);
      auction.finish();
      assertEquals(AuctionStatus.FINISHED, auction.getStatus());
      assertFalse(auction.isActive());
      assertNotNull(auction.getEndTime());
    }

    @Test
    @DisplayName("EP-Invalid: finish() từ PENDING ném IllegalStateException")
    void finish_fromPending_throwsIllegalState() {
      assertThrows(IllegalStateException.class,
          () -> createPendingAuction(1000.0).finish());
    }

    @Test
    @DisplayName("EP-Invalid: finish() từ CANCELLED ném IllegalStateException")
    void finish_fromCancelled_throwsIllegalState() {
      Auction auction = createPendingAuction(1000.0);
      auction.cancel();
      assertThrows(IllegalStateException.class, auction::finish);
    }
  }

  // ──────── Lifecycle: cancel() ────────

  @Nested
  @DisplayName("cancel() - Lifecycle (EP)")
  class CancelLifecycle {

    @Test
    @DisplayName("EP-Valid: cancel() từ PENDING chuyển sang CANCELLED")
    void cancel_fromPending_becomesCancelled() {
      Auction auction = createPendingAuction(1000.0);
      auction.cancel();
      assertEquals(AuctionStatus.CANCELLED, auction.getStatus());
      assertNotNull(auction.getEndTime());
    }

    @Test
    @DisplayName("EP-Valid: cancel() từ ACTIVE chuyển sang CANCELLED")
    void cancel_fromActive_becomesCancelled() {
      Auction auction = createActiveAuction(1000.0);
      auction.cancel();
      assertEquals(AuctionStatus.CANCELLED, auction.getStatus());
    }

    @Test
    @DisplayName("EP-Invalid: cancel() từ FINISHED ném IllegalStateException")
    void cancel_fromFinished_throwsIllegalState() {
      Auction auction = createActiveAuction(1000.0);
      auction.finish();
      assertThrows(IllegalStateException.class, auction::cancel);
    }
  }

  // ──────── updateHighestBid() ────────

  @Nested
  @DisplayName("updateHighestBid() (EP + BVA)")
  class UpdateHighestBid {

    @Test
    @DisplayName("EP-Valid: bid hợp lệ cập nhật highest bid")
    void updateHighestBid_validAmount_updates() {
      Auction auction = createActiveAuction(1000.0);
      auction.updateHighestBid(bidder, 1500.0);
      assertEquals(1500.0, auction.getCurrentHighestBid());
      assertEquals(bidder, auction.getCurrentHighestBidder());
    }

    @Test
    @DisplayName("BVA-Boundary: bid = currentHighestBid ném InvalidBidException")
    void updateHighestBid_equalToCurrent_throwsInvalidBid() {
      Auction auction = createActiveAuction(1000.0);
      assertThrows(InvalidBidException.class,
          () -> auction.updateHighestBid(bidder, 1000.0));
    }

    @Test
    @DisplayName("BVA-Boundary: bid = currentHighestBid + 0.01 hợp lệ")
    void updateHighestBid_justAboveCurrent_isValid() {
      Auction auction = createActiveAuction(1000.0);
      auction.updateHighestBid(bidder, 1000.01);
      assertEquals(1000.01, auction.getCurrentHighestBid(), 0.001);
    }

    @Test
    @DisplayName("BVA-Boundary: bid nhỏ hơn currentHighestBid ném InvalidBidException")
    void updateHighestBid_belowCurrent_throwsInvalidBid() {
      Auction auction = createActiveAuction(1000.0);
      assertThrows(InvalidBidException.class,
          () -> auction.updateHighestBid(bidder, 999.0));
    }

    @Test
    @DisplayName("EP-Invalid: bid amount = 0 ném BidException")
    void updateHighestBid_zeroAmount_throwsBidException() {
      Auction auction = createActiveAuction(0.0);
      assertThrows(BidException.class,
          () -> auction.updateHighestBid(bidder, 0.0));
    }

    @Test
    @DisplayName("EP-Invalid: bid amount âm ném BidException")
    void updateHighestBid_negativeAmount_throwsBidException() {
      Auction auction = createActiveAuction(1000.0);
      assertThrows(BidException.class,
          () -> auction.updateHighestBid(bidder, -100.0));
    }

    @Test
    @DisplayName("EP-Invalid: bidder null ném BidException")
    void updateHighestBid_nullBidder_throwsBidException() {
      Auction auction = createActiveAuction(1000.0);
      assertThrows(BidException.class,
          () -> auction.updateHighestBid(null, 2000.0));
    }

    @Test
    @DisplayName("EP-Invalid: auction không active ném AuctionClosedException")
    void updateHighestBid_notActive_throwsAuctionClosed() {
      Auction auction = createPendingAuction(1000.0);
      assertThrows(AuctionClosedException.class,
          () -> auction.updateHighestBid(bidder, 2000.0));
    }

    @Test
    @DisplayName("EP-Invalid: auction FINISHED ném AuctionClosedException")
    void updateHighestBid_afterFinish_throwsAuctionClosed() {
      Auction auction = createActiveAuction(1000.0);
      auction.finish();
      assertThrows(AuctionClosedException.class,
          () -> auction.updateHighestBid(bidder, 2000.0));
    }

    @Test
    @DisplayName("EP-Valid: bid liên tiếp cập nhật đúng highest bid")
    void updateHighestBid_consecutive_updatesCorrectly() {
      Auction auction = createActiveAuction(1000.0);
      Bidder bidder2 = new Bidder("B-002", "Bidder2", "b2@test.com",
          new BankPayment("VCB", "999", "Bidder2"));

      auction.updateHighestBid(bidder, 1500.0);
      auction.updateHighestBid(bidder2, 2000.0);

      assertEquals(2000.0, auction.getCurrentHighestBid());
      assertEquals(bidder2, auction.getCurrentHighestBidder());
    }
  }

  // ──────── setDurationSeconds / getDurationSeconds ────────

  @Nested
  @DisplayName("setDurationSeconds / getDurationSeconds (EP + BVA)")
  class DurationSeconds {

    @Test
    @DisplayName("EP-Valid: set và get durationSeconds đúng")
    void setGetDuration_valid() {
      Auction a = createPendingAuction(1000.0);
      a.setDurationSeconds(3600);
      assertEquals(3600, a.getDurationSeconds());
    }

    @Test
    @DisplayName("BVA-Boundary: durationSeconds = 0")
    void setDuration_zero() {
      Auction a = createPendingAuction(1000.0);
      a.setDurationSeconds(0);
      assertEquals(0, a.getDurationSeconds());
    }

    @Test
    @DisplayName("EP-Valid: durationSeconds lớn")
    void setDuration_large() {
      Auction a = createPendingAuction(1000.0);
      a.setDurationSeconds(86400);
      assertEquals(86400, a.getDurationSeconds());
    }
  }

  // ──────── setStartingPrice ────────

  @Nested
  @DisplayName("setStartingPrice (EP + BVA)")
  class SetStartingPrice {

    @Test
    @DisplayName("EP-Valid: setStartingPrice khi PENDING cập nhật cả currentHighestBid")
    void setStartingPrice_pending_updatesBoth() {
      Auction a = createPendingAuction(1000.0);
      a.setStartingPrice(2000.0);
      assertEquals(2000.0, a.getStartingPrice());
      assertEquals(2000.0, a.getCurrentHighestBid());
    }

    @Test
    @DisplayName("EP-Valid: setStartingPrice khi ACTIVE không cập nhật currentHighestBid")
    void setStartingPrice_active_doesNotUpdateCurrentBid() {
      Auction a = createActiveAuction(1000.0);
      a.updateHighestBid(bidder, 1500.0);
      a.setStartingPrice(500.0);
      assertEquals(500.0, a.getStartingPrice());
      assertEquals(1500.0, a.getCurrentHighestBid());
    }

    @Test
    @DisplayName("BVA-Boundary: setStartingPrice = 0 hợp lệ")
    void setStartingPrice_zero_valid() {
      Auction a = createPendingAuction(1000.0);
      a.setStartingPrice(0.0);
      assertEquals(0.0, a.getStartingPrice());
    }

    @Test
    @DisplayName("EP-Invalid: setStartingPrice âm ném BidException")
    void setStartingPrice_negative_throws() {
      Auction a = createPendingAuction(1000.0);
      assertThrows(com.auction.exceptions.BidException.class,
          () -> a.setStartingPrice(-1.0));
    }
  }

  // ──────── setScheduledEndTime / getScheduledEndTime ────────

  @Nested
  @DisplayName("setScheduledEndTime / getScheduledEndTime")
  class ScheduledEndTime {

    @Test
    @DisplayName("EP-Valid: set và get scheduledEndTime đúng")
    void setGetScheduledEndTime_valid() {
      Auction a = createPendingAuction(1000.0);
      java.time.LocalDateTime time = java.time.LocalDateTime.now().plusHours(1);
      a.setScheduledEndTime(time);
      assertEquals(time, a.getScheduledEndTime());
    }

    @Test
    @DisplayName("EP-Valid: setScheduledEndTime null được chấp nhận")
    void setScheduledEndTime_null_accepted() {
      Auction a = createPendingAuction(1000.0);
      a.setScheduledEndTime(null);
      assertNull(a.getScheduledEndTime());
    }
  }

  // ──────── extendScheduledEndTime ────────

  @Nested
  @DisplayName("extendScheduledEndTime (EP)")
  class ExtendScheduledEndTime {

    @Test
    @DisplayName("EP-Valid: gia hạn từ scheduledEndTime đã đặt")
    void extend_fromExistingTime_adds() {
      Auction a = createActiveAuction(1000.0);
      java.time.LocalDateTime base = java.time.LocalDateTime.now().plusHours(1);
      a.setScheduledEndTime(base);
      a.extendScheduledEndTime(300);
      assertEquals(base.plusSeconds(300), a.getScheduledEndTime());
    }

    @Test
    @DisplayName("EP-Valid: gia hạn khi scheduledEndTime null — set từ now")
    void extend_fromNull_setsFromNow() {
      Auction a = createPendingAuction(1000.0);
      assertNull(a.getScheduledEndTime());
      a.extendScheduledEndTime(600);
      assertNotNull(a.getScheduledEndTime());
    }
  }

  // ──────── getLock ────────

  @Test
  @DisplayName("getLock trả về non-null khi tạo qua constructor")
  void getLock_notNull() {
    assertNotNull(createPendingAuction(1000.0).getLock());
  }

  // ──────── getCreatedAt / getStartTime / getEndTime ────────

  @Test
  @DisplayName("getCreatedAt non-null khi tạo auction")
  void getCreatedAt_notNull() {
    assertNotNull(createPendingAuction(500.0).getCreatedAt());
  }

  @Test
  @DisplayName("getStartTime null trước khi start")
  void getStartTime_nullBeforeStart() {
    assertNull(createPendingAuction(500.0).getStartTime());
  }

  @Test
  @DisplayName("getEndTime null trước khi finish/cancel")
  void getEndTime_nullBeforeEnd() {
    assertNull(createActiveAuction(500.0).getEndTime());
  }

  // ──────── toString ────────

  @Test
  @DisplayName("toString không null")
  void toString_isNotNull() {
    assertNotNull(createActiveAuction(1000.0).toString());
  }
}
