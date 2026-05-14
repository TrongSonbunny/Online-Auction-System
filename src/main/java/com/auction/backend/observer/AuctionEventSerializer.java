package com.auction.backend.observer;

import com.auction.models.auction.Auction;
import com.auction.models.bid.BidTransaction;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Utility chuyển đổi AuctionEvent thành JSON string để gửi tới frontend.
 *
 * <p>Chỉ serialize các field cần thiết từ payload — tránh circular reference
 * (ví dụ: Auction → Seller → không serialize toàn bộ đối tượng lồng nhau).
 *
 * <p>Cấu trúc JSON cơ bản:
 * <pre>{@code
 * {
 *   "eventType": "NEW_BID",
 *   "auctionId": "AUC-12345678",
 *   "message": "Alice đặt giá 500.0",
 *   "createdAt": "2026-05-13T10:30:00",
 *   "payload": { ... }   // phụ thuộc eventType
 * }
 * }</pre>
 */
public final class AuctionEventSerializer {

  private AuctionEventSerializer() {
  }

  /**
   * Chuyển AuctionEvent thành JSON string.
   *
   * @param event event cần serialize
   * @return JSON string đầy đủ
   */
  public static String toJson(AuctionEvent event) {

    if (event == null) {
      return "{}";
    }

    JsonObject root = new JsonObject();

    root.addProperty(
        "eventType",
        event.getEventType().name());

    root.addProperty(
        "auctionId",
        event.getAuctionId());

    root.addProperty(
        "message",
        event.getMessage());

    root.addProperty(
        "createdAt",
        event.getCreatedAt().toString());

    JsonObject payloadJson =
        buildPayload(event);

    if (payloadJson != null) {
      root.add("payload", payloadJson);
    } else {
      root.add("payload", JsonNull.INSTANCE);
    }

    return root.toString();
  }

  /**
   * Xây dựng payload JSON phụ thuộc eventType.
   *
   * @param event event nguồn
   * @return JsonObject payload, hoặc null nếu không có payload
   */
  private static JsonObject buildPayload(
      AuctionEvent event) {

    Object raw = event.getPayload();

    if (raw == null) {
      return null;
    }

    switch (event.getEventType()) {

      case NEW_BID:
      case AUTO_BID_PLACED:
        if (raw instanceof BidTransaction tx) {
          return buildBidPayload(tx);
        }
        break;

      case AUCTION_CREATED:
      case AUCTION_STARTED:
      case AUCTION_FINISHED:
      case AUCTION_CANCELLED:
      case AUCTION_EXTENDED:
        if (raw instanceof Auction auction) {
          return buildAuctionPayload(auction);
        }
        break;

      default:
        break;
    }

    return null;
  }

  /**
   * Serialize BidTransaction thành payload JSON.
   * Chỉ lấy fields cần thiết cho frontend.
   *
   * @param tx bid transaction
   * @return JsonObject payload
   */
  private static JsonObject buildBidPayload(
      BidTransaction tx) {

    JsonObject obj = new JsonObject();

    obj.addProperty(
        "transactionId",
        tx.getTransactionId());

    obj.addProperty(
        "bidderName",
        tx.getBidder().getName());

    obj.addProperty(
        "bidAmount",
        tx.getBidAmount());

    obj.addProperty(
        "createdAt",
        tx.getCreatedAt().toString());

    return obj;
  }

  /**
   * Serialize Auction thành payload JSON.
   * Chỉ lấy fields trạng thái cần thiết cho frontend.
   *
   * @param auction auction
   * @return JsonObject payload
   */
  private static JsonObject buildAuctionPayload(
      Auction auction) {

    JsonObject obj = new JsonObject();

    obj.addProperty(
        "currentHighestBid",
        auction.getCurrentHighestBid());

    if (auction.getCurrentHighestBidder()
        != null) {

      obj.addProperty(
          "currentHighestBidder",
          auction.getCurrentHighestBidder()
              .getName());

    } else {

      obj.add(
          "currentHighestBidder",
          JsonNull.INSTANCE);
    }

    obj.addProperty(
        "status",
        auction.getStatus().name());

    if (auction.getScheduledEndTime() != null) {

      obj.addProperty(
          "scheduledEndTime",
          auction.getScheduledEndTime()
              .toString());
    }

    if (auction.getEndTime() != null) {

      obj.addProperty(
          "endTime",
          auction.getEndTime().toString());
    }

    return obj;
  }
}
