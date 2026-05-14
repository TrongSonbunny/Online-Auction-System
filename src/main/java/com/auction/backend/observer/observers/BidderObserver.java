package com.auction.backend.observer.observers;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionObserver;
import com.auction.backend.observer.FrontendNotifier;
import com.auction.exceptions.BidException;
import com.google.gson.JsonObject;
import java.util.Objects;

/**
 * Observer dành cho bidder.
 */
public class BidderObserver
    implements AuctionObserver {

  private final String bidderName;

  private final FrontendNotifier frontendNotifier;

  /**
   * Constructor bidder observer.
   *
   * @param bidderName tên bidder
   * @param frontendNotifier notifier gửi JSON tới client
   */
  public BidderObserver(
      String bidderName,
      FrontendNotifier frontendNotifier) {

    if (bidderName == null
        || bidderName.isBlank()) {

      throw new BidException(
          "Bidder name không hợp lệ.");
    }

    this.bidderName = bidderName;

    this.frontendNotifier =
        Objects.requireNonNull(
            frontendNotifier,
            "FrontendNotifier không được null.");
  }

  /**
   * Nhận event, đóng gói thành JSON và gửi tới client qua FrontendNotifier.
   *
   * @param event event được publish
   */
  @Override
  public void update(
      AuctionEvent event) {

    JsonObject json = new JsonObject();
    json.addProperty("recipient", bidderName);
    json.addProperty(
        "eventType",
        event.getEventType().name());
    json.addProperty(
        "auctionId",
        event.getAuctionId());
    json.addProperty(
        "message",
        event.getMessage());
    json.addProperty(
        "createdAt",
        event.getCreatedAt().toString());

    frontendNotifier.sendNotification(
        event.getAuctionId(),
        json.toString());
  }

  @Override
  public String toString() {

    return "BidderObserver{"
        + "bidderName='"
        + bidderName
        + '\''
        + '}';
  }
}