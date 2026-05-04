package com.auction;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuthenticationException;
import com.auction.exceptions.ConnectionException;
import com.auction.exceptions.DataException;
import com.auction.exceptions.InvalidBidException;

/**
 * Unit Test cho toan bo Custom Exception - JUnit 5.
 *
 * <p>EP = Equivalence Partitioning : chia input thanh nhom hop le / khong hop le 
 * BVA = Boundary Value Analysis : kiem tra gia tri tai bien
 *
 * <p>Luu y quan trong: Tat ca exception deu extends Exception (checked exception), 
 * KHONG phai RuntimeException -> phai dung assertThrows() hoac throws trong test.
 */
@DisplayName("Custom Exception Tests - Huong")
class CustomExceptionTest {

  // =========================================================================
  // 1. AuctionClosedException
  // =========================================================================
  @Nested
  @DisplayName("1. AuctionClosedException")
  class AuctionClosedExceptionTest {

    @Test
    @DisplayName("EP: Message binh thuong -> getMessage() tra dung")
    void constructor_EP_messageBinhThuong_traDung() {
      AuctionClosedException ex = new AuctionClosedException("Phien dau gia da ket thuc");

      assertAll(
          () -> assertNotNull(ex),
          () -> assertEquals("Phien dau gia da ket thuc", ex.getMessage())
      );
    }

    @Test
    @DisplayName("BVA: Message rong -> khong crash, getMessage() tra chuoi rong")
    void constructor_BVA_messageRong_khongCrash() {
      AuctionClosedException ex = new AuctionClosedException("");
      assertNotNull(ex);
      assertEquals("", ex.getMessage());
    }

    @Test
    @DisplayName("BVA: Message null -> getMessage() tra null")
    void constructor_BVA_messageNull_traNullAnToan() {
      AuctionClosedException ex = new AuctionClosedException(null);
      assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("EP: Message rat dai -> luu du, khong bi cat")
    void constructor_EP_messageRatDai_luuDu() {
      String longMsg = "X".repeat(1000);
      AuctionClosedException ex = new AuctionClosedException(longMsg);
      assertEquals(1000, ex.getMessage().length());
    }

    @Test
    @DisplayName("AuctionClosedException phai la checked Exception")
    void auctionClosedException_laCheckedEx_khongLaRuntimeException() {
      AuctionClosedException ex = new AuctionClosedException("test");
      assertInstanceOf(Exception.class, ex);
    }

    @Test
    @DisplayName("EP: Nem va bat AuctionClosedException -> bat duoc dung")
    void throwAndCatch_EP_batDuocDung() {
      AuctionClosedException caught = assertThrows(
          AuctionClosedException.class,
          () -> {
            throw new AuctionClosedException("Phien da dong");
          }
      );
      assertEquals("Phien da dong", caught.getMessage());
    }
  }

  // =========================================================================
  // 2. AuthenticationException
  // =========================================================================
  @Nested
  @DisplayName("2. AuthenticationException")
  class AuthenticationExceptionTest {

    @Test
    @DisplayName("EP: Message sai mat khau -> luu dung")
    void constructor_EP_messageSaiMatKhau_luuDung() {
      AuthenticationException ex = new AuthenticationException("Sai mat khau");
      assertEquals("Sai mat khau", ex.getMessage());
    }

    @Test
    @DisplayName("EP: Message tai khoan khong ton tai -> luu dung")
    void constructor_EP_messageTaiKhoanKhongTonTai_luuDung() {
      AuthenticationException ex = new AuthenticationException("Tai khoan khong ton tai");
      assertEquals("Tai khoan khong ton tai", ex.getMessage());
    }

    @Test
    @DisplayName("BVA: Message rong -> khong crash")
    void constructor_BVA_messageRong_khongCrash() {
      AuthenticationException ex = new AuthenticationException("");
      assertNotNull(ex);
      assertEquals("", ex.getMessage());
    }

