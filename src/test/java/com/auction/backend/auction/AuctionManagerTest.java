package com.auction.backend.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Test cho AuctionManager (Singleton + CRUD in-memory).
 *
 * <p>EP: addAuction hợp lệ/null; removeAuction tồn tại/không tồn tại;
 *      findAuction tồn tại/không tồn tại; getInstance trả cùng instance.
 * BVA: map rỗng (0 auction), 1 auction, nhiều auction; getAllAuctions trả unmodifiable.
 *
 * <p>Lưu ý: AuctionManager là Singleton nên setUp() phải xóa sạch state
 * giữa các test để đảm bảo test isolation.
 */
@DisplayName("AuctionManager Tests")
class AuctionManagerTest {

  private AuctionManager manager;

  @BeforeEach
  void setUp() {
    manager = AuctionManager.getInstance();
    // Xóa sạch trạng thái singleton giữa các test bằng cách xóa tất cả
    manager.getAllAuctions()
        .stream()
        .map(Auction::getAuctionId)
        .toList()
        .forEach(manager::removeAuction);
  }

  private Auction createAuction(String auctionId) {
    Seller seller = new Seller("S-001", "Seller", "s@test.com");
    AuctionItem item = new AuctionItem("I-001", "Item", "Desc", ItemCategory.ART, "New", 100.0);
    return new Auction(auctionId, seller, item, 500.0);
  }

  // ──────── Singleton ────────

  @Nested
  @DisplayName("Singleton")
  class SingletonTest {

    @Test
    @DisplayName("getInstance() luôn trả cùng instance")
    void getInstance_returnsSameInstance() {
      AuctionManager m1 = AuctionManager.getInstance();
      AuctionManager m2 = AuctionManager.getInstance();
      assertSame(m1, m2);
    }
  }

  // ──────── addAuction ────────

  @Nested
  @DisplayName("addAuction (EP + BVA)")
  class AddAuction {

    @Test
    @DisplayName("EP-Valid: thêm auction lưu vào map")
    void addAuction_validAuction_stored() {
      Auction auction = createAuction("AUC-001");
      manager.addAuction(auction);
      assertNotNull(manager.findAuction("AUC-001"));
    }

    @Test
    @DisplayName("EP-Invalid: addAuction null ném AuctionException")
    void addAuction_null_throwsAuctionException() {
      assertThrows(AuctionException.class, () -> manager.addAuction(null));
    }

    @Test
    @DisplayName("BVA-Boundary: thêm nhiều auction lưu tất cả")
    void addAuction_multiple_allStored() {
      manager.addAuction(createAuction("AUC-A"));
      manager.addAuction(createAuction("AUC-B"));
      manager.addAuction(createAuction("AUC-C"));
      assertEquals(3, manager.getAllAuctions().size());
    }
  }

  // ──────── removeAuction ────────

  @Nested
  @DisplayName("removeAuction (EP + BVA)")
  class RemoveAuction {

    @Test
    @DisplayName("EP-Valid: xóa auction tồn tại")
    void removeAuction_existingId_removed() {
      manager.addAuction(createAuction("AUC-001"));
      manager.removeAuction("AUC-001");
      assertNull(manager.findAuction("AUC-001"));
    }

    @Test
    @DisplayName("EP-Invalid: xóa auction không tồn tại không ném exception")
    void removeAuction_nonExistingId_noException() {
      manager.removeAuction("NON-EXISTENT");
      // không ném exception
    }

    @Test
    @DisplayName("BVA-Boundary: sau khi xóa hết, getAllAuctions rỗng")
    void removeAuction_allAuctions_emptyMap() {
      manager.addAuction(createAuction("AUC-X"));
      manager.removeAuction("AUC-X");
      assertTrue(manager.getAllAuctions().isEmpty());
    }
  }

  // ──────── findAuction ────────

  @Nested
  @DisplayName("findAuction (EP + BVA)")
  class FindAuction {

    @Test
    @DisplayName("EP-Valid: tìm auction tồn tại trả đúng auction")
    void findAuction_existingId_returnsAuction() {
      Auction auction = createAuction("AUC-FIND");
      manager.addAuction(auction);
      assertSame(auction, manager.findAuction("AUC-FIND"));
    }

    @Test
    @DisplayName("EP-Invalid: tìm auction không tồn tại trả null")
    void findAuction_nonExistingId_returnsNull() {
      assertNull(manager.findAuction("NO-SUCH-AUCTION"));
    }

    @Test
    @DisplayName("BVA-Boundary: findAuction khi map rỗng trả null")
    void findAuction_emptyMap_returnsNull() {
      assertNull(manager.findAuction("ANY-ID"));
    }
  }

  // ──────── getAllAuctions ────────

  @Nested
  @DisplayName("getAllAuctions (EP + BVA)")
  class GetAllAuctions {

    @Test
    @DisplayName("BVA-Boundary: map rỗng trả collection rỗng")
    void getAllAuctions_emptyMap_returnsEmpty() {
      assertTrue(manager.getAllAuctions().isEmpty());
    }

    @Test
    @DisplayName("EP-Valid: trả unmodifiable collection")
    void getAllAuctions_returnsUnmodifiableCollection() {
      manager.addAuction(createAuction("AUC-001"));
      assertThrows(UnsupportedOperationException.class,
          () -> manager.getAllAuctions().clear());
    }
  }
}
