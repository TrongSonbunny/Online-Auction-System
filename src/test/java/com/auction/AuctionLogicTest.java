package com.auction;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.auction.backend.Auction;
import com.auction.backend.AuctionEvent;
import com.auction.backend.AuctionObserver;
import com.auction.backend.AuctionStatus;
import com.auction.backend.BidTransaction;
import com.auction.backend.Bidder;
import com.auction.backend.Seller;

/**
 * Unit Test cho Auction: placeBid, endAuction, Observer - JUnit 5.
 * EP  = Equivalence Partitioning
 * BVA = Boundary Value Analysis
 */
@DisplayName("Auction Logic Tests")
class AuctionLogicTest {

  private Auction auction;
  private Bidder bidder1;
  private Bidder bidder2;
  private Seller seller;

  @BeforeEach
  void setUp() {
    seller = new Seller("SELLER-1", "Nguoi Ban", "s@t.com", "hash");
    bidder1 = new Bidder("BIDDER-1", "Nguoi Mua A", "a@t.com", "hash");
    bidder2 = new Bidder("BIDDER-2", "Nguoi Mua B", "b@t.com", "hash");

    auction = new Auction(
        "AUC-001",
        "Laptop Gaming",
        "Mo ta san pham",
        seller.getUserId(),
        100.0,
        LocalDateTime.now().plusSeconds(60)
    );
  }

  // =========================================================================
  // A. Khoi tao Auction
  // =========================================================================
  @Nested
  @DisplayName("A. Khoi tao Auction")
  class KhoiTaoTest {

    @Test
    @DisplayName("EP: Tao moi -> OPEN, gia = gia khoi diem, chua co ai dat")
    void taoMoi_EP_trangThaiVaGiaDung() {
      assertAll(
          () -> assertEquals(AuctionStatus.OPEN, auction.getStatus()),
          () -> assertEquals(100.0, auction.getCurrentHighestBid(), 0.001),
          () -> assertNull(auction.getCurrentLeaderId()),
          () -> assertTrue(auction.getBidHistory().isEmpty())
      );
    }

    @Test
    @DisplayName("EP: Getter tra dung gia tri truyen vao khi tao")
    void getter_EP_traDungGiaTri() {
      assertAll(
          () -> assertEquals("AUC-001", auction.getAuctionId()),
          () -> assertEquals("Laptop Gaming", auction.getItemName()),
          () -> assertEquals("SELLER-1", auction.getSellerId()),
          () -> assertEquals(100.0, auction.getStartingPrice(), 0.001)
      );
    }
  }

  // =========================================================================
  // B. placeBid — EP va BVA
  // =========================================================================
  @Nested
  @DisplayName("B. placeBid - EP va BVA")
  class PlaceBidTest {

    @BeforeEach
    void batDauPhien() {
      auction.startAuction(); // Chuyen sang RUNNING
    }

    @Test
    @DisplayName("EP: Dat gia cao hon hien tai -> cap nhat thanh cong")
    void placeBid_EP_giaCaoHon_thanhCong() {
      BidTransaction tx = auction.placeBid(bidder1, 150.0);

      assertAll(
          () -> assertNotNull(tx),
          () -> assertEquals(150.0, auction.getCurrentHighestBid(), 0.001),
          () -> assertEquals("BIDDER-1", auction.getCurrentLeaderId()),
          () -> assertEquals(1, auction.getBidHistory().size())
      );
    }

    @Test
    @DisplayName("BVA: Dat gia = hien tai + 0.01 (bien nho nhat hop le) -> thanh cong")
    void placeBid_BVA_giaCaoHonDung001_thanhCong() {
      double justAbove = 100.0 + 0.01;
      auction.placeBid(bidder1, justAbove);

      assertEquals(justAbove, auction.getCurrentHighestBid(), 0.001);
    }

    @Test
    @DisplayName("BVA: Dat gia = gia hien tai (bien) -> throw exception")
    void placeBid_BVA_giaBangHienTai_throwException() {
      assertThrows(IllegalArgumentException.class,
          () -> auction.placeBid(bidder1, 100.0));
    }

    @Test
    @DisplayName("BVA: Dat gia = hien tai - 0.01 (bien duoi) -> throw exception")
    void placeBid_BVA_giaThapHonDung001_throwException() {
      assertThrows(IllegalArgumentException.class,
          () -> auction.placeBid(bidder1, 99.99));
    }

    @Test
    @DisplayName("EP: Dat gia am -> throw exception")
    void placeBid_EP_giaAm_throwException() {
      assertThrows(IllegalArgumentException.class,
          () -> auction.placeBid(bidder1, -1.0));
    }

    @Test
    @DisplayName("EP: Dat gia = 0 -> throw exception")
    void placeBid_EP_giaBangKhong_throwException() {
      assertThrows(IllegalArgumentException.class,
          () -> auction.placeBid(bidder1, 0.0));
    }

    @Test
    @DisplayName("EP: Nguoi ban tu dat gia phien cua minh -> throw exception")
    void placeBid_EP_nguoiBanTuDat_throwException() {
      Bidder sellerAsBidder = new Bidder("SELLER-1", "Nguoi Ban", "s@t.com", "hash");
      assertThrows(IllegalArgumentException.class,
          () -> auction.placeBid(sellerAsBidder, 200.0));
    }

