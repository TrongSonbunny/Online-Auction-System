package com.auction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.auction.backend.Auction;
import com.auction.backend.AuctionItem;
import com.auction.backend.AuctionObserver;
import com.auction.backend.BankPayments;
import com.auction.backend.BidTransaction;
import com.auction.backend.Bidder;
import com.auction.backend.MomoPayments;
import com.auction.backend.Seller;
import com.auction.backend.VnPayPayments;

/**
 * Test dam bao dau gia dong thoi (Multi-threading) an toan - JUnit 5.
 * tang, khong bi rollback 3. Two Winners - Chi 1 nguoi thang sau endAuction 4. Bid sau dong - Phai
 * bi tu choi hoan toan.
 *
 * <p>Cong cu: CountDownLatch - Bat tat ca thread bat dau CUNG LUC (tao ap luc toi da)
 * ExecutorService - Quan ly pool thread AtomicInteger - Dem an toan (thread-safe).
 */
@DisplayName("Concurrent Bidding - Multi-threading Safety")
class ConcurrentBiddingTest {

  private Auction auction;
  private Seller seller;

  private static final int THREAD_COUNT = 50;
  private static final int TIMEOUT_SECONDS = 10;

  @BeforeEach
  void setUp() {
    seller = new Seller("SELLER-1", "Người Bán", "s@t.com", "hash");

    AuctionItem item =
        seller.createItem(
            "electronics",
            "ITEM-CONCURRENT",
            "Sản phẩm test concurrent",
            "Mô tả",
            0.0,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(10));

    auction =
        new Auction(
            "AUC-CONCURRENT",
            item,
            seller.getUserId(),
            0.0,
            LocalDateTime.now().plusMinutes(10));

    auction.startAuction();
  }

  // =========================================================================
  // TEST 1: Khong Lost Update
  // =========================================================================
  @Test
  @DisplayName("CONCURRENT-01: 50 thread -> lich su bid khong bi mat (no lost update)")
  void concurrent_Ep_khongLostUpdate_lichSuKhongBiMat() throws InterruptedException {

    ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch endGate = new CountDownLatch(THREAD_COUNT);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    for (int i = 0; i < THREAD_COUNT; i++) {
      final double bidAmount = (i + 1) * 10.0; // 10, 20, ..., 500
      final Bidder bidder =
          new Bidder(
              "BIDDER-" + i, "Bidder " + i, "b" + i + "@t.com", "hash", new VnPayPayments());

      pool.submit(
          () -> {
            try {
              startGate.await(); // Cho lenh bat dau - tat ca cung xuat phat
              auction.placeBid(bidder, bidAmount);
              successCount.incrementAndGet();
            } catch (Exception e) {
              failCount.incrementAndGet();
            } finally {
              endGate.countDown();
            }
          });
    }

    startGate.countDown(); // BAT DAU - tat ca thread cung luc!
    boolean done = endGate.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    pool.shutdown();

    assertTrue(done, "He thong bi treo (timeout " + TIMEOUT_SECONDS + "s)!");

    int historySize = auction.getBidHistory().size();
    assertEquals(
        successCount.get(),
        historySize,
        "BidHistory (" + historySize + ") phai bang so lan thanh cong (" 
        + successCount.get() + ")");
    assertEquals(
        THREAD_COUNT,
        successCount.get() + failCount.get(),
        "Tong success + fail phai = " + THREAD_COUNT);

    System.out.println(
        "[TEST-01] Success="
            + successCount.get()
            + " Fail="
            + failCount.get()
            + " History="
            + historySize);
  }

