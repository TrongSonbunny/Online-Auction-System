package com.auction.backend.observer.observers;

import com.auction.backend.database.dao.AuctionDao;
import com.auction.backend.database.dao.BidDao;
import com.auction.backend.database.dao.ItemDao;
import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionEventType;
import com.auction.backend.observer.AuctionObserver;
import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import java.util.Objects;

/**
 * Observer chịu trách nhiệm lưu thay đổi nghiệp vụ xuống SQLite.
 *
 * <p>Command chỉ gọi service. Service publish event. Observer này nhận event
 * và quyết định thao tác persistence tương ứng:
 * <ul>
 *   <li>Auction created: lưu item và auction.
 *   <li>Auction changed: cập nhật auction.
 *   <li>Bid placed: lưu bid transaction và cập nhật auction.
 * </ul>
 */
public class DataPersistenceObserver
    implements AuctionObserver {

  private final ItemDao itemDao;

  private final AuctionDao auctionDao;

  private final BidDao bidDao;

  /**
   * Constructor data persistence observer.
   *
   * @param itemDao DAO xử lý item
   * @param auctionDao DAO xử lý auction
   * @param bidDao DAO xử lý bid transaction
   */
  public DataPersistenceObserver(
      ItemDao itemDao,
      AuctionDao auctionDao,
      BidDao bidDao) {

    this.itemDao =
        Objects.requireNonNull(
            itemDao,
            "ItemDao không được null.");

    this.auctionDao =
        Objects.requireNonNull(
            auctionDao,
            "AuctionDao không được null.");

    this.bidDao =
        Objects.requireNonNull(
            bidDao,
            "BidDao không được null.");
  }

  /**
   * Nhận event và lưu dữ liệu xuống SQLite.
   *
   * @param event event được publish
   */
  @Override
  public void update(
      AuctionEvent event) {

    if (event == null) {
      return;
    }

    AuctionEventType eventType =
        event.getEventType();

    switch (eventType) {
      case AUCTION_CREATED:
        saveCreatedAuction(event);
        break;

      case AUCTION_FINISHED:
      case AUCTION_CANCELLED:
      case AUCTION_EXTENDED:
        updateAuction(event);
        break;

      case NEW_BID:
      case AUTO_BID_PLACED:
        saveBidAndUpdateAuction(event);
        break;

      case AUTO_BID_REGISTERED:
      case AUTO_BID_CANCELLED:
      case AUCTION_STARTED:
      default:
        break;
    }
  }

  /**
   * Lưu item và auction khi auction được tạo.
   *
   * @param event auction created event
   */
  private void saveCreatedAuction(
      AuctionEvent event) {

    Auction auction =
        extractAuction(event);

    itemDao.saveItem(
        auction.getItem());

    auctionDao.saveAuction(
        auction);
  }

  /**
   * Cập nhật auction khi trạng thái/thời gian/giá thay đổi.
   *
   * @param event auction event
   */
  private void updateAuction(
      AuctionEvent event) {

    auctionDao.updateAuction(
        extractAuction(event));
  }

  /**
   * Lưu bid transaction và cập nhật auction hiện tại.
   *
   * @param event bid event
   */
  private void saveBidAndUpdateAuction(
      AuctionEvent event) {

    BidEventPayload payload =
        extractBidPayload(event);

    bidDao.saveBidTransaction(
        payload.getTransaction());

    auctionDao.updateAuction(
        payload.getAuction());
  }

  /**
   * Lấy auction từ payload.
   *
   * @param event event chứa auction
   * @return auction
   */
  private Auction extractAuction(
      AuctionEvent event) {

    Object payload =
        event.getPayload();

    if (!(payload instanceof Auction)) {
      throw new AuctionException(
          "Payload phải là Auction.");
    }

    return (Auction) payload;
  }

  /**
   * Lấy bid payload từ event.
   *
   * @param event event chứa bid payload
   * @return bid payload
   */
  private BidEventPayload extractBidPayload(
      AuctionEvent event) {

    Object payload =
        event.getPayload();

    if (!(payload instanceof BidEventPayload)) {
      throw new AuctionException(
          "Payload phải là BidEventPayload.");
    }

    return (BidEventPayload) payload;
  }
}