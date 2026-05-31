package com.auction.backend.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.backend.observer.AuctionEventPublisher;
import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.auction.AuctionStatus;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.user.Seller;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho AuctionService.
 *
 * <p>EP: seller hợp lệ/null; auction tồn tại/không tồn tại;
 *      trạng thái đúng/sai lifecycle.
 */
@DisplayName("AuctionService Tests")
class AuctionServiceTest {

  private AuctionService auctionService;
  private AuctionScheduler scheduler;
  private AuctionManager auctionManager;
  private Seller seller;
  private AuctionItem item;
  private final List<String> createdIds = new ArrayList<>();

  @BeforeEach
  void setUp() {
    scheduler = new AuctionScheduler();
    auctionService = new AuctionService(scheduler, new AuctionEventPublisher());
    auctionManager = AuctionManager.getInstance();
    seller = new Seller("S-SVC-001", "Test Seller", "svc_seller@test.com");
    item = new AuctionItem("I-SVC-001", "Test Item", "Description",
        ItemCategory.ELECTRONICS, "Mới", 5_000_000.0);
  }

  @AfterEach
  void tearDown() {
    createdIds.forEach(auctionManager::removeAuction);
    scheduler.shutdown();
  }

  private Auction create(double price, long duration) {
    Auction a = auctionService.createAuction(seller, item, price, duration);
    createdIds.add(a.getAuctionId());
    return a;
  }

  // ──────── createAuction ────────

  @Nested
  @DisplayName("createAuction (EP + BVA)")
  class CreateAuction {

    @Test
    @DisplayName("EP-Valid: tạo auction trả PENDING với đúng thông tin")
    void createAuction_valid_returnsPending() {
      Auction a = create(2000.0, 3600);
      assertNotNull(a);
      assertEquals(AuctionStatus.PENDING, a.getStatus());
      assertEquals(2000.0, a.getStartingPrice());
      assertEquals(3600L, a.getDurationSeconds());
      assertNotNull(a.getAuctionId());
    }

    @Test
    @DisplayName("EP-Valid: auction được lưu vào AuctionManager")
    void createAuction_valid_storedInManager() {
      Auction a = create(1000.0, 3600);
      assertNotNull(auctionManager.findAuction(a.getAuctionId()));
    }

    @Test
    @DisplayName("BVA-Boundary: startingPrice = 0 hợp lệ")
    void createAuction_zeroPriceIsValid() {
      Auction a = create(0.0, 3600);
      assertEquals(0.0, a.getStartingPrice());
    }

    @Test
    @DisplayName("EP-Invalid: seller null ném AuctionException")
    void createAuction_nullSeller_throws() {
      assertThrows(AuctionException.class,
          () -> auctionService.createAuction(null, item, 1000.0, 3600));
    }

    @Test
    @DisplayName("EP-Invalid: seller không có quyền ném UnauthorizedException")
    void createAuction_sellerCantCreate_throws() {
      Seller noRight = new Seller("S-NO", "No Right", "noright@test.com") {
        @Override
        public boolean canCreateAuction() {
          return false;
        }
      };
      assertThrows(UnauthorizedException.class,
          () -> auctionService.createAuction(noRight, item, 1000.0, 3600));
    }
  }

  // ──────── startAuction ────────

  @Nested
  @DisplayName("startAuction (EP)")
  class StartAuction {

    @Test
    @DisplayName("EP-Valid: startAuction chuyển PENDING → ACTIVE")
    void startAuction_fromPending_becomesActive() {
      Auction a = create(1000.0, 3600);
      Auction started = auctionService.startAuction(a.getAuctionId());
      assertEquals(AuctionStatus.ACTIVE, started.getStatus());
      assertNotNull(started.getStartTime());
      assertNotNull(started.getScheduledEndTime());
    }

    @Test
    @DisplayName("EP-Invalid: auctionId không tồn tại ném AuctionClosedException")
    void startAuction_notFound_throws() {
      assertThrows(AuctionClosedException.class,
          () -> auctionService.startAuction("GHOST-ID"));
    }

    @Test
    @DisplayName("EP-Invalid: startAuction từ ACTIVE ném IllegalStateException")
    void startAuction_alreadyActive_throws() {
      Auction a = create(1000.0, 3600);
      auctionService.startAuction(a.getAuctionId());
      assertThrows(IllegalStateException.class,
          () -> auctionService.startAuction(a.getAuctionId()));
    }
  }

  // ──────── finishAuction ────────

  @Nested
  @DisplayName("finishAuction (EP)")
  class FinishAuction {

    @Test
    @DisplayName("EP-Valid: finishAuction từ ACTIVE thành FINISHED")
    void finishAuction_fromActive_becomesFinished() {
      Auction a = create(1000.0, 3600);
      auctionService.startAuction(a.getAuctionId());
      Auction finished = auctionService.finishAuction(a.getAuctionId());
      assertEquals(AuctionStatus.FINISHED, finished.getStatus());
      assertNotNull(finished.getEndTime());
    }

    @Test
    @DisplayName("EP-Invalid: finishAuction từ PENDING ném IllegalStateException")
    void finishAuction_fromPending_throws() {
      Auction a = create(1000.0, 3600);
      assertThrows(IllegalStateException.class,
          () -> auctionService.finishAuction(a.getAuctionId()));
    }

    @Test
    @DisplayName("EP-Invalid: auctionId không tồn tại ném AuctionClosedException")
    void finishAuction_notFound_throws() {
      assertThrows(AuctionClosedException.class,
          () -> auctionService.finishAuction("GHOST-ID"));
    }
  }

  // ──────── cancelAuction ────────

  @Nested
  @DisplayName("cancelAuction (EP)")
  class CancelAuction {

    @Test
    @DisplayName("EP-Valid: cancelAuction từ PENDING thành CANCELLED")
    void cancelAuction_fromPending_becomesCancelled() {
      Auction a = create(1000.0, 3600);
      Auction cancelled = auctionService.cancelAuction(a.getAuctionId());
      assertEquals(AuctionStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("EP-Valid: cancelAuction từ ACTIVE thành CANCELLED")
    void cancelAuction_fromActive_becomesCancelled() {
      Auction a = create(1000.0, 3600);
      auctionService.startAuction(a.getAuctionId());
      Auction cancelled = auctionService.cancelAuction(a.getAuctionId());
      assertEquals(AuctionStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("EP-Invalid: cancelAuction từ FINISHED ném IllegalStateException")
    void cancelAuction_fromFinished_throws() {
      Auction a = create(1000.0, 3600);
      auctionService.startAuction(a.getAuctionId());
      auctionService.finishAuction(a.getAuctionId());
      assertThrows(IllegalStateException.class,
          () -> auctionService.cancelAuction(a.getAuctionId()));
    }

    @Test
    @DisplayName("EP-Invalid: auctionId không tồn tại ném AuctionClosedException")
    void cancelAuction_notFound_throws() {
      assertThrows(AuctionClosedException.class,
          () -> auctionService.cancelAuction("GHOST-ID"));
    }
  }

  // ──────── updateAuction ────────

  @Nested
  @DisplayName("updateAuction (EP)")
  class UpdateAuction {

    @Test
    @DisplayName("EP-Valid: updateAuction thay đổi đúng thông tin")
    void updateAuction_valid_updatesFields() {
      Auction a = create(1000.0, 3600);
      Auction updated = auctionService.updateAuction(
          a.getAuctionId(),
          "New Name", "New Desc", ItemCategory.ART,
          "Đã qua sử dụng", 8_000_000.0, 2000.0, 7200);
      assertEquals(2000.0, updated.getStartingPrice());
      assertEquals(7200L, updated.getDurationSeconds());
      assertEquals("New Name", updated.getItem().getName());
      assertEquals("New Desc", updated.getItem().getDescription());
      assertEquals(ItemCategory.ART, updated.getItem().getCategory());
    }

    @Test
    @DisplayName("EP-Invalid: auctionId không tồn tại ném AuctionClosedException")
    void updateAuction_notFound_throws() {
      assertThrows(AuctionClosedException.class,
          () -> auctionService.updateAuction("GHOST-ID", "N", "D",
              ItemCategory.BOOK, "OK", 1000.0, 500.0, 3600));
    }
  }

  // ──────── getAuctionManager ────────

  @Test
  @DisplayName("getAuctionManager trả về non-null")
  void getAuctionManager_returnsNonNull() {
    assertNotNull(auctionService.getAuctionManager());
  }
}
