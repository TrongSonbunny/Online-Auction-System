package com.auction.backend.bid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.BidException;
import com.auction.models.bid.BidTransaction;
import com.auction.models.payment.BankPayment;
import com.auction.models.user.Bidder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho BidHistoryManager.
 *
 * <p>EP: thêm transaction hợp lệ / null; getTotalBids khi rỗng / có phần tử
 * BVA: 0 transaction, 1 transaction, nhiều transaction; clearHistory
 */
@DisplayName("BidHistoryManager Tests")
class BidHistoryManagerTest {

  private BidHistoryManager manager;
  private Bidder bidder;

  @BeforeEach
  void setUp() {
    manager = new BidHistoryManager();
    bidder = new Bidder("B-001", "Bidder", "b@test.com",
        new BankPayment("VCB", "123456", "A"));
  }

  private BidTransaction createTx(String txId, double amount) {
    return new BidTransaction(txId, bidder, "AUC-001", amount);
  }

  // ──────── addTransaction ────────

  @Nested
  @DisplayName("addTransaction (EP + BVA)")
  class AddTransaction {

    @Test
    @DisplayName("EP-Valid: thêm 1 transaction thành công")
    void addTransaction_valid_stored() {
      manager.addTransaction(createTx("T-001", 1000.0));
      assertEquals(1, manager.getTotalBids());
    }

    @Test
    @DisplayName("EP-Invalid: thêm null ném BidException")
    void addTransaction_null_throwsBidException() {
      assertThrows(BidException.class, () -> manager.addTransaction(null));
    }

    @Test
    @DisplayName("BVA-Boundary: thêm nhiều transaction đếm đúng")
    void addTransaction_multiple_countsCorrectly() {
      manager.addTransaction(createTx("T-001", 1000.0));
      manager.addTransaction(createTx("T-002", 2000.0));
      manager.addTransaction(createTx("T-003", 3000.0));
      assertEquals(3, manager.getTotalBids());
    }
  }

  // ──────── getBidHistory ────────

  @Nested
  @DisplayName("getBidHistory (EP + BVA)")
  class GetBidHistory {

    @Test
    @DisplayName("BVA-Boundary: khi rỗng trả list rỗng")
    void getBidHistory_empty_returnsEmptyList() {
      assertTrue(manager.getBidHistory().isEmpty());
    }

    @Test
    @DisplayName("EP-Valid: chứa transaction vừa thêm")
    void getBidHistory_withTransactions_containsThem() {
      BidTransaction tx = createTx("T-001", 1500.0);
      manager.addTransaction(tx);
      assertTrue(manager.getBidHistory().contains(tx));
    }

    @Test
    @DisplayName("EP-Valid: trả unmodifiable list")
    void getBidHistory_returnsUnmodifiableList() {
      manager.addTransaction(createTx("T-001", 1000.0));
      assertThrows(UnsupportedOperationException.class,
          () -> manager.getBidHistory().clear());
    }
  }

  // ──────── getTotalBids ────────

  @Nested
  @DisplayName("getTotalBids (EP + BVA)")
  class GetTotalBids {

    @Test
    @DisplayName("BVA-Boundary: khi rỗng = 0")
    void getTotalBids_empty_isZero() {
      assertEquals(0, manager.getTotalBids());
    }

    @Test
    @DisplayName("BVA-Boundary: sau 1 thêm = 1")
    void getTotalBids_afterOneAdd_isOne() {
      manager.addTransaction(createTx("T-001", 100.0));
      assertEquals(1, manager.getTotalBids());
    }

    @Test
    @DisplayName("EP-Valid: sau 5 thêm = 5")
    void getTotalBids_afterFiveAdds_isFive() {
      for (int i = 1; i <= 5; i++) {
        manager.addTransaction(createTx("T-00" + i, i * 100.0));
      }
      assertEquals(5, manager.getTotalBids());
    }
  }

  // ──────── clearHistory ────────

  @Nested
  @DisplayName("clearHistory (EP + BVA)")
  class ClearHistory {

    @Test
    @DisplayName("EP-Valid: clearHistory xóa tất cả")
    void clearHistory_withData_becomesEmpty() {
      manager.addTransaction(createTx("T-001", 100.0));
      manager.addTransaction(createTx("T-002", 200.0));
      manager.clearHistory();
      assertEquals(0, manager.getTotalBids());
      assertTrue(manager.getBidHistory().isEmpty());
    }

    @Test
    @DisplayName("BVA-Boundary: clearHistory khi đã rỗng không ném exception")
    void clearHistory_alreadyEmpty_doesNotThrow() {
      manager.clearHistory();
      assertEquals(0, manager.getTotalBids());
    }
  }
}
