package com.auction;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.auction.backend.Admin;
import com.auction.backend.AuctionStatus;
import com.auction.backend.BidTransaction;
import com.auction.backend.Bidder;
import com.auction.backend.Seller;
import com.auction.backend.User;

/**
 * Unit Test toan bo backend: AuctionStatus, BidTransaction, User - JUnit 5.
 * EP  = Equivalence Partitioning
 * BVA = Boundary Value Analysis
 */
@DisplayName("Backend Unit Tests")
class BackendUnitTest {

  private Bidder bidder;
  private Seller seller;
  private Admin admin;

  @BeforeEach
  void setUp() {
    bidder = new Bidder("BIDDER-1", "Nguyen Van A", "a@test.com", "hash123");
    seller = new Seller("SELLER-1", "Tran Thi B", "b@test.com", "hash456");
    admin = new Admin("ADMIN-1", "Le Van C", "c@test.com", "hash789");
  }

  // =========================================================================
  // A. AuctionStatus — Chuyen trang thai
  // =========================================================================
  @Nested
  @DisplayName("A. AuctionStatus - Chuyen trang thai")
  class AuctionStatusTest {

    @Test
    @DisplayName("EP: OPEN co the chuyen sang RUNNING")
    void openToRunning_EP_hanhTri() {
      assertTrue(AuctionStatus.OPEN.canTransitionTo(AuctionStatus.RUNNING));
    }

    @Test
    @DisplayName("EP: OPEN co the chuyen sang CANCELED")
    void openToCanceled_EP_hanhTri() {
      assertTrue(AuctionStatus.OPEN.canTransitionTo(AuctionStatus.CANCELED));
    }

    @Test
    @DisplayName("EP: RUNNING co the chuyen sang FINISHED")
    void runningToFinished_EP_hanhTri() {
      assertTrue(AuctionStatus.RUNNING.canTransitionTo(AuctionStatus.FINISHED));
    }

    @Test
    @DisplayName("EP: RUNNING co the chuyen sang CANCELED")
    void runningToCanceled_EP_hanhTri() {
      assertTrue(AuctionStatus.RUNNING.canTransitionTo(AuctionStatus.CANCELED));
    }

    @Test
    @DisplayName("EP: FINISHED co the chuyen sang PAID")
    void finishedToPaid_EP_hanhTri() {
      assertTrue(AuctionStatus.FINISHED.canTransitionTo(AuctionStatus.PAID));
    }

    @Test
    @DisplayName("EP: FINISHED co the chuyen sang CANCELED")
    void finishedToCanceled_EP_hanhTri() {
      assertTrue(AuctionStatus.FINISHED.canTransitionTo(AuctionStatus.CANCELED));
    }

    @Test
    @DisplayName("EP: OPEN KHONG the chuyen thang sang FINISHED")
    void openToFinished_EP_khongHanhTri() {
      assertFalse(AuctionStatus.OPEN.canTransitionTo(AuctionStatus.FINISHED));
    }

    @Test
    @DisplayName("EP: OPEN KHONG the chuyen sang PAID")
    void openToPaid_EP_khongHanhTri() {
      assertFalse(AuctionStatus.OPEN.canTransitionTo(AuctionStatus.PAID));
    }

    @Test
    @DisplayName("EP: RUNNING KHONG the quay lai OPEN")
    void runningToOpen_EP_khongHanhTri() {
      assertFalse(AuctionStatus.RUNNING.canTransitionTo(AuctionStatus.OPEN));
    }

    @Test
    @DisplayName("BVA trang thai cuoi: PAID khong the chuyen sang bat ky trang thai nao")
    void paid_BVA_trangThaiCuoi_khongChuyen() {
      for (AuctionStatus s : AuctionStatus.values()) {
        assertFalse(AuctionStatus.PAID.canTransitionTo(s),
            "PAID khong duoc chuyen sang " + s);
      }
    }

    @Test
    @DisplayName("BVA trang thai cuoi: CANCELED khong the chuyen sang bat ky trang thai nao")
    void canceled_BVA_trangThaiCuoi_khongChuyen() {
      for (AuctionStatus s : AuctionStatus.values()) {
        assertFalse(AuctionStatus.CANCELED.canTransitionTo(s),
            "CANCELED khong duoc chuyen sang " + s);
      }
    }
  }

  // =========================================================================
  // B. BidTransaction — Tao giao dich
  // =========================================================================
  @Nested
  @DisplayName("B. BidTransaction - Tao giao dich")
  class BidTransactionTest {

    @Test
    @DisplayName("EP: Tao voi du lieu day du -> ID tu sinh, getter dung")
    void create_EP_duLieuDayDu_IDTuSinhVaGetterDung() {
      BidTransaction tx = new BidTransaction(bidder, 500.0, "AUC-001");

      assertAll(
          () -> assertNotNull(tx.getTransactionId()),
          () -> assertFalse(tx.getTransactionId().isEmpty()),
          () -> assertEquals(500.0, tx.getBidAmount(), 0.001),
          () -> assertEquals("AUC-001", tx.getAuctionId()),
          () -> assertEquals(bidder, tx.getBidder()),
          () -> assertNotNull(tx.getTimestamp())
      );
    }

