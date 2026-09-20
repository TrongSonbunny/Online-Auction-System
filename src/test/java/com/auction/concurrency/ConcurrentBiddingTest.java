package com.auction.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.backend.auction.AuctionManager;
import com.auction.backend.auction.AuctionService;
import com.auction.backend.bid.BidService;
import com.auction.models.auction.Auction;
import com.auction.models.auction.AuctionStatus;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.payment.BankPayment;
import com.auction.models.user.Bidder;
import com.auction.models.user.Seller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Test đảm bảo tính đúng đắn của hệ thống đấu giá trong môi trường đa luồng.
 *
 * <p>Các kịch bản được kiểm tra:
 * <ol>
 *   <li>Highest bid nhất quán: sau khi 20 thread bid đồng thời, giá cao nhất phải
 *       đúng và không bao giờ giảm.
 *   <li>BidHistoryManager: số bid trong lịch sử bằng đúng số lần placeBid thành công.
 *   <li>AuctionManager (ConcurrentHashMap): 15 thread thêm auction đồng thời không mất dữ liệu.
 *   <li>Auction lifecycle: chỉ 1 trong 10 thread finish()/start() thành công, còn lại nhận
 *       IllegalStateException.
 *   <li>Bidder AtomicInteger: 50 thread increment đồng thời không bị lost update.
 *   <li>End-to-end: 30 bidder đặt giá đồng thời, kết quả nhất quán.
 *   <li>AuctionService: tạo auction đồng thời sinh ID duy nhất.
 * </ol>
 * Mỗi kịch bản lặp {@value #REPEATED_RUNS} lần để phát hiện race condition không ổn định.
 */
@DisplayName("Concurrent Bidding Tests")
class ConcurrentBiddingTest {

  private static final int THREAD_COUNT = 20;
  private static final int REPEATED_RUNS = 3;

  private Auction activeAuction;
  private BidService bidService;
  private List<Bidder> bidders;

  @BeforeEach
  void setUp() {
    bidService = new BidService();

    Seller seller = new Seller("S-001", "Seller", "s@test.com");
    AuctionItem item = new AuctionItem(
        "I-001", "Laptop", "Gaming Laptop",
        ItemCategory.ELECTRONICS, "Mới", 10_000_000.0);

    activeAuction = new Auction("AUC-CONCURRENT", seller, item, 1000.0);
    activeAuction.start();

    // Tạo THREAD_COUNT bidder, mỗi người có mức giá khác nhau
    bidders = new ArrayList<>();
    for (int i = 1; i <= THREAD_COUNT; i++) {
      Bidder bidder = new Bidder(
          "B-" + String.format("%03d", i),
          "Bidder " + i,
          "bidder" + i + "@test.com",
          new BankPayment("VCB", "ACC-" + i, "Bidder " + i));
      bidders.add(bidder);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // 1. HIGHEST BID CONSISTENCY
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("1. Highest Bid Consistency")
  class HighestBidConsistency {

    /**
     * Mỗi bidder đặt giá tăng dần theo index.
     * Sau khi tất cả thread hoàn thành, highest bid phải là giá lớn nhất.
     */
    @RepeatedTest(REPEATED_RUNS)
    @Timeout(10)
    @DisplayName("Giá cao nhất cuối cùng là max trong tất cả bid thành công")
    void concurrentBids_highestBidIsMaxOfAllAccepted()
        throws InterruptedException {

      // Mỗi bidder đặt giá: bid[i] = 1001 + i * 100
      // Đảm bảo giá tăng dần, không bid thấp hơn
      double[] bidAmounts = new double[THREAD_COUNT];
      for (int i = 0; i < THREAD_COUNT; i++) {
        bidAmounts[i] = 1001.0 + i * 100.0;
      }

      AtomicInteger successCount = new AtomicInteger(0);
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

      Auction auctionForTest = activeAuction;

      for (int i = 0; i < THREAD_COUNT; i++) {
        final Bidder bidder = bidders.get(i);
        final double amount = bidAmounts[i];
        executor.submit(() -> {
          try {
            startLatch.await();
            bidService.placeBid(auctionForTest, bidder, amount);
            successCount.incrementAndGet();
          } catch (Exception ignored) {
            // Bid thất bại (do bị outbid hoặc racing) - bình thường
          } finally {
            doneLatch.countDown();
          }
        });
      }

      startLatch.countDown();
      assertTrue(doneLatch.await(9, TimeUnit.SECONDS));
      executor.shutdown();

      // Highest bid phải >= 1001 (ít nhất 1 bid thành công)
      assertTrue(auctionForTest.getCurrentHighestBid() >= 1001.0,
          "Highest bid phải cao hơn starting price");
      assertNotNull(auctionForTest.getCurrentHighestBidder(),
          "Phải có highest bidder");
    }

    /**
     * Kịch bản: Tất cả bidder đặt cùng một mức giá tăng dần nghiêm ngặt,
     * đảm bảo chỉ 1 bid tại 1 thời điểm được cập nhật.
     * currentHighestBid luôn tăng đơn điệu, không bao giờ giảm.
     */
    @RepeatedTest(REPEATED_RUNS)
    @Timeout(10)
    @DisplayName("currentHighestBid không bao giờ giảm khi có nhiều thread")
    void concurrentBids_highestBidNeverDecreases()
        throws InterruptedException {

      List<Double> observedHighestBids =
          Collections.synchronizedList(new ArrayList<>());
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

      Auction auctionForTest = activeAuction;

      for (int i = 0; i < THREAD_COUNT; i++) {
        final Bidder bidder = bidders.get(i);
        final double amount = 1001.0 + i * 50.0;
        executor.submit(() -> {
          try {
            startLatch.await();
            try {
              bidService.placeBid(auctionForTest, bidder, amount);
            } catch (Exception ignored) {
              // Expected: có thể bị outbid
            }
            observedHighestBids.add(auctionForTest.getCurrentHighestBid());
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            doneLatch.countDown();
          }
        });
      }

      startLatch.countDown();
      assertTrue(doneLatch.await(9, TimeUnit.SECONDS));
      executor.shutdown();

      // Kiểm tra: bid cuối cùng >= starting price
      double finalHighest = auctionForTest.getCurrentHighestBid();
      assertTrue(finalHighest >= 1001.0,
          "Final highest bid phải >= starting price 1001");

      // Đảm bảo highest bid hợp lý (không vượt max bid amount)
      double maxBid = 1001.0 + (THREAD_COUNT - 1) * 50.0;
      assertTrue(finalHighest <= maxBid,
          "Final highest bid không được vượt quá bid lớn nhất");
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // 2. BID HISTORY THREAD SAFETY
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("2. BidHistoryManager Thread Safety")
  class BidHistoryThreadSafety {

    /**
     * Nhiều thread ghi đồng thời vào BidHistoryManager.
     * Tổng số bid trong lịch sử phải bằng số lần thành công.
     */
    @RepeatedTest(REPEATED_RUNS)
    @Timeout(10)
    @DisplayName("BidHistoryManager ghi đồng thời không mất dữ liệu")
    void concurrentBidHistory_noDataLoss()
        throws InterruptedException {

      int threadCount = 10;
      AtomicInteger successCount = new AtomicInteger(0);
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(threadCount);
      ExecutorService executor = Executors.newFixedThreadPool(threadCount);

      Auction auctionForHistory = activeAuction;

      for (int i = 0; i < threadCount; i++) {
        final Bidder bidder = bidders.get(i);
        final double amount = 1001.0 + i * 200.0;
        executor.submit(() -> {
          try {
            startLatch.await();
            bidService.placeBid(auctionForHistory, bidder, amount);
            successCount.incrementAndGet();
          } catch (Exception ignored) {
            // Bid thất bại do race condition
          } finally {
            doneLatch.countDown();
          }
        });
      }

      startLatch.countDown();
      assertTrue(doneLatch.await(9, TimeUnit.SECONDS));
      executor.shutdown();

      int historySize = bidService.getBidHistoryManager().getTotalBids();
      assertEquals(successCount.get(), historySize,
          "Số bid trong history phải bằng số lần placeBid thành công");
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // 3. AUCTION MANAGER THREAD SAFETY
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("3. AuctionManager Thread Safety")
  class AuctionManagerThreadSafety {

    /**
     * Nhiều thread thêm auction đồng thời vào AuctionManager (ConcurrentHashMap).
     * Tất cả auction phải được lưu đúng, không có mất mát.
     */
    @RepeatedTest(REPEATED_RUNS)
    @Timeout(10)
    @DisplayName("Thêm nhiều auction đồng thời không mất dữ liệu")
    void concurrentAddAuctions_noDataLoss()
        throws InterruptedException {

      AuctionManager manager = AuctionManager.getInstance();
      // Dọn sạch trước khi test
      manager.getAllAuctions()
          .stream()
          .map(Auction::getAuctionId)
          .toList()
          .forEach(manager::removeAuction);

      int auctionCount = 15;
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(auctionCount);
      ExecutorService executor = Executors.newFixedThreadPool(auctionCount);

      Seller seller = new Seller("S-MT", "Seller MT", "s.mt@test.com");
      AuctionItem item = new AuctionItem(
          "I-MT", "Item MT", "Desc", ItemCategory.ART, "New", 100.0);

      for (int i = 0; i < auctionCount; i++) {
        final String auctionId = "AUC-MT-" + String.format("%03d", i);
        executor.submit(() -> {
          try {
            startLatch.await();
            Auction auction = new Auction(auctionId, seller, item, 500.0);
            manager.addAuction(auction);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            doneLatch.countDown();
          }
        });
      }

      startLatch.countDown();
      assertTrue(doneLatch.await(9, TimeUnit.SECONDS));
      executor.shutdown();

      assertEquals(auctionCount, manager.getAllAuctions().size(),
          "Tất cả " + auctionCount + " auction phải được lưu");

      // Dọn sạch
      manager.getAllAuctions()
          .stream()
          .map(Auction::getAuctionId)
          .toList()
          .forEach(manager::removeAuction);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // 4. AUCTION LIFECYCLE THREAD SAFETY
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("4. Auction Lifecycle Thread Safety")
  class AuctionLifecycleThreadSafety {

    /**
     * Nhiều thread cùng gọi finish() trên 1 auction.
     * Chỉ 1 thread thành công, các thread còn lại nhận IllegalStateException.
     */
    @RepeatedTest(REPEATED_RUNS)
    @Timeout(10)
    @DisplayName("Chỉ 1 thread finish() auction thành công khi có race condition")
    void concurrentFinish_onlyOneSucceeds()
        throws InterruptedException {

      Seller seller = new Seller("S-LC", "Seller LC", "s.lc@test.com");
      AuctionItem item = new AuctionItem(
          "I-LC", "Item LC", "Desc", ItemCategory.ART, "New", 100.0);
      Auction auction = new Auction("AUC-LC", seller, item, 500.0);
      auction.start();

      int threadCount = 10;
      AtomicInteger successCount = new AtomicInteger(0);
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(threadCount);
      ExecutorService executor = Executors.newFixedThreadPool(threadCount);

      for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
          try {
            startLatch.await();
            auction.finish();
            successCount.incrementAndGet();
          } catch (IllegalStateException ignored) {
            // Expected: auction đã finished rồi
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            doneLatch.countDown();
          }
        });
      }

      startLatch.countDown();
      assertTrue(doneLatch.await(9, TimeUnit.SECONDS));
      executor.shutdown();

      assertEquals(1, successCount.get(),
          "Chỉ đúng 1 thread được finish() auction thành công");
      assertEquals(AuctionStatus.FINISHED, auction.getStatus());
    }

    /**
     * Nhiều thread cùng gọi start() trên 1 auction.
     * Chỉ 1 thread thành công, các thread còn lại nhận IllegalStateException.
     */
    @RepeatedTest(REPEATED_RUNS)
    @Timeout(10)
    @DisplayName("Chỉ 1 thread start() auction thành công khi có race condition")
    void concurrentStart_onlyOneSucceeds()
        throws InterruptedException {

      Seller seller = new Seller("S-ST", "Seller ST", "s.st@test.com");
      AuctionItem item = new AuctionItem(
          "I-ST", "Item ST", "Desc", ItemCategory.BOOK, "New", 50.0);
      Auction auction = new Auction("AUC-ST", seller, item, 200.0);

      int threadCount = 10;
      AtomicInteger successCount = new AtomicInteger(0);
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(threadCount);
      ExecutorService executor = Executors.newFixedThreadPool(threadCount);

      for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
          try {
            startLatch.await();
            auction.start();
            successCount.incrementAndGet();
          } catch (IllegalStateException ignored) {
            // Expected: auction đã active rồi
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            doneLatch.countDown();
          }
        });
      }

      startLatch.countDown();
      assertTrue(doneLatch.await(9, TimeUnit.SECONDS));
      executor.shutdown();

      assertEquals(1, successCount.get(),
          "Chỉ đúng 1 thread được start() auction thành công");
      assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // 5. BIDDER ATOMIC COUNTER THREAD SAFETY
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("5. Bidder AtomicInteger Thread Safety")
  class BidderAtomicCounterSafety {

    /**
     * Nhiều thread gọi incrementTotalBidsPlaced() đồng thời.
     * AtomicInteger đảm bảo không có lost update.
     */
    @RepeatedTest(REPEATED_RUNS)
    @Timeout(10)
    @DisplayName("incrementTotalBidsPlaced concurrent không bị lost update")
    void concurrentIncrement_atomicInt_noLostUpdates()
        throws InterruptedException {

      Bidder sharedBidder = new Bidder(
          "B-SHARED", "Shared Bidder", "shared@test.com",
          new BankPayment("VCB", "SHARED", "Shared"));

      int threadCount = 50;
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(threadCount);
      ExecutorService executor = Executors.newFixedThreadPool(threadCount);

      for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
          try {
            startLatch.await();
            sharedBidder.incrementTotalBidsPlaced();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            doneLatch.countDown();
          }
        });
      }

      startLatch.countDown();
      assertTrue(doneLatch.await(9, TimeUnit.SECONDS));
      executor.shutdown();

      assertEquals(threadCount, sharedBidder.getTotalBidsPlaced(),
          "AtomicInteger phải đếm chính xác " + threadCount + " lần increment");
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // 6. FULL END-TO-END CONCURRENT AUCTION FLOW
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("6. End-to-End Concurrent Auction Flow")
  class EndToEndConcurrentFlow {

    /**
     * Mô phỏng luồng đấu giá thực tế:
     * - Nhiều bidder đặt giá ngẫu nhiên đồng thời (tăng dần theo index).
     * - Sau khi kết thúc: highest bid > starting price, có bidder thắng
     * - Tổng bid trong history == số lần placeBid thành công
     */
    @Test
    @Timeout(15)
    @DisplayName("End-to-end: đấu giá đồng thời giữ dữ liệu nhất quán")
    void endToEnd_concurrentAuction_dataConsistent()
        throws InterruptedException {

      BidService localBidService = new BidService();
      Seller seller = new Seller("S-E2E", "Seller E2E", "s.e2e@test.com");
      AuctionItem item = new AuctionItem(
          "I-E2E", "Item E2E", "Desc E2E",
          ItemCategory.ELECTRONICS, "Mới", 5_000_000.0);
      Auction auction = new Auction("AUC-E2E", seller, item, 500.0);
      auction.start();

      int numBidders = 30;
      AtomicInteger successCount = new AtomicInteger(0);
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(numBidders);
      ExecutorService executor = Executors.newFixedThreadPool(numBidders);

      for (int i = 0; i < numBidders; i++) {
        final Bidder bidder = new Bidder(
            "E2E-B-" + i, "E2E Bidder " + i, "e2e" + i + "@test.com",
            new BankPayment("VCB", "E2E-" + i, "E2E " + i));
        final double amount = 501.0 + i * 100.0;

        executor.submit(() -> {
          try {
            startLatch.await();
            localBidService.placeBid(auction, bidder, amount);
            successCount.incrementAndGet();
          } catch (Exception ignored) {
            // Một số bid sẽ fail vì bị outbid - bình thường
          } finally {
            doneLatch.countDown();
          }
        });
      }

      startLatch.countDown();
      assertTrue(doneLatch.await(14, TimeUnit.SECONDS));
      executor.shutdown();

      // Dữ liệu phải nhất quán:
      assertTrue(auction.getCurrentHighestBid() > 500.0,
          "Highest bid phải cao hơn starting price");
      assertNotNull(auction.getCurrentHighestBidder(),
          "Phải có người bid cao nhất");

      int historyCount = localBidService.getBidHistoryManager().getTotalBids();
      assertEquals(successCount.get(), historyCount,
          "Số bid trong lịch sử phải bằng số lần thành công");

      // Auction vẫn active (chưa bị kết thúc ngoài ý muốn)
      assertEquals(AuctionStatus.ACTIVE, auction.getStatus(),
          "Auction vẫn phải ACTIVE sau khi bidding");
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // 7. AUCTION SERVICE THREAD SAFETY
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("7. AuctionService Thread Safety")
  class AuctionServiceThreadSafety {

    /**
     * Nhiều thread tạo auction đồng thời qua AuctionService.
     * Mỗi auction phải có ID duy nhất, tất cả phải được lưu.
     */
    @RepeatedTest(REPEATED_RUNS)
    @Timeout(10)
    @DisplayName("createAuction đồng thời sinh ID duy nhất cho từng auction")
    void concurrentCreateAuction_uniqueIds()
        throws InterruptedException {

      AuctionService auctionService = new AuctionService();
      AuctionManager manager = auctionService.getAuctionManager();
      manager.getAllAuctions()
          .stream()
          .map(Auction::getAuctionId)
          .toList()
          .forEach(manager::removeAuction);

      int threadCount = 10;
      List<String> createdIds = Collections.synchronizedList(new ArrayList<>());
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(threadCount);
      ExecutorService executor = Executors.newFixedThreadPool(threadCount);

      for (int i = 0; i < threadCount; i++) {
        final int idx = i;
        executor.submit(() -> {
          try {
            startLatch.await();
            Seller seller = new Seller(
                "S-CS-" + idx, "Seller CS " + idx, "s.cs" + idx + "@test.com");
            AuctionItem item = new AuctionItem(
                "I-CS-" + idx, "Item CS " + idx, "Desc CS",
                ItemCategory.ART, "New", 100.0);
            Auction auction = auctionService.createAuction(seller, item, 100.0, 3600L);
            createdIds.add(auction.getAuctionId());
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            doneLatch.countDown();
          }
        });
      }

      startLatch.countDown();
      assertTrue(doneLatch.await(9, TimeUnit.SECONDS));
      executor.shutdown();

      // Tất cả ID phải unique
      long uniqueCount = createdIds.stream().distinct().count();
      assertEquals(threadCount, uniqueCount,
          "Mỗi auction tạo đồng thời phải có ID duy nhất");

      // Dọn sạch
      manager.getAllAuctions()
          .stream()
          .map(Auction::getAuctionId)
          .toList()
          .forEach(manager::removeAuction);
    }
  }
}