    @Test
    @DisplayName("EP: Phien chua bat dau (OPEN) -> throw exception")
    void placeBid_EP_phienChuaBatDau_throwException() {
      Auction phienMoi = new Auction("AUC-002", "Item", "Desc",
          "SELLER-1", 100.0, LocalDateTime.now().plusSeconds(60));

      assertThrows(IllegalStateException.class,
          () -> phienMoi.placeBid(bidder1, 200.0));
    }

    @Test
    @DisplayName("EP: Nhieu lan dat gia -> nguoi dat cao nhat dang dan dau")
    void placeBid_EP_nhieuLan_nguoiCaoNhatDanDau() {
      auction.placeBid(bidder1, 200.0);
      auction.placeBid(bidder2, 300.0);
      auction.placeBid(bidder1, 400.0);

      assertAll(
          () -> assertEquals(400.0, auction.getCurrentHighestBid(), 0.001),
          () -> assertEquals("BIDDER-1", auction.getCurrentLeaderId()),
          () -> assertEquals(3, auction.getBidHistory().size())
      );
    }
  }

  // =========================================================================
  // C. endAuction va chuyen trang thai
  // =========================================================================
  @Nested
  @DisplayName("C. endAuction va chuyen trang thai")
  class EndAuctionTest {

    @Test
    @DisplayName("EP: Ket thuc co nguoi thang -> FINISHED, co currentLeader")
    void endAuction_EP_coNguoiThang_FINISHED() {
      auction.startAuction();
      auction.placeBid(bidder1, 200.0);
      auction.endAuction();

      assertAll(
          () -> assertEquals(AuctionStatus.FINISHED, auction.getStatus()),
          () -> assertEquals("BIDDER-1", auction.getCurrentLeaderId())
      );
    }

    @Test
    @DisplayName("EP: Ket thuc khong co ai dat gia -> tu dong CANCELED")
    void endAuction_EP_khongCoBid_tuDongCANCELED() {
      auction.startAuction();
      auction.endAuction();

      assertEquals(AuctionStatus.CANCELED, auction.getStatus());
    }

    @Test
    @DisplayName("EP: Dat gia sau FINISHED -> throw exception")
    void placeBid_EP_sauFinished_throwException() {
      auction.startAuction();
      auction.placeBid(bidder1, 200.0);
      auction.endAuction();

      assertThrows(IllegalStateException.class,
          () -> auction.placeBid(bidder2, 300.0));
    }

    @Test
    @DisplayName("EP: FINISHED -> markAsPaid() -> PAID")
    void markAsPaid_EP_tuFinished_PAID() {
      auction.startAuction();
      auction.placeBid(bidder1, 200.0);
      auction.endAuction();
      auction.markAsPaid();

      assertEquals(AuctionStatus.PAID, auction.getStatus());
    }

    @Test
    @DisplayName("BVA: PAID -> markAsPaid() lan nua -> throw exception")
    void markAsPaid_BVA_tuPaidLanNua_throwException() {
      auction.startAuction();
      auction.placeBid(bidder1, 200.0);
      auction.endAuction();
      auction.markAsPaid();

      assertThrows(IllegalStateException.class,
          () -> auction.markAsPaid());
    }

    @Test
    @DisplayName("EP: cancelAuction() tu RUNNING -> CANCELED")
    void cancelAuction_EP_tuRunning_CANCELED() {
      auction.startAuction();
      auction.cancelAuction();

      assertEquals(AuctionStatus.CANCELED, auction.getStatus());
    }

    @Test
    @DisplayName("BVA: CANCELED -> cancelAuction() lan nua -> throw exception")
    void cancelAuction_BVA_tuCanceledLanNua_throwException() {
      auction.startAuction();
      auction.cancelAuction();

      assertThrows(IllegalStateException.class,
          () -> auction.cancelAuction());
    }

    @Test
    @DisplayName("EP: extendEndTime them dung so giay")
    void extendEndTime_EP_themDungGiay() {
      auction.startAuction();
      LocalDateTime truoc = auction.getEndTime();
      auction.extendEndTime(30);

      assertEquals(truoc.plusSeconds(30), auction.getEndTime());
    }
  }

  // =========================================================================
  // D. Observer — Thong bao
  // =========================================================================
  @Nested
  @DisplayName("D. Observer - Kiem tra thong bao")
  class ObserverTest {

    @Test
    @DisplayName("EP: Observer dang ky -> nhan duoc NEW_BID")
    void registerObserver_EP_nhanDuocNewBid() {
      boolean[] received = {false};

      auction.registerObserver(event -> {
        if (event.getEventType() == AuctionEvent.EventType.NEW_BID) {
          received[0] = true;
        }
      });

      auction.startAuction();
      auction.placeBid(bidder1, 200.0);

      assertTrue(received[0], "Observer phai nhan duoc NEW_BID");
    }

    @Test
    @DisplayName("EP: Observer xoa di -> khong nhan thong bao sau do")
    void removeObserver_EP_khongNhanThongBao() {
      int[] count = {0};
      AuctionObserver obs = event -> count[0]++;

      auction.registerObserver(obs);
      auction.removeObserver(obs);

      auction.startAuction();
      auction.placeBid(bidder1, 200.0);

      assertEquals(0, count[0], "Observer da xoa khong duoc nhan thong bao");
    }

    @Test
    @DisplayName("EP: getBidHistory() tra unmodifiable list -> khong sua duoc")
    void getBidHistory_EP_traUnmodifiableList() {
      auction.startAuction();
      auction.placeBid(bidder1, 200.0);

      List<BidTransaction> history = auction.getBidHistory();

      assertThrows(UnsupportedOperationException.class,
          () -> history.clear());
    }
  }
}