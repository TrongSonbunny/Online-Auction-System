package com.auction.backend.observer;

/**
 * Interface gửi thông báo JSON tới frontend.
 *
 * <p>Frontend team implement interface này để nhận cập nhật real-time.
 * Ví dụ: ghi vào JavaFX property, push qua WebSocket, ghi file, v.v.
 *
 * <p>Ví dụ JSON nhận được với event NEW_BID:
 * <pre>{@code
 * {
 *   "eventType": "NEW_BID",
 *   "auctionId": "AUC-12345678",
 *   "message": "Alice đặt giá 500.0",
 *   "createdAt": "2026-05-13T10:30:00",
 *   "payload": {
 *     "transactionId": "TRANS-ABC123",
 *     "bidderName": "Alice",
 *     "bidAmount": 500.0,
 *     "createdAt": "2026-05-13T10:30:00"
 *   }
 * }
 * }</pre>
 */
public interface FrontendNotifier {

  /**
   * Gửi thông báo JSON tới frontend.
   *
   * @param auctionId mã auction liên quan
   * @param jsonPayload nội dung JSON đầy đủ của event
   */
  void sendNotification(
      String auctionId,
      String jsonPayload);
}