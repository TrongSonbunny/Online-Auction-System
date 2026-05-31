package com.auction.backend.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.exceptions.AuctionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho AuctionEvent.
 *
 * <p>EP: eventType null / valid; auctionId null / blank / valid; message null / blank / valid
 * BVA: auctionId = "" (invalid), 1 ký tự (valid); message = " " (invalid blank)
 */
@DisplayName("AuctionEvent Tests")
class AuctionEventTest {

  private AuctionEvent validEvent() {
    return new AuctionEvent(
        AuctionEventType.AUCTION_CREATED,
        "AUC-001",
        "Auction đã được tạo");
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor - EventType (EP)")
  class EventTypeValidation {

    @Test
    @DisplayName("EP-Valid: eventType hợp lệ lưu đúng")
    void constructor_validEventType_stores() {
      assertEquals(AuctionEventType.AUCTION_CREATED, validEvent().getEventType());
    }

    @Test
    @DisplayName("EP-Invalid: eventType null ném NullPointerException")
    void constructor_nullEventType_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> new AuctionEvent(null, "AUC-001", "Message"));
    }

    @Test
    @DisplayName("EP-Valid: tất cả loại event hoạt động đúng")
    void constructor_allEventTypes_work() {
      for (AuctionEventType type : AuctionEventType.values()) {
        AuctionEvent event = new AuctionEvent(type, "AUC-001", "msg");
        assertEquals(type, event.getEventType());
      }
    }
  }

  @Nested
  @DisplayName("Constructor - AuctionId (EP + BVA)")
  class AuctionIdValidation {

    @Test
    @DisplayName("EP-Valid: auctionId hợp lệ lưu đúng")
    void constructor_validAuctionId_stores() {
      assertEquals("AUC-001", validEvent().getAuctionId());
    }

    @Test
    @DisplayName("EP-Invalid: auctionId null ném AuctionException")
    void constructor_nullAuctionId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionEvent(AuctionEventType.NEW_BID, null, "msg"));
    }

    @Test
    @DisplayName("BVA-Boundary: auctionId rỗng ném AuctionException")
    void constructor_emptyAuctionId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionEvent(AuctionEventType.NEW_BID, "", "msg"));
    }

    @Test
    @DisplayName("BVA-Boundary: auctionId blank ném AuctionException")
    void constructor_blankAuctionId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionEvent(AuctionEventType.NEW_BID, "   ", "msg"));
    }

    @Test
    @DisplayName("BVA-Boundary: auctionId 1 ký tự hợp lệ")
    void constructor_singleCharAuctionId_isValid() {
      AuctionEvent event = new AuctionEvent(AuctionEventType.NEW_BID, "A", "msg");
      assertEquals("A", event.getAuctionId());
    }
  }

  @Nested
  @DisplayName("Constructor - Message (EP + BVA)")
  class MessageValidation {

    @Test
    @DisplayName("EP-Valid: message hợp lệ lưu đúng")
    void constructor_validMessage_stores() {
      assertEquals("Auction đã được tạo", validEvent().getMessage());
    }

    @Test
    @DisplayName("EP-Invalid: message null ném AuctionException")
    void constructor_nullMessage_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionEvent(AuctionEventType.NEW_BID, "AUC-001", null));
    }

    @Test
    @DisplayName("BVA-Boundary: message rỗng ném AuctionException")
    void constructor_emptyMessage_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionEvent(AuctionEventType.NEW_BID, "AUC-001", ""));
    }

    @Test
    @DisplayName("BVA-Boundary: message blank ném AuctionException")
    void constructor_blankMessage_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionEvent(AuctionEventType.NEW_BID, "AUC-001", "  "));
    }

    @Test
    @DisplayName("BVA-Boundary: message 1 ký tự hợp lệ")
    void constructor_singleCharMessage_isValid() {
      AuctionEvent event = new AuctionEvent(AuctionEventType.NEW_BID, "AUC-001", "X");
      assertEquals("X", event.getMessage());
    }
  }

  @Nested
  @DisplayName("CreatedAt và toString")
  class CreatedAtAndToString {

    @Test
    @DisplayName("EP-Valid: createdAt không null")
    void constructor_createdAt_isNotNull() {
      assertNotNull(validEvent().getCreatedAt());
    }

    @Test
    @DisplayName("toString không null")
    void toString_isNotNull() {
      assertNotNull(validEvent().toString());
    }
  }
}
