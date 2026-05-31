package com.auction.backend.observer;

import com.auction.exceptions.AuctionException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Bất biến đại diện cho một sự kiện trong hệ thống đấu giá.
 *
 * <p>Mang thông tin: loại event, auctionId, message mô tả và payload tùy chọn.
 * Payload theo quy ước: auction events → {@code Auction}, bid events → {@code BidTransaction}.
 * Dùng bởi {@link AuctionEventPublisher} để phân phối tới các {@link AuctionObserver}.
 */
public class AuctionEvent {

  private final AuctionEventType eventType;

  private final String auctionId;

  private final String message;

  private final LocalDateTime createdAt;

  /**
   * Payload đính kèm event — dùng cho JSON serialization và persistence.
   *
   * <p>Quy ước theo eventType:
   * <ul>
   *   <li>{@code AUCTION_CREATED}, {@code AUCTION_STARTED},
   *       {@code AUCTION_FINISHED}, {@code AUCTION_CANCELLED},
   *       {@code AUCTION_EXTENDED} → payload là {@code Auction}.
   *   <li>{@code NEW_BID}, {@code AUTO_BID_PLACED} → payload là {@code BidEventPayload}.
   * </ul>
   */
  private final Object payload;

  /**
   * Constructor auction event (không có payload — tương thích ngược).
   *
   * @param eventType loại event
   * @param auctionId mã auction
   * @param message nội dung event
   */
  public AuctionEvent(
      AuctionEventType eventType,
      String auctionId,
      String message) {

    this(eventType, auctionId, message, null);
  }

  /**
   * Constructor auction event với payload.
   *
   * @param eventType loại event
   * @param auctionId mã auction
   * @param message nội dung event
   * @param payload đối tượng dữ liệu đính kèm (Auction hoặc BidEventPayload)
   */
  public AuctionEvent(
      AuctionEventType eventType,
      String auctionId,
      String message,
      Object payload) {

    this.eventType = Objects.requireNonNull(
        eventType,
        "Event type không được null.");

    validateAuctionId(auctionId);
    validateMessage(message);

    this.auctionId = auctionId;
    this.message = message;
    this.payload = payload;
    this.createdAt = LocalDateTime.now();
  }

  /**
   * Validate auctionId.
   *
   * @param id auctionId
   */
  private void validateAuctionId(
      String id) {

    if (id == null || id.isBlank()) {

      throw new AuctionException(
          "AuctionId không hợp lệ.");
    }
  }

  /**
   * Validate message.
   *
   * @param eventMessage message
   */
  private void validateMessage(
      String eventMessage) {

    if (eventMessage == null
        || eventMessage.isBlank()) {

      throw new AuctionException(
          "Message không hợp lệ.");
    }
  }

  public AuctionEventType getEventType() {
    return eventType;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public String getMessage() {
    return message;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Object getPayload() {
    return payload;
  }

  @Override
  public String toString() {

    return "AuctionEvent{"
        + "eventType="
        + eventType
        + ", auctionId='"
        + auctionId
        + '\''
        + ", message='"
        + message
        + '\''
        + ", createdAt="
        + createdAt
        + '}';
  }
}