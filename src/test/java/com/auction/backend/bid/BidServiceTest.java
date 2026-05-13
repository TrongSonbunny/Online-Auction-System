package com.auction.backend.bid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.BidException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.BidTransaction;
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
 * Test cho BidService - placeBid.
 *
 * <p>EP: auction active/inactive; bidder có/không có quyền;
 *      amount hợp lệ / bằng giá hiện tại / thấp hơn
 * BVA: amount = currentHighestBid (invalid), currentHighestBid+0.01 (valid)
 */
@DisplayName("BidService Tests")
class BidServiceTest {

  private BidService bidService;
  private Auction activeAuction;
  private Auction pendingAuction;
  private Bidder bidder;

  @BeforeEach
  void setUp() {
    bidService = new BidService();
    bidder = new Bidder("B-001", "Bidder", "b@test.com",
        new BankPayment("VCB", "123456", "A"));

    Seller seller = new Seller("S-001", "Seller", "s@test.com");
    AuctionItem item = new AuctionItem(
        "I-001", "Laptop", "Gaming", ItemCategory.ELECTRONICS, "Mới", 10_000_000.0);

    activeAuction = new Auction("AUC-001", seller, item, 1000.0);
    activeAuction.start();

    pendingAuction = new Auction("AUC-002", seller, item, 1000.0);
  }

  // ──────── placeBid ────────

  @Nested
  @DisplayName("placeBid - Success (EP + BVA)")
  class PlaceBidSuccess {

    @Test
    @DisplayName("EP-Valid: placeBid hợp lệ trả BidTransaction")
    void placeBid_valid_returnsBidTransaction() {
      BidTransaction tx = bidService.placeBid(activeAuction, bidder, 1500.0);
      assertNotNull(tx);
      assertEquals(bidder, tx.getBidder());
      assertEquals("AUC-001", tx.getAuctionId());
      assertEquals(1500.0, tx.getBidAmount());
    }

    @Test
    @DisplayName("EP-Valid: transactionId có prefix TRANS-")
    void placeBid_valid_transactionIdHasPrefix() {
      BidTransaction tx = bidService.placeBid(activeAuction, bidder, 1500.0);
      assertTrue(tx.getTransactionId().startsWith("TRANS-"));
    }

    @Test
    @DisplayName("EP-Valid: cập nhật currentHighestBid trên auction")
    void placeBid_valid_updatesAuctionHighestBid() {
      bidService.placeBid(activeAuction, bidder, 1500.0);
      assertEquals(1500.0, activeAuction.getCurrentHighestBid());
    }

    @Test
    @DisplayName("EP-Valid: tăng totalBidsPlaced của bidder")
    void placeBid_valid_incrementsBidderCount() {
      bidService.placeBid(activeAuction, bidder, 1500.0);
      assertEquals(1, bidder.getTotalBidsPlaced());
    }

    @Test
    @DisplayName("EP-Valid: lưu vào BidHistoryManager")
    void placeBid_valid_storedInHistory() {
      bidService.placeBid(activeAuction, bidder, 1500.0);
      assertEquals(1, bidService.getBidHistoryManager().getTotalBids());
    }

    @Test
    @DisplayName("BVA-Boundary: amount = currentHighestBid + 0.01 hợp lệ")
    void placeBid_justAboveCurrent_valid() {
      BidTransaction tx = bidService.placeBid(activeAuction, bidder, 1000.01);
      assertEquals(1000.01, tx.getBidAmount(), 0.001);
    }
  }

  @Nested
  @DisplayName("placeBid - Failures (EP + BVA)")
  class PlaceBidFailure {

    @Test
    @DisplayName("EP-Invalid: auction null ném NullPointerException (synchronized trước validator)")
    void placeBid_nullAuction_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> bidService.placeBid(null, bidder, 1500.0));
    }

    @Test
    @DisplayName("EP-Invalid: bidder null ném BidException")
    void placeBid_nullBidder_throwsBidException() {
      assertThrows(BidException.class,
          () -> bidService.placeBid(activeAuction, null, 1500.0));
    }

    @Test
    @DisplayName("EP-Invalid: auction không active ném AuctionClosedException")
    void placeBid_pendingAuction_throwsAuctionClosedException() {
      assertThrows(AuctionClosedException.class,
          () -> bidService.placeBid(pendingAuction, bidder, 1500.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = 0 ném BidException")
    void placeBid_zeroAmount_throwsBidException() {
      assertThrows(BidException.class,
          () -> bidService.placeBid(activeAuction, bidder, 0.0));
    }

    @Test
    @DisplayName("BVA-Boundary: amount = currentHighestBid (1000) ném BidException")
    void placeBid_equalToCurrent_throwsBidException() {
      assertThrows(BidException.class,
          () -> bidService.placeBid(activeAuction, bidder, 1000.0));
    }

    @Test
    @DisplayName("EP-Invalid: amount nhỏ hơn currentHighestBid ném BidException")
    void placeBid_belowCurrent_throwsBidException() {
      assertThrows(BidException.class,
          () -> bidService.placeBid(activeAuction, bidder, 500.0));
    }

    @Test
    @DisplayName("EP-Invalid: amount âm ném BidException")
    void placeBid_negativeAmount_throwsBidException() {
      assertThrows(BidException.class,
          () -> bidService.placeBid(activeAuction, bidder, -100.0));
    }
  }

  @Nested
  @DisplayName("placeBid - Multiple Bids (EP)")
  class PlaceBidMultiple {

    @Test
    @DisplayName("EP-Valid: nhiều bid liên tiếp lưu đúng lịch sử")
    void placeBid_consecutive_allStoredInHistory() {
      Bidder bidder2 = new Bidder("B-002", "Bidder2", "b2@test.com",
          new BankPayment("MB", "999", "B2"));
      bidService.placeBid(activeAuction, bidder, 1500.0);
      bidService.placeBid(activeAuction, bidder2, 2000.0);
      assertEquals(2, bidService.getBidHistoryManager().getTotalBids());
      assertEquals(2000.0, activeAuction.getCurrentHighestBid());
    }
  }
}
