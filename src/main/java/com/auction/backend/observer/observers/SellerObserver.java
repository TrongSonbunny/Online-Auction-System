package com.auction.backend.observer.observers;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionObserver;
import com.auction.backend.observer.FrontendNotifier;
import com.auction.exceptions.AuctionException;
import com.google.gson.JsonObject;
import java.util.Objects;

/**
 * Observer dành cho seller.
 */
public class SellerObserver
    implements AuctionObserver {

  private final String sellerName;

  private final FrontendNotifier frontendNotifier;

  /**
   * Constructor seller observer.
   *
   * @param sellerName tên seller
   * @param frontendNotifier notifier gửi JSON tới client
   */
  public SellerObserver(
      String sellerName,
      FrontendNotifier frontendNotifier) {

    if (sellerName == null
        || sellerName.isBlank()) {

      throw new AuctionException(
          "Seller name không hợp lệ.");
    }

    this.sellerName = sellerName;

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
    json.addProperty("recipient", sellerName);
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

    return "SellerObserver{"
        + "sellerName='"
        + sellerName
        + '\''
        + '}';
  }
}