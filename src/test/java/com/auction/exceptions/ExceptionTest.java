package com.auction.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho toàn bộ custom exception hierarchy.
 *
 * <p>EP: thông điệp hợp lệ / null; có cause / không có cause
 * BVA: chuỗi rỗng vs chuỗi 1 ký tự; cause null vs cause có giá trị
 */
@DisplayName("Custom Exceptions Tests")
class ExceptionTest {

  // ───────────────────────── AuctionException ─────────────────────────

  @Nested
  @DisplayName("AuctionException")
  class AuctionExceptionTest {

    @Test
    @DisplayName("EP-Valid: tạo với message hợp lệ lưu đúng message")
    void constructor_withValidMessage_storesMessage() {
      AuctionException ex = new AuctionException("lỗi auction");
      assertEquals("lỗi auction", ex.getMessage());
    }

    @Test
    @DisplayName("EP-Valid: tạo với message và cause lưu cả hai")
    void constructor_withMessageAndCause_storesBoth() {
      Throwable cause = new RuntimeException("nguyên nhân");
      AuctionException ex = new AuctionException("lỗi", cause);
      assertEquals("lỗi", ex.getMessage());
      assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("EP-Invalid: message null vẫn tạo được, getMessage() trả null")
    void constructor_withNullMessage_returnsNull() {
      AuctionException ex = new AuctionException((String) null);
      assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("BVA-Boundary: message 1 ký tự vẫn hợp lệ")
    void constructor_withSingleCharMessage_isValid() {
      AuctionException ex = new AuctionException("X");
      assertEquals("X", ex.getMessage());
    }

    @Test
    @DisplayName("EP-Valid: là RuntimeException (unchecked)")
    void auctionException_isRuntimeException() {
      assertInstanceOf(RuntimeException.class, new AuctionException("msg"));
    }

    @Test
    @DisplayName("EP-Valid: ném và bắt được bởi catch RuntimeException")
    void auctionException_canBeCaughtAsRuntimeException() {
      assertThrows(RuntimeException.class,
          () -> { 
            throw new AuctionException("test"); });
    }
  }

  // ───────────────────────── AuctionClosedException ─────────────────────────

  @Nested
  @DisplayName("AuctionClosedException")
  class AuctionClosedExceptionTest {

    @Test
    @DisplayName("EP-Valid: tạo với message lưu đúng message")
    void constructor_withMessage_storesMessage() {
      AuctionClosedException ex = new AuctionClosedException("đã đóng");
      assertEquals("đã đóng", ex.getMessage());
    }

    @Test
    @DisplayName("EP-Valid: tạo với cause lưu cause")
    void constructor_withCause_storesCause() {
      Throwable cause = new IllegalStateException("gốc");
      AuctionClosedException ex = new AuctionClosedException("đóng", cause);
      assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("EP-Valid: kế thừa AuctionException")
    void isSubclassOfAuctionException() {
      assertInstanceOf(AuctionException.class,
          new AuctionClosedException("msg"));
    }

    @Test
    @DisplayName("EP-Valid: bắt được bởi AuctionException")
    void canBeCaughtAsAuctionException() {
      assertThrows(AuctionException.class,
          () -> { 
            throw new AuctionClosedException("test"); });
    }

    @Test
    @DisplayName("BVA-Boundary: message rỗng vẫn tạo được")
    void constructor_withEmptyMessage_doesNotThrow() {
      AuctionClosedException ex = new AuctionClosedException("");
      assertEquals("", ex.getMessage());
    }
  }

  // ───────────────────────── BidException ─────────────────────────

  @Nested
  @DisplayName("BidException")
  class BidExceptionTest {

    @Test
    @DisplayName("EP-Valid: message hợp lệ lưu đúng")
    void constructor_withValidMessage_storesMessage() {
      BidException ex = new BidException("bid lỗi");
      assertEquals("bid lỗi", ex.getMessage());
    }

    @Test
    @DisplayName("EP-Valid: với cause lưu cause")
    void constructor_withCause_storesCause() {
      Throwable cause = new RuntimeException("gốc");
      BidException ex = new BidException("msg", cause);
      assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("EP-Valid: kế thừa AuctionException")
    void isSubclassOfAuctionException() {
      assertInstanceOf(AuctionException.class, new BidException("msg"));
    }

    @Test
    @DisplayName("BVA-Boundary: message 1 ký tự")
    void constructor_withSingleChar_isValid() {
      BidException ex = new BidException("B");
      assertEquals("B", ex.getMessage());
    }
  }

  // ───────────────────────── InvalidBidException ─────────────────────────

  @Nested
  @DisplayName("InvalidBidException")
  class InvalidBidExceptionTest {

    @Test
    @DisplayName("EP-Valid: message hợp lệ")
    void constructor_withMessage_storesMessage() {
      InvalidBidException ex = new InvalidBidException("bid không hợp lệ");
      assertEquals("bid không hợp lệ", ex.getMessage());
    }

    @Test
    @DisplayName("EP-Valid: với cause")
    void constructor_withCause_storesCause() {
      Throwable cause = new Exception("gốc");
      InvalidBidException ex = new InvalidBidException("invalid", cause);
      assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("EP-Valid: kế thừa BidException")
    void isSubclassOfBidException() {
      assertInstanceOf(BidException.class, new InvalidBidException("msg"));
    }

    @Test
    @DisplayName("EP-Valid: kế thừa AuctionException")
    void isSubclassOfAuctionException() {
      assertInstanceOf(AuctionException.class, new InvalidBidException("msg"));
    }

    @Test
    @DisplayName("EP-Valid: bắt được ở catch BidException")
    void canBeCaughtAsBidException() {
      assertThrows(BidException.class,
          () -> { 
            throw new InvalidBidException("test"); });
    }
  }

  // ───────────────────────── PaymentException ─────────────────────────

  @Nested
  @DisplayName("PaymentException")
  class PaymentExceptionTest {

    @Test
    @DisplayName("EP-Valid: message hợp lệ")
    void constructor_withMessage_storesMessage() {
      PaymentException ex = new PaymentException("thanh toán lỗi");
      assertEquals("thanh toán lỗi", ex.getMessage());
    }

    @Test
    @DisplayName("EP-Valid: với cause")
    void constructor_withCause_storesCause() {
      Throwable cause = new IllegalArgumentException("gốc");
      PaymentException ex = new PaymentException("lỗi", cause);
      assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("EP-Valid: kế thừa AuctionException")
    void isSubclassOfAuctionException() {
      assertInstanceOf(AuctionException.class, new PaymentException("msg"));
    }

    @Test
    @DisplayName("BVA-Boundary: message null vẫn tạo được")
    void constructor_withNullMessage_nullCause_doesNotThrow() {
      PaymentException ex = new PaymentException((String) null);
      assertNotNull(ex);
      assertNull(ex.getMessage());
    }
  }

  // ───────────────────────── UnauthorizedException ─────────────────────────

  @Nested
  @DisplayName("UnauthorizedException")
  class UnauthorizedExceptionTest {

    @Test
    @DisplayName("EP-Valid: message hợp lệ")
    void constructor_withMessage_storesMessage() {
      UnauthorizedException ex = new UnauthorizedException("không có quyền");
      assertEquals("không có quyền", ex.getMessage());
    }

    @Test
    @DisplayName("EP-Valid: với cause")
    void constructor_withCause_storesCause() {
      Throwable cause = new RuntimeException("security");
      UnauthorizedException ex = new UnauthorizedException("unauthorized", cause);
      assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("EP-Valid: kế thừa AuctionException")
    void isSubclassOfAuctionException() {
      assertInstanceOf(AuctionException.class,
          new UnauthorizedException("msg"));
    }

    @Test
    @DisplayName("EP-Valid: bắt được ở catch AuctionException")
    void canBeCaughtAsAuctionException() {
      assertThrows(AuctionException.class,
          () -> { 
            throw new UnauthorizedException("test"); });
    }

    @Test
    @DisplayName("BVA-Boundary: message 1 ký tự")
    void constructor_withSingleChar_isValid() {
      UnauthorizedException ex = new UnauthorizedException("U");
      assertEquals("U", ex.getMessage());
    }
  }

  // ───────────────────────── Exception Hierarchy ─────────────────────────

  @Nested
  @DisplayName("Exception Hierarchy")
  class ExceptionHierarchyTest {

    @Test
    @DisplayName("InvalidBidException -> BidException -> AuctionException -> RuntimeException")
    void invalidBidException_fullHierarchy() {
      InvalidBidException ex = new InvalidBidException("test");
      assertInstanceOf(BidException.class, ex);
      assertInstanceOf(AuctionException.class, ex);
      assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("AuctionClosedException -> AuctionException -> RuntimeException")
    void auctionClosedException_fullHierarchy() {
      AuctionClosedException ex = new AuctionClosedException("test");
      assertInstanceOf(AuctionException.class, ex);
      assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("PaymentException -> AuctionException -> RuntimeException")
    void paymentException_fullHierarchy() {
      PaymentException ex = new PaymentException("test");
      assertInstanceOf(AuctionException.class, ex);
      assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("UnauthorizedException -> AuctionException -> RuntimeException")
    void unauthorizedException_fullHierarchy() {
      UnauthorizedException ex = new UnauthorizedException("test");
      assertInstanceOf(AuctionException.class, ex);
      assertInstanceOf(RuntimeException.class, ex);
    }
  }
}
