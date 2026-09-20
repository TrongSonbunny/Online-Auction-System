package com.auction.network;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionEventType;
import com.auction.backend.observer.AuctionObserver;
import com.auction.backend.observer.FrontendNotifier;
import com.auction.backend.observer.observers.BidEventPayload;
import com.auction.backend.observer.observers.BidderObserver;
import com.auction.backend.observer.observers.SellerObserver;
import com.auction.models.auction.Auction;
import com.auction.models.user.Bidder;
import com.auction.models.user.Seller;
import java.util.Locale;

/**
 * Định tuyến thông báo cá nhân tới đúng người dùng đang online.
 *
 * <p>Đây là lớp keo (glue) nối Observer pattern của backend với registry socket
 * của network: nó nhận mọi {@link AuctionEvent} từ publisher dùng chung rồi quyết
 * định gửi thông báo riêng tới:
 * <ul>
 *   <li>{@link SellerObserver} — chủ phiên đấu giá khi sản phẩm của họ có lượt
 *       trả giá mới hoặc khi phiên kết thúc.</li>
 *   <li>{@link BidderObserver} — người chiến thắng khi phiên kết thúc (kèm lời
 *       nhắc thanh toán).</li>
 * </ul>
 *
 * <p>Khác với {@link com.auction.backend.observer.observers.FrontendNotificationObserver}
 * (broadcast giá real-time tới TẤT CẢ client đang xem), observer này chỉ gửi tới
 * MỘT người nhận cụ thể, tra cứu socket qua {@link ClientHandler#getOnlineUser(String)}.
 * Người nhận offline sẽ được bỏ qua an toàn.
 */
public class PersonalNotificationObserver implements AuctionObserver {

  @Override
  public void update(AuctionEvent event) {
    if (event == null) {
      return;
    }

    Auction auction = extractAuction(event);
    if (auction == null) {
      return;
    }

    switch (event.getEventType()) {
      case NEW_BID:
      case AUTO_BID_PLACED:
        notifySeller(event, auction);
        break;

      case AUCTION_FINISHED:
        notifySeller(event, auction);
        notifyWinner(event, auction);
        break;

      default:
        break;
    }
  }

  /** Thông báo cho chủ phiên (seller) nếu họ đang online. */
  private void notifySeller(AuctionEvent event, Auction auction) {
    Seller seller = auction.getSeller();
    if (seller == null) {
      return;
    }

    FrontendNotifier handler = ClientHandler.getOnlineUser(seller.getUserId());
    if (handler == null) {
      return;
    }

    String itemName = itemNameOf(auction);
    String message;

    if (event.getEventType() == AuctionEventType.AUCTION_FINISHED) {
      Bidder winner = auction.getCurrentHighestBidder();
      message = winner != null
          ? "🏁 Phiên '" + itemName + "' đã kết thúc. Người thắng: "
              + winner.getName() + " (" + formatPrice(auction.getCurrentHighestBid()) + ")."
          : "🏁 Phiên '" + itemName + "' đã kết thúc — không có người mua.";
    } else {
      message = "🔔 Sản phẩm '" + itemName + "' của bạn vừa nhận giá "
          + formatPrice(auction.getCurrentHighestBid()) + ".";
    }

    new SellerObserver(seller.getName(), handler)
        .update(personalEvent(event, message));
  }

  /** Thông báo cho người chiến thắng (bidder) nếu họ đang online. */
  private void notifyWinner(AuctionEvent event, Auction auction) {
    Bidder winner = auction.getCurrentHighestBidder();
    if (winner == null) {
      return;
    }

    FrontendNotifier handler = ClientHandler.getOnlineUser(winner.getUserId());
    if (handler == null) {
      return;
    }

    String message = "🏆 Chúc mừng! Bạn đã thắng '" + itemNameOf(auction)
        + "' với giá " + formatPrice(auction.getCurrentHighestBid())
        + ". Vui lòng tiến hành thanh toán.";

    new BidderObserver(winner.getName(), handler)
        .update(personalEvent(event, message));
  }

  private AuctionEvent personalEvent(AuctionEvent source, String message) {
    return new AuctionEvent(
        source.getEventType(),
        source.getAuctionId(),
        message,
        null);
  }

  private Auction extractAuction(AuctionEvent event) {
    Object payload = event.getPayload();
    if (payload instanceof Auction auction) {
      return auction;
    }
    if (payload instanceof BidEventPayload bidPayload) {
      return bidPayload.getAuction();
    }
    return null;
  }

  private String itemNameOf(Auction auction) {
    return auction.getItem() != null ? auction.getItem().getName() : "sản phẩm";
  }

  private String formatPrice(double value) {
    return String.format(Locale.US, "%,.0f VNĐ", value);
  }
}
