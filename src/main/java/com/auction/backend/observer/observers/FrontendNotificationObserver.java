package com.auction.backend.observer.observers;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionEventSerializer;
import com.auction.backend.observer.AuctionObserver;
import com.auction.backend.observer.FrontendNotifier;
import com.auction.exceptions.AuctionException;

/**
 * Observer gửi thông báo JSON tới frontend khi có auction event.
 *
 * <p>Luồng xử lý:
 * <ol>
 *   <li>Nhận {@link AuctionEvent} từ publisher.
 *   <li>Serialize event thành JSON qua {@link AuctionEventSerializer}.
 *   <li>Gọi {@link FrontendNotifier#sendNotification(String, String)}
 *       để frontend cập nhật UI.
 * </ol>
 *
 * <p>Frontend team cần implement {@link FrontendNotifier} và truyền vào đây.
 */
public class FrontendNotificationObserver
    implements AuctionObserver {

  private final FrontendNotifier frontendNotifier;

  /**
   * Constructor frontend notification observer.
   *
   * @param frontendNotifier implementation của team frontend
   */
  public FrontendNotificationObserver(
      FrontendNotifier frontendNotifier) {

    if (frontendNotifier == null) {

      throw new AuctionException(
          "FrontendNotifier không được null.");
    }

    this.frontendNotifier = frontendNotifier;
  }

  /**
   * Nhận event, serialize thành JSON rồi gửi tới frontend.
   * Event null được bỏ qua.
   *
   * @param event event được publish
   */
  @Override
  public void update(AuctionEvent event) {

    if (event == null) {
      return;
    }

    String json =
        AuctionEventSerializer.toJson(event);

    frontendNotifier.sendNotification(
        event.getAuctionId(),
        json);
  }

  @Override
  public String toString() {

    return "FrontendNotificationObserver{}";
  }
}