    @Test
    @DisplayName("BVA: Message null -> tra null an toan")
    void constructor_BVA_messageNull_traNullAnToan() {
      AuthenticationException ex = new AuthenticationException(null);
      assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("AuthenticationException phai la checked Exception")
    void authenticationException_laCheckedEx() {
      AuthenticationException ex = new AuthenticationException("test");
      assertInstanceOf(Exception.class, ex);
    }

    @Test
    @DisplayName("EP: Nem va bat AuthenticationException -> bat duoc dung")
    void throwAndCatch_EP_batDuocDung() {
      AuthenticationException caught = assertThrows(
          AuthenticationException.class,
          () -> {
            throw new AuthenticationException("Xac thuc that bai");
          }
      );
      assertEquals("Xac thuc that bai", caught.getMessage());
    }
  }

  // =========================================================================
  // 3. ConnectionException
  // =========================================================================
  @Nested
  @DisplayName("3. ConnectionException")
  class ConnectionExceptionTest {

    @Test
    @DisplayName("EP: Message loi ket noi -> luu dung")
    void constructor_EP_messageLoiKetNoi_luuDung() {
      ConnectionException ex = new ConnectionException("Khong the ket noi den server");
      assertEquals("Khong the ket noi den server", ex.getMessage());
    }

    @Test
    @DisplayName("BVA: Message rong -> khong crash")
    void constructor_BVA_messageRong_khongCrash() {
      ConnectionException ex = new ConnectionException("");
      assertNotNull(ex);
      assertEquals("", ex.getMessage());
    }

    @Test
    @DisplayName("BVA: Message null -> tra null an toan")
    void constructor_BVA_messageNull_traNullAnToan() {
      ConnectionException ex = new ConnectionException(null);
      assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("EP: Message chua dia chi IP -> luu day du")
    void constructor_EP_messageChuaIP_luuDayDu() {
      String msg = "Timeout khi ket noi toi 192.168.1.1:8080";
      ConnectionException ex = new ConnectionException(msg);
      assertEquals(msg, ex.getMessage());
      assertTrue(ex.getMessage().contains("192.168.1.1:8080"));
    }

    @Test
    @DisplayName("ConnectionException phai la checked Exception")
    void connectionException_laCheckedEx() {
      assertInstanceOf(Exception.class, new ConnectionException("test"));
    }

    @Test
    @DisplayName("EP: Nem va bat ConnectionException -> bat duoc dung")
    void throwAndCatch_EP_batDuocDung() {
      ConnectionException caught = assertThrows(
          ConnectionException.class,
          () -> {
            throw new ConnectionException("Mat ket noi");
          }
      );
      assertEquals("Mat ket noi", caught.getMessage());
    }
  }

  // =========================================================================
  // 4. DataException
  // =========================================================================
  @Nested
  @DisplayName("4. DataException")
  class DataExceptionTest {

    @Test
    @DisplayName("EP: Message va cause hop le -> luu ca hai dung")
    void constructor_EP_messageVaCauseHopLe_luuCaHai() {
      Throwable cause = new RuntimeException("Loi goc tu database");
      DataException ex = new DataException("Loi doc du lieu", cause);

      assertAll(
          () -> assertEquals("Loi doc du lieu", ex.getMessage()),
          () -> assertSame(cause, ex.getCause()),
          () -> assertEquals("Loi goc tu database", ex.getCause().getMessage())
      );
    }