  // =========================================================================
  // TEST 2: Gia chi tang, khong bao gio giam
  // =========================================================================
  @Test
  @DisplayName("CONCURRENT-02: Gia trong history chi tang, khong bi rollback (no race condition)")
  void concurrent_Ep_giaChiTang_khongBiRollback() throws InterruptedException {

    ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch endGate = new CountDownLatch(THREAD_COUNT);

    for (int i = 0; i < THREAD_COUNT; i++) {
      final double bidAmount = (i + 1) * 7.0; // Gia khong trung nhau
      final Bidder bidder =
          new Bidder(
              "BIDDER-" + i, "Bidder " + i, "b" + i + "@t.com", "hash", new VnPayPayments());

      pool.submit(
          () -> {
            try {
              startGate.await();
              auction.placeBid(bidder, bidAmount);
            } catch (Exception ignored) {
              // Ignored for testing race condition
            } finally {
              endGate.countDown();
            }
          });
    }

    startGate.countDown();
    boolean done = endGate.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    pool.shutdown();

    assertTrue(done, "Timeout!");

    // Gia trong history PHAI TANG DAN
    List<BidTransaction> history = auction.getBidHistory();
    for (int i = 1; i < history.size(); i++) {
      double prev = history.get(i - 1).getBidAmount();
      double curr = history.get(i).getBidAmount();
      assertTrue(
          curr > prev,
          "GIA BI ROLLBACK! Bid[" + i + "]=" + curr + " <= Bid[" + (i - 1) + "]=" + prev);
    }

    System.out.println(
        "[TEST-02] Gia cuoi="
            + auction.getCurrentHighestBid()
            + " So bid hop le="
            + history.size());
  }

  // =========================================================================
  // TEST 3: Chi 1 nguoi thang
  // =========================================================================
  @Test
  @DisplayName("CONCURRENT-03: 50 thread cung luc -> chi dung 1 nguoi thang (no two winners)")
  void concurrent_Ep_chiMotNguoiThang() throws InterruptedException {

    ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch endGate = new CountDownLatch(THREAD_COUNT);

    for (int i = 0; i < THREAD_COUNT; i++) {
      final double bidAmount = 1000.0 + i; // 1000, 1001, ..., 1049
      final Bidder bidder =
          new Bidder("BIDDER-" + i, "Bidder " + i, "b" + i + "@t.com", "hash", new BankPayments());

      pool.submit(
          () -> {
            try {
              startGate.await();
              auction.placeBid(bidder, bidAmount);
            } catch (Exception ignored) {
              // Ignored
            } finally {
              endGate.countDown();
            }
          });
    }

    startGate.countDown();
    boolean done = endGate.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    pool.shutdown();

    assertTrue(done, "Timeout!");
    auction.endAuction();

    String winnerId = auction.getCurrentLeaderId();
    double winPrice = auction.getCurrentHighestBid();

    assertNotNull(winnerId, "Phai co nguoi thang");
    assertTrue(winPrice >= 1000.0, "Gia thang phai >= 1000");

    List<BidTransaction> history = auction.getBidHistory();
    BidTransaction lastBid = history.get(history.size() - 1);
    assertEquals(
        lastBid.getBidder().getUserId(),
        winnerId,
        "Nguoi thang phai la nguoi co bid cuoi trong history");

    System.out.println("[TEST-03] Winner=" + winnerId + " Price=" + winPrice);
  }

  // =========================================================================
  // TEST 4: Bid sau khi dong phien -> tat ca phai bi tu choi
  // =========================================================================
  @Test
  @DisplayName("CONCURRENT-04 BVA: Bid sau endAuction -> tat ca phai throw exception")
  void concurrent_Bva_bidSauDongPhien_tatCaThatBai() throws InterruptedException {

    auction.placeBid(new Bidder("B0", "B0", "b0@t.com", "h", new MomoPayments()), 500.0);
    auction.endAuction();

    int threadCount = 20;
    ExecutorService pool = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch endGate = new CountDownLatch(threadCount);
    AtomicInteger exceptionCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      final Bidder lateBidder =
          new Bidder("LATE-" + i, "Late " + i, "l" + i + "@t.com", "hash", new MomoPayments());

      pool.submit(
          () -> {
            try {
              startGate.await();
              auction.placeBid(lateBidder, 99999.0);
            } catch (IllegalStateException | IllegalArgumentException e) {
              exceptionCount.incrementAndGet();
            } catch (Exception e) {
              exceptionCount.incrementAndGet();
            } finally {
              endGate.countDown();
            }
          });
    }

    startGate.countDown();
    boolean done = endGate.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    pool.shutdown();

    assertTrue(done, "Timeout!");
    assertEquals(
        threadCount,
        exceptionCount.get(),
        "Tat ca " + threadCount + " thread phai bi tu choi");
  }

  // =========================================================================
  // TEST 5: Stress Test 100 thread
  // =========================================================================
  @Test
  @DisplayName("CONCURRENT-05 [STRESS]: 100 thread dong thoi -> he thong khong bi treo/deadlock")
  @Timeout(15)
  void concurrent_Ep_stressTest_khongBiTreo() throws InterruptedException {

    int threadCount = 100;
    ExecutorService pool = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch endGate = new CountDownLatch(threadCount);
    AtomicInteger totalProcessed = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      final double bidAmount = (i + 1) * 3.0;
      final Bidder bidder =
          new Bidder("STRESS-" + i, "Stress " + i, "s" + i + "@t.com", "hash", new MomoPayments());

      pool.submit(
          () -> {
            try {
              startGate.await();
              auction.placeBid(bidder, bidAmount);
            } catch (Exception ignored) {
              // Ignored
            } finally {
              totalProcessed.incrementAndGet();
              endGate.countDown();
            }
          });
    }

    startGate.countDown();
    boolean done = endGate.await(15, TimeUnit.SECONDS);
    pool.shutdown();

    assertTrue(done, "He thong bi treo! Khong xu ly xong trong 15 giay");
    assertEquals(
        threadCount, totalProcessed.get(), "Phai xu ly du " + threadCount + " request");
  }

  // =========================================================================
  // TEST 6: Observer thread-safe
  // =========================================================================
  @Test
  @DisplayName("CONCURRENT-06: Them/xoa Observer trong luc notify" 
      + "khong ConcurrentModificationException")
  void concurrent_Ep_observerThreadSafe_khongConcurrentModificationException()
      throws InterruptedException {

    int observerCount = 20;
    List<AuctionObserver> observers = new ArrayList<>();

    for (int i = 0; i < observerCount; i++) {
      AuctionObserver obs =
          event -> {
            try {
              Thread.sleep(1);
            } catch (InterruptedException ignored) {
              Thread.currentThread().interrupt();
            }
          };
      observers.add(obs);
      auction.registerObserver(obs);
    }

    ExecutorService pool = Executors.newFixedThreadPool(10);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch endGate = new CountDownLatch(3);
    AtomicInteger errorCount = new AtomicInteger(0);

    // Thread 1: Lien tuc dat gia
    pool.submit(
        () -> {
          try {
            startGate.await();
            for (int i = 1; i <= 10; i++) {
              try {
                auction.placeBid(
                    new Bidder("BID-" + i, "B", "b@t.com", "h", new MomoPayments()), i * 100.0);
                Thread.sleep(5);
              } catch (Exception ignored) {
                // Ignored
              }
            }
          } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
          } finally {
            endGate.countDown();
          }
        });

    // Thread 2: Lien tuc them observer moi
    pool.submit(
        () -> {
          try {
            startGate.await();
            for (int i = 0; i < 10; i++) {
              auction.registerObserver(event -> {});
              Thread.sleep(3);
            }
          } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
          } finally {
            endGate.countDown();
          }
        });

    // Thread 3: Lien tuc xoa observer cu
    pool.submit(
        () -> {
          try {
            startGate.await();
            for (AuctionObserver obs : observers) {
              auction.removeObserver(obs);
              Thread.sleep(2);
            }
          } catch (Exception e) {
            errorCount.incrementAndGet();
          } finally {
            endGate.countDown();
          }
        });

    startGate.countDown();
    boolean done = endGate.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    pool.shutdown();

    assertTrue(done, "Timeout!");
    assertEquals(0, errorCount.get(), "Khong duoc co loi ConcurrentModificationException");
  }
}