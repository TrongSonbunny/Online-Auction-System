package com.auction.backend.observer;

import java.time.LocalDateTime;
import java.util.Objects;

import com.auction.exceptions.AuctionException;

/**
 * Đại diện cho event trong hệ thống đấu giá.
 */
public class AuctionEvent {

  private final AuctionEventType eventType;

  private final String auctionId;

  private final String message;

  private final LocalDateTime createdAt;

  /**
   * Constructor auction event.
   *
   * @param eventType loại event
   * @param auctionId mã auction
   * @param message nội dung event
   */
  public AuctionEvent(
      AuctionEventType eventType,
      String auctionId,
      String message) {

    this.eventType = Objects.requireNonNull(
        eventType,
        "Event type không được null.");

    validateAuctionId(auctionId);
    validateMessage(message);

    this.auctionId = auctionId;
    this.message = message;
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