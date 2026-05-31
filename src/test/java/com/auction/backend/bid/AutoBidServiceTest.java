package com.auction.backend.bid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.AuctionException;
import com.auction.exceptions.BidException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.AutoBid;
import com.auction.models.bid.BidTransaction;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.payment.BankPayment;
import com.auction.models.user.Bidder;
import com.auction.models.user.Seller;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho AutoBidService.
 *
 * <p>EP: điều kiện đăng ký hợp lệ/không hợp lệ; cascade scenario.
 * BVA: maxBid = currentBid (invalid), maxBid = currentBid + 0.01 (valid);
 *      increment = 0 (invalid), increment = 0.01 (valid).
 */
@DisplayName("AutoBidService Tests")
class AutoBidServiceTest {

  private AutoBidService autoBidService;
  private AutoBidManager autoBidManager;
  private BidHistoryManager bidHistoryManager;
  private Auction activeAuction;
  private Auction pendingAuction;
  private Bidder bidder1;
  private Bidder bidder2;

  @BeforeEach
  void setUp() {
    autoBidManager = new AutoBidManager();
    bidHistoryManager = new BidHistoryManager();
    autoBidService = new AutoBidService(autoBidManager, bidHistoryManager);

    Seller seller = new Seller("S-AUTO-001", "Seller", "auto_seller@test.com");
    AuctionItem item = new AuctionItem("I-AUTO-001", "Watch", "Luxury watch",
        ItemCategory.COLLECTIBLE, "Mới", 50_000_000.0);

    activeAuction = new Auction("AUTO-AUC-001", seller, item, 1000.0);
    activeAuction.start();

    pendingAuction = new Auction("AUTO-AUC-002", seller, item, 1000.0);

    bidder1 = new Bidder("B-AUTO-001", "Bidder1", "auto_b1@test.com",
        new BankPayment("VCB", "111", "Bidder1"));
    bidder2 = new Bidder("B-AUTO-002", "Bidder2", "auto_b2@test.com",
        new BankPayment("MB", "222", "Bidder2"));
  }

  // ──────── registerAutoBid ────────

  @Nested
  @DisplayName("registerAutoBid (EP + BVA)")
  class RegisterAutoBid {

    @Test
    @DisplayName("EP-Valid: đăng ký auto-bid hợp lệ trả AutoBid")
    void register_valid_returnsAutoBid() {
      AutoBid ab = autoBidService.registerAutoBid(activeAuction, bidder1, 5000.0, 500.0);
      assertNotNull(ab);
      assertEquals(bidder1, ab.getBidder());
      assertEquals(5000.0, ab.getMaxBid());
      assertEquals(500.0, ab.getIncrement());
      assertEquals("AUTO-AUC-001", ab.getAuctionId());
      assertTrue(ab.getAutoBidId().startsWith("ABID-"));
    }

    @Test
    @DisplayName("EP-Valid: auto-bid được lưu vào AutoBidManager")
    void register_valid_storedInManager() {
      autoBidService.registerAutoBid(activeAuction, bidder1, 5000.0, 500.0);
      assertFalse(autoBidManager.getAutoBidsForAuction("AUTO-AUC-001").isEmpty());
    }

    @Test
    @DisplayName("EP-Invalid: auction null ném AuctionException")
    void register_nullAuction_throws() {
      assertThrows(AuctionException.class,
          () -> autoBidService.registerAutoBid(null, bidder1, 5000.0, 500.0));
    }

    @Test
    @DisplayName("EP-Invalid: bidder null ném BidException")
    void register_nullBidder_throws() {
      assertThrows(BidException.class,
          () -> autoBidService.registerAutoBid(activeAuction, null, 5000.0, 500.0));
    }

    @Test
    @DisplayName("EP-Invalid: auction không active ném AuctionException")
    void register_pendingAuction_throws() {
      assertThrows(AuctionException.class,
          () -> autoBidService.registerAutoBid(pendingAuction, bidder1, 5000.0, 500.0));
    }

    @Test
    @DisplayName("BVA-Boundary: maxBid = currentHighestBid ném BidException")
    void register_maxBidEqualsCurrent_throws() {
      assertThrows(BidException.class,
          () -> autoBidService.registerAutoBid(activeAuction, bidder1, 1000.0, 500.0));
    }

    @Test
    @DisplayName("BVA-Boundary: maxBid = currentHighestBid + 0.01 hợp lệ")
    void register_maxBidJustAboveCurrent_valid() {
      AutoBid ab = autoBidService.registerAutoBid(activeAuction, bidder1, 1000.01, 0.01);
      assertNotNull(ab);
    }

    @Test
    @DisplayName("BVA-Boundary: increment = 0 ném BidException")
    void register_zeroIncrement_throws() {
      assertThrows(BidException.class,
          () -> autoBidService.registerAutoBid(activeAuction, bidder1, 5000.0, 0.0));
    }