    @Test
    @DisplayName("BVA: bidAmount = 0.01 (nho nhat thuc te) -> duoc tao binh thuong")
    void create_BVA_bidAmountNhoNhat_001() {
      BidTransaction tx = new BidTransaction(bidder, 0.01, "AUC-001");
      assertEquals(0.01, tx.getBidAmount(), 0.001);
    }

    @Test
    @DisplayName("BVA: bidAmount = Double.MAX_VALUE -> khong overflow")
    void create_BVA_bidAmountMaxDouble_khongOverflow() {
      BidTransaction tx = new BidTransaction(bidder, Double.MAX_VALUE, "AUC-001");
      assertEquals(Double.MAX_VALUE, tx.getBidAmount(), 0.0);
    }

    @Test
    @DisplayName("EP: Hai BidTransaction -> transactionId khac nhau")
    void create_EP_haiGiaoDich_IDKhacNhau() {
      BidTransaction tx1 = new BidTransaction(bidder, 100.0, "AUC-001");
      BidTransaction tx2 = new BidTransaction(bidder, 200.0, "AUC-001");
      assertNotEquals(tx1.getTransactionId(), tx2.getTransactionId());
    }

    @Test
    @DisplayName("EP: getTransactionDetails() chua day du thong tin")
    void getDetails_EP_chuaDuThongTin() {
      BidTransaction tx = new BidTransaction(bidder, 300.0, "AUC-002");
      String details = tx.getTransactionDetails();

      assertAll(
          () -> assertTrue(details.contains("AUC-002")),
          () -> assertTrue(details.contains("300.0")),
          () -> assertTrue(details.contains("Nguyen Van A"))
      );
    }
  }

  // =========================================================================
  // C. User — Phan cap ke thua
  // =========================================================================
  @Nested
  @DisplayName("C. User - Phan cap ke thua")
  class UserTest {

    @Test
    @DisplayName("EP: Bidder/Seller/Admin tra dung getRole()")
    void getRole_EP_tatCaRoleTraDung() {
      assertAll(
          () -> assertEquals("BIDDER", bidder.getRole()),
          () -> assertEquals("SELLER", seller.getRole()),
          () -> assertEquals("ADMIN", admin.getRole())
      );
    }

    @Test
    @DisplayName("EP: Bidder/Seller/Admin deu la instanceof User")
    void instanceOf_EP_tatCaLaUser() {
      assertAll(
          () -> assertInstanceOf(User.class, bidder),
          () -> assertInstanceOf(User.class, seller),
          () -> assertInstanceOf(User.class, admin)
      );
    }

    @Test
    @DisplayName("BVA: userId bat bien sau khi tao")
    void userId_BVA_khongThayDoi() {
      assertEquals("BIDDER-1", bidder.getUserId());
    }

    @Test
    @DisplayName("EP: setName() va setEmail() cap nhat dung gia tri")
    void setNameEmail_EP_capNhatDung() {
      bidder.setName("Ten Moi");
      bidder.setEmail("moi@test.com");

      assertAll(
          () -> assertEquals("Ten Moi", bidder.getName()),
          () -> assertEquals("moi@test.com", bidder.getEmail())
      );
    }

    @Test
    @DisplayName("EP: Seller.addAuction() -> luu va lay dung")
    void addAuction_EP_luuDung() {
      seller.addAuction("AUC-001");
      seller.addAuction("AUC-002");

      assertAll(
          () -> assertTrue(seller.getAuctionIds().contains("AUC-001")),
          () -> assertTrue(seller.getAuctionIds().contains("AUC-002")),
          () -> assertEquals(2, seller.getAuctionIds().size())
      );
    }

    @Test
    @DisplayName("BVA: getAuctionIds() tra ban sao -> data goc khong bi anh huong")
    void getAuctionIds_BVA_traBanSao_datGocAnToan() {
      seller.addAuction("AUC-001");
      seller.getAuctionIds().clear(); // Co tinh sua ban sao
      assertEquals(1, seller.getAuctionIds().size());
    }

    @Test
    @DisplayName("EP: Bidder.totalWins bat dau 0, tang 1 moi lan goi increment")
    void totalWins_EP_tangDung() {
      assertEquals(0, bidder.getTotalWins());
      bidder.incrementTotalWins();
      bidder.incrementTotalWins();
      assertEquals(2, bidder.getTotalWins());
    }

    @Test
    @DisplayName("EP: Bidder.totalSpent tang dung sau addToTotalSpent")
    void totalSpent_EP_tangDung() {
      assertEquals(0.0, bidder.getTotalSpent(), 0.001);
      bidder.addToTotalSpent(500.0);
      bidder.addToTotalSpent(300.0);
      assertEquals(800.0, bidder.getTotalSpent(), 0.001);
    }

    @Test
    @DisplayName("BVA: addToTotalSpent(0) -> totalSpent khong thay doi")
    void totalSpent_BVA_themKhong_khongThayDoi() {
      bidder.addToTotalSpent(0.0);
      assertEquals(0.0, bidder.getTotalSpent(), 0.001);
    }
  }
}