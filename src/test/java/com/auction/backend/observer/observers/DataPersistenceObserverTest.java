package com.auction.backend.observer.observers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.backend.database.dao.AuctionDao;
import com.auction.backend.database.dao.BidDao;
import com.auction.backend.database.dao.ItemDao;
import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionEventType;
import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.BidTransaction;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.payment.BankPayment;
import com.auction.models.user.Bidder;
import com.auction.models.user.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test cho DataPersistenceObserver — routing event type → đúng DAO method.
 *
 * <p>Dùng fake DAO (extend thật, override no-op) để tránh kết nối SQLite.
 */
@DisplayName("DataPersistenceObserver Tests")
class DataPersistenceObserverTest {

  static class FakeItemDao extends ItemDao {

    boolean savedItem;

    @Override
    public void saveItem(AuctionItem item) {
      savedItem = true;
    }
  }

  static class FakeAuctionDao extends AuctionDao {

    boolean savedAuction;
    boolean updatedAuction;

    @Override
    public void saveAuction(Auction auction) {
      savedAuction = true;
    }

    @Override
    public void updateAuction(Auction auction) {
      updatedAuction = true;
    }
  }

  static class FakeBidDao extends BidDao {

    boolean savedBid;

    @Override
    public void saveBidTransaction(BidTransaction tx) {
      savedBid = true;
    }
  }

  private FakeItemDao fakeItemDao;
  private FakeAuctionDao fakeAuctionDao;
  private FakeBidDao fakeBidDao;
  private DataPersistenceObserver observer;

  @BeforeEach
  void setUp() {
    fakeItemDao = new FakeItemDao();
    fakeAuctionDao = new FakeAuctionDao();
    fakeBidDao = new FakeBidDao();
    observer = new DataPersistenceObserver(fakeItemDao, fakeAuctionDao, fakeBidDao);
  }

  private Auction buildAuction() {
    final Seller seller = new Seller("S-001", "Seller", "s@test.com");
    final AuctionItem item = new AuctionItem(
        "I-001", "Item", "Desc", ItemCategory.ELECTRONICS, "Mới", 1000.0);
    return new Auction("AUC-001", seller, item, 500.0);
  }

  private BidEventPayload buildBidPayload() {
    final Bidder bidder = new Bidder(
        "B-001", "Bidder", "b@test.com", new BankPayment("VCB", "123", "A"));
    final BidTransaction tx = new BidTransaction("TX-001", bidder, "AUC-001", 600.0);
    return new BidEventPayload(buildAuction(), tx);
  }

  // ──────── Constructor ────────

  @Test
  @DisplayName("null itemDao → NullPointerException")
  void constructor_nullItemDao_throws() {
    assertThrows(NullPointerException.class,
        () -> new DataPersistenceObserver(null, fakeAuctionDao, fakeBidDao));
  }

  @Test
  @DisplayName("null auctionDao → NullPointerException")
  void constructor_nullAuctionDao_throws() {
    assertThrows(NullPointerException.class,
        () -> new DataPersistenceObserver(fakeItemDao, null, fakeBidDao));
  }

  @Test
  @DisplayName("null bidDao → NullPointerException")
  void constructor_nullBidDao_throws() {
    assertThrows(NullPointerException.class,
        () -> new DataPersistenceObserver(fakeItemDao, fakeAuctionDao, null));
  }

  // ──────── update routing ────────

  @Test
  @DisplayName("update(null) → không ném exception")
  void update_nullEvent_noException() {
    observer.update(null);
  }

  @Test
  @DisplayName("AUCTION_CREATED → saveItem + saveAuction được gọi")
  void update_auctionCreated_savesItemAndAuction() {
    final Auction auction = buildAuction();
    final AuctionEvent event = new AuctionEvent(
        AuctionEventType.AUCTION_CREATED, "AUC-001", "Created", auction);
    observer.update(event);
    assertTrue(fakeItemDao.savedItem);
    assertTrue(fakeAuctionDao.savedAuction);
  }

  @Test
  @DisplayName("AUCTION_FINISHED → updateAuction được gọi")
  void update_auctionFinished_updatesAuction() {
    final AuctionEvent event = new AuctionEvent(
        AuctionEventType.AUCTION_FINISHED, "AUC-001", "Finished", buildAuction());
    observer.update(event);
    assertTrue(fakeAuctionDao.updatedAuction);
  }

  @Test
  @DisplayName("AUCTION_CANCELLED → updateAuction được gọi")
  void update_auctionCancelled_updatesAuction() {
    final AuctionEvent event = new AuctionEvent(
        AuctionEventType.AUCTION_CANCELLED, "AUC-001", "Cancelled", buildAuction());
    observer.update(event);
    assertTrue(fakeAuctionDao.updatedAuction);
  }

  @Test
  @DisplayName("AUCTION_EXTENDED → updateAuction được gọi")
  void update_auctionExtended_updatesAuction() {
    final AuctionEvent event = new AuctionEvent(
        AuctionEventType.AUCTION_EXTENDED, "AUC-001", "Extended", buildAuction());
    observer.update(event);
    assertTrue(fakeAuctionDao.updatedAuction);
  }

  @Test
  @DisplayName("NEW_BID → saveBidTransaction + updateAuction được gọi")
  void update_newBid_savesBidAndUpdatesAuction() {
    final AuctionEvent event = new AuctionEvent(
        AuctionEventType.NEW_BID, "AUC-001", "Bid", buildBidPayload());
    observer.update(event);
    assertTrue(fakeBidDao.savedBid);
    assertTrue(fakeAuctionDao.updatedAuction);
  }

  @Test
  @DisplayName("AUTO_BID_PLACED → saveBidTransaction + updateAuction được gọi")
  void update_autoBidPlaced_savesBidAndUpdatesAuction() {
    final AuctionEvent event = new AuctionEvent(
        AuctionEventType.AUTO_BID_PLACED, "AUC-001", "AutoBid", buildBidPayload());
    observer.update(event);
    assertTrue(fakeBidDao.savedBid);
    assertTrue(fakeAuctionDao.updatedAuction);
  }

  @Test
  @DisplayName("AUTO_BID_REGISTERED → không gọi bất kỳ DAO nào")
  void update_autoBidRegistered_noDaoCalled() {
    final AuctionEvent event = new AuctionEvent(
        AuctionEventType.AUTO_BID_REGISTERED, "AUC-001", "Registered");
    observer.update(event);
    assertTrue(!fakeItemDao.savedItem && !fakeAuctionDao.savedAuction
        && !fakeAuctionDao.updatedAuction && !fakeBidDao.savedBid);
  }

  @Test
  @DisplayName("AUCTION_CREATED với payload sai kiểu → AuctionException")
  void update_auctionCreatedWrongPayload_throws() {
    final AuctionEvent event = new AuctionEvent(
        AuctionEventType.AUCTION_CREATED, "AUC-001", "Created", "notAnAuction");
    assertThrows(AuctionException.class, () -> observer.update(event));
  }

  @Test
  @DisplayName("NEW_BID với payload sai kiểu → AuctionException")
  void update_newBidWrongPayload_throws() {
    final AuctionEvent event = new AuctionEvent(
        AuctionEventType.NEW_BID, "AUC-001", "Bid", "notABidPayload");
    assertThrows(AuctionException.class, () -> observer.update(event));
  }
}