    @Test
    @DisplayName("BVA-Boundary: increment âm ném BidException")
    void register_negativeIncrement_throws() {
      assertThrows(BidException.class,
          () -> autoBidService.registerAutoBid(activeAuction, bidder1, 5000.0, -100.0));
    }
  }

  // ──────── cancelAutoBid ────────

  @Nested
  @DisplayName("cancelAutoBid (EP)")
  class CancelAutoBid {

    @Test
    @DisplayName("EP-Valid: hủy auto-bid tồn tại trả true")
    void cancel_existing_returnsTrue() {
      AutoBid ab = autoBidService.registerAutoBid(activeAuction, bidder1, 5000.0, 500.0);
      assertTrue(autoBidService.cancelAutoBid(ab.getAutoBidId()));
    }

    @Test
    @DisplayName("EP-Invalid: hủy auto-bid không tồn tại trả false")
    void cancel_nonExistent_returnsFalse() {
      assertFalse(autoBidService.cancelAutoBid("NON-EXISTENT-ID"));
    }

    @Test
    @DisplayName("EP-Valid: sau khi hủy, auto-bid không còn trong manager")
    void cancel_removedFromManager() {
      AutoBid ab = autoBidService.registerAutoBid(activeAuction, bidder1, 5000.0, 500.0);
      autoBidService.cancelAutoBid(ab.getAutoBidId());
      assertTrue(autoBidManager.getAutoBidsForAuction("AUTO-AUC-001").isEmpty());
    }
  }

  // ──────── processAutoBids (cascade) ────────

  @Nested
  @DisplayName("processAutoBids (cascade)")
  class ProcessAutoBids {

    @Test
    @DisplayName("EP-Valid: không có auto-bid → cascade rỗng")
    void process_noAutoBids_returnsEmpty() {
      List<BidTransaction> txs =
          autoBidService.processAutoBids(activeAuction, bidder1);
      assertTrue(txs.isEmpty());
    }

    @Test
    @DisplayName("EP-Valid: bidder bị outbid → auto-bid kích hoạt")
    void process_outbidBidder_triggersAutoBid() {
      autoBidService.registerAutoBid(activeAuction, bidder1, 3000.0, 500.0);

      activeAuction.updateHighestBid(bidder2, 1500.0);
      List<BidTransaction> txs =
          autoBidService.processAutoBids(activeAuction, bidder1);

      assertFalse(txs.isEmpty());
      assertEquals(bidder1, txs.get(0).getBidder());
      assertTrue(activeAuction.getCurrentHighestBid() > 1500.0);
    }

    @Test
    @DisplayName("EP-Valid: cascade hai auto-bid đối nhau")
    void process_twoBidders_cascades() {
      autoBidService.registerAutoBid(activeAuction, bidder1, 4000.0, 300.0);
      autoBidService.registerAutoBid(activeAuction, bidder2, 5000.0, 300.0);

      activeAuction.updateHighestBid(bidder1, 1500.0);
      List<BidTransaction> txs =
          autoBidService.processAutoBids(activeAuction, bidder2);

      assertFalse(txs.isEmpty());
    }

    @Test
    @DisplayName("EP-Valid: maxBid đã đạt tới → cascade dừng")
    void process_maxBidReached_stopsCascade() {
      autoBidService.registerAutoBid(activeAuction, bidder1, 1200.0, 500.0);

      activeAuction.updateHighestBid(bidder2, 1100.0);
      List<BidTransaction> txs =
          autoBidService.processAutoBids(activeAuction, bidder1);

      assertTrue(txs.isEmpty() || activeAuction.getCurrentHighestBid() <= 1200.0);
    }

    @Test
    @DisplayName("EP-Valid: outbidBidder null → cascade rỗng")
    void process_nullOutbidBidder_returnsEmpty() {
      autoBidService.registerAutoBid(activeAuction, bidder1, 3000.0, 500.0);
      List<BidTransaction> txs =
          autoBidService.processAutoBids(activeAuction, null);
      assertTrue(txs.isEmpty());
    }

    @Test
    @DisplayName("EP-Valid: auction không active → cascade dừng ngay")
    void process_inactiveAuction_returnsEmpty() {
      autoBidService.registerAutoBid(activeAuction, bidder1, 3000.0, 500.0);
      pendingAuction = activeAuction;
      activeAuction.finish();
      List<BidTransaction> txs =
          autoBidService.processAutoBids(activeAuction, bidder2);
      assertTrue(txs.isEmpty());
    }
  }

  // ──────── getAutoBidManager ────────

  @Test
  @DisplayName("getAutoBidManager trả về non-null")
  void getAutoBidManager_returnsNonNull() {
    assertNotNull(autoBidService.getAutoBidManager());
  }
}
