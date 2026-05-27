package com.auction.backend.observer.observers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionEventType;
import com.auction.backend.observer.FrontendNotifier;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.BidException;
import com.auction.models.auction.Auction;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.payment.BankPayment;
import com.auction.models.user.Bidder;
import com.auction.models.user.Seller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho các observer: AdminObserver, BidderObserver, SellerObserver,
 * FrontendNotificationObserver và BidEventPayload.
 */
@DisplayName("Observers Tests")
class ObserversTest {

  static class CapturingNotifier implements FrontendNotifier {

    String lastAuctionId;
    String lastJson;
    int callCount;

    @Override
    public void sendNotification(String auctionId, String jsonPayload) {
      lastAuctionId = auctionId;
      lastJson = jsonPayload;
      callCount++;
    }
  }

  // ──────── AdminObserver ────────

  @Nested
  @DisplayName("AdminObserver")
  class AdminObserverTests {

    @Test
    @DisplayName("null adminName → AuctionException")
    void constructor_nullName_throws() {
      assertThrows(AuctionException.class, () -> new AdminObserver(null));
    }

    @Test
    @DisplayName("blank adminName → AuctionException")
    void constructor_blankName_throws() {
      assertThrows(AuctionException.class, () -> new AdminObserver("   "));
    }

    @Test
    @DisplayName("update hợp lệ → không ném exception")
    void update_validEvent_noException() {
      final AdminObserver observer = new AdminObserver("AdminA");
      final AuctionEvent event = new AuctionEvent(
          AuctionEventType.AUCTION_STARTED, "AUC-001", "Started");
      observer.update(event);
    }

    @Test
    @DisplayName("toString chứa adminName")
    void toString_containsAdminName() {
      final AdminObserver observer = new AdminObserver("AdminZ");
      assertTrue(observer.toString().contains("AdminZ"));
    }
  }

  // ──────── BidderObserver ────────

  @Nested
  @DisplayName("BidderObserver")
  class BidderObserverTests {

    @Test
    @DisplayName("null bidderName → BidException")
    void constructor_nullName_throws() {
      assertThrows(BidException.class,
          () -> new BidderObserver(null, new CapturingNotifier()));
    }

    @Test
    @DisplayName("null FrontendNotifier → NullPointerException")
    void constructor_nullNotifier_throws() {
      assertThrows(NullPointerException.class,
          () -> new BidderObserver("Alice", null));
    }

    @Test
    @DisplayName("update → gửi notification đúng auctionId và JSON chứa recipient")
    void update_callsNotifierWithCorrectData() {
      final CapturingNotifier notifier = new CapturingNotifier();
      final BidderObserver observer = new BidderObserver("Alice", notifier);
      final AuctionEvent event = new AuctionEvent(
          AuctionEventType.NEW_BID, "AUC-001", "Bid placed");
      observer.update(event);
      assertEquals("AUC-001", notifier.lastAuctionId);
      assertTrue(notifier.lastJson.contains("\"recipient\":\"Alice\""));
    }

    @Test
    @DisplayName("toString chứa bidderName")
    void toString_containsBidderName() {
      final BidderObserver observer = new BidderObserver("Bob", new CapturingNotifier());
      assertTrue(observer.toString().contains("Bob"));
    }
  }

  // ──────── SellerObserver ────────

  @Nested
  @DisplayName("SellerObserver")
  class SellerObserverTests {

    @Test
    @DisplayName("null sellerName → AuctionException")
    void constructor_nullName_throws() {
      assertThrows(AuctionException.class,
          () -> new SellerObserver(null, new CapturingNotifier()));
    }

    @Test
    @DisplayName("null FrontendNotifier → NullPointerException")
    void constructor_nullNotifier_throws() {
      assertThrows(NullPointerException.class,
          () -> new SellerObserver("Carol", null));
    }

    @Test
    @DisplayName("update → gửi notification đúng auctionId và JSON chứa recipient")
    void update_callsNotifierWithCorrectData() {
      final CapturingNotifier notifier = new CapturingNotifier();
      final SellerObserver observer = new SellerObserver("Carol", notifier);
      final AuctionEvent event = new AuctionEvent(
          AuctionEventType.AUCTION_FINISHED, "AUC-002", "Finished");
      observer.update(event);
      assertEquals("AUC-002", notifier.lastAuctionId);
      assertTrue(notifier.lastJson.contains("\"recipient\":\"Carol\""));
    }

    @Test
    @DisplayName("toString chứa sellerName")
    void toString_containsSellerName() {
      final SellerObserver observer = new SellerObserver("Dave", new CapturingNotifier());
      assertTrue(observer.toString().contains("Dave"));
    }
  }

  // ──────── FrontendNotificationObserver ────────

  @Nested
  @DisplayName("FrontendNotificationObserver")
  class FrontendNotificationObserverTests {

    @Test
    @DisplayName("null FrontendNotifier → AuctionException")
    void constructor_nullNotifier_throws() {
      assertThrows(AuctionException.class,
          () -> new FrontendNotificationObserver(null));
    }

    @Test
    @DisplayName("update(null event) → notifier không được gọi")
    void update_nullEvent_notifierNotCalled() {
      final CapturingNotifier notifier = new CapturingNotifier();
      final FrontendNotificationObserver observer =
          new FrontendNotificationObserver(notifier);
      observer.update(null);
      assertEquals(0, notifier.callCount);
    }

    @Test
    @DisplayName("update(valid event) → notifier được gọi với JSON hợp lệ")
    void update_validEvent_notifierCalled() {
      final CapturingNotifier notifier = new CapturingNotifier();
      final FrontendNotificationObserver observer =
          new FrontendNotificationObserver(notifier);
      final AuctionEvent event = new AuctionEvent(
          AuctionEventType.AUCTION_CANCELLED, "AUC-003", "Cancelled");
      observer.update(event);
      assertEquals(1, notifier.callCount);
      assertEquals("AUC-003", notifier.lastAuctionId);
      assertNotNull(notifier.lastJson);
    }

    @Test
    @DisplayName("toString trả về chuỗi không null")
    void toString_notNull() {
      final FrontendNotificationObserver observer =
          new FrontendNotificationObserver(new CapturingNotifier());
      assertNotNull(observer.toString());
    }
  }

  // ──────── BidEventPayload ────────

  @Nested
  @DisplayName("BidEventPayload")
  class BidEventPayloadTests {

    private Auction buildAuction() {
      final Seller seller = new Seller("S-001", "Seller", "s@test.com");
      final AuctionItem item = new AuctionItem(
          "I-001", "Laptop", "Gaming", ItemCategory.ELECTRONICS, "Mới", 10_000_000.0);
      return new Auction("AUC-001", seller, item, 1000.0);
    }

    private com.auction.models.bid.BidTransaction buildTransaction() {
      final Bidder bidder = new Bidder(
          "B-001", "Bidder", "b@test.com", new BankPayment("VCB", "123", "A"));
      return new com.auction.models.bid.BidTransaction(
          "TRANS-001", bidder, "AUC-001", 1500.0);
    }

    @Test
    @DisplayName("null auction → NullPointerException")
    void constructor_nullAuction_throws() {
      final com.auction.models.bid.BidTransaction tx = buildTransaction();
      assertThrows(NullPointerException.class, () -> new BidEventPayload(null, tx));
    }

    @Test
    @DisplayName("null transaction → NullPointerException")
    void constructor_nullTransaction_throws() {
      final Auction auction = buildAuction();
      assertThrows(NullPointerException.class, () -> new BidEventPayload(auction, null));
    }

    @Test
    @DisplayName("getters trả về đúng auction và transaction")
    void getters_returnCorrectObjects() {
      final Auction auction = buildAuction();
      final com.auction.models.bid.BidTransaction tx = buildTransaction();
      final BidEventPayload payload = new BidEventPayload(auction, tx);
      assertEquals(auction, payload.getAuction());
      assertEquals(tx, payload.getTransaction());
    }
  }
}