    @Test
    @DisplayName("BVA: Message rong, cause hop le -> khong crash")
    void constructor_BVA_messageRong_causeHopLe_khongCrash() {
      Throwable cause = new RuntimeException("cause");
      DataException ex = new DataException("", cause);
      assertNotNull(ex);
      assertEquals("", ex.getMessage());
      assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("BVA: Message null, cause hop le -> getMessage() tra null")
    void constructor_BVA_messageNull_causeHopLe_traNullAnToan() {
      Throwable cause = new RuntimeException("cause");
      DataException ex = new DataException(null, cause);
      assertNull(ex.getMessage());
      assertNotNull(ex.getCause());
    }

    @Test
    @DisplayName("BVA: Cause null -> getCause() tra null")
    void constructor_BVA_causeNull_getCauseTraNull() {
      DataException ex = new DataException("Loi du lieu", null);
      assertEquals("Loi du lieu", ex.getMessage());
      assertNull(ex.getCause());
    }

    @Test
    @DisplayName("EP: Cause la IOException -> getCause() la IOException")
    void constructor_EP_causeIOException_getCauseLayDung() {
      java.io.IOException ioEx = new java.io.IOException("File not found");
      DataException ex = new DataException("Loi ghi file", ioEx);
      assertInstanceOf(java.io.IOException.class, ex.getCause());
      assertEquals("File not found", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("DataException phai la checked Exception")
    void dataException_laCheckedEx() {
      DataException ex = new DataException("test", new RuntimeException());
      assertInstanceOf(Exception.class, ex);
    }

    @Test
    @DisplayName("EP: Nem va bat DataException -> bat duoc dung voi cause")
    void throwAndCatch_EP_batDuocDungVoiCause() {
      Throwable cause = new RuntimeException("nguyen nhan");
      DataException caught = assertThrows(
          DataException.class,
          () -> {
            throw new DataException("Loi xu ly du lieu", cause);
          }
      );
      assertEquals("Loi xu ly du lieu", caught.getMessage());
      assertSame(cause, caught.getCause());
    }
  }

  // =========================================================================
  // 5. InvalidBidException
  // =========================================================================
  @Nested
  @DisplayName("5. InvalidBidException")
  class InvalidBidExceptionTest {

    @Test
    @DisplayName("EP: Message gia thap hon -> luu dung")
    void constructor_EP_messagGiaThapHon_luuDung() {
      InvalidBidException ex = new InvalidBidException(
          "Gia dat 50.0 phai cao hon gia hien tai 100.0");
      assertNotNull(ex);
      assertTrue(ex.getMessage().contains("50.0"));
      assertTrue(ex.getMessage().contains("100.0"));
    }

    @Test
    @DisplayName("EP: Message gia bang gia hien tai -> luu dung")
    void constructor_EP_messageGiaBangHienTai_luuDung() {
      InvalidBidException ex = new InvalidBidException(
          "Gia dat phai VUOT QUA gia hien tai, khong duoc bang");
      assertEquals("Gia dat phai VUOT QUA gia hien tai, khong duoc bang",
          ex.getMessage());
    }

    @Test
    @DisplayName("BVA: Message rong -> khong crash")
    void constructor_BVA_messageRong_khongCrash() {
      InvalidBidException ex = new InvalidBidException("");
      assertNotNull(ex);
      assertEquals("", ex.getMessage());
    }

    @Test
    @DisplayName("BVA: Message null -> tra null an toan")
    void constructor_BVA_messageNull_traNullAnToan() {
      InvalidBidException ex = new InvalidBidException(null);
      assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("EP: Message chua gia tri cu the -> lay dung thong tin")
    void constructor_EP_messageChuaGiaTri_layDungThongTin() {
      String msg = "Gia dat [99.99] khong hop le, gia hien tai la [100.0]";
      InvalidBidException ex = new InvalidBidException(msg);
      assertTrue(ex.getMessage().contains("99.99"));
      assertTrue(ex.getMessage().contains("100.0"));
    }

    @Test
    @DisplayName("InvalidBidException phai la checked Exception")
    void invalidBidException_laCheckedEx() {
      InvalidBidException ex = new InvalidBidException("test");
      assertInstanceOf(Exception.class, ex);
    }

    @Test
    @DisplayName("EP: Nem va bat InvalidBidException -> bat duoc dung")
    void throwAndCatch_EP_batDuocDung() {
      InvalidBidException caught = assertThrows(
          InvalidBidException.class,
          () -> {
            throw new InvalidBidException("Gia khong hop le");
          }
      );
      assertEquals("Gia khong hop le", caught.getMessage());
    }
  }
}