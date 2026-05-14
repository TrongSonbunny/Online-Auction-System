package com.auction.backend.auction;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionEventPublisher;
import com.auction.backend.observer.AuctionEventType;
import com.auction.backend.util.IdGenerator;
import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.item.AuctionItem;
import com.auction.models.user.Seller;

/**
 * Service điều phối luồng tạo, hủy và kết thúc auction.
 */
public class AuctionService {

  private final AuctionManager auctionManager;

  private final AuctionValidator auctionValidator;

  private final AuctionScheduler auctionScheduler;

  private final AuctionEventPublisher eventPublisher;

  /**
   * Constructor mặc định.
   */
  public AuctionService() {
    this(
        new AuctionScheduler(),
        new AuctionEventPublisher());
  }

  /**
   * Constructor dùng chung scheduler và event publisher từ bên ngoài.
   *
   * @param auctionScheduler scheduler dùng để kết thúc/gia hạn auction
   * @param eventPublisher publisher phát event
   */
  public AuctionService(
      AuctionScheduler auctionScheduler,
      AuctionEventPublisher eventPublisher) {

    if (auctionScheduler == null) {
      throw new AuctionException(
          "AuctionScheduler không được null.");
    }

    this.auctionManager =
        AuctionManager.getInstance();

    this.auctionValidator =
        new AuctionValidator();

    this.auctionScheduler =
        auctionScheduler;

    this.eventPublisher =
        eventPublisher == null
            ? new AuctionEventPublisher()
            : eventPublisher;
  }

  /**
   * Tạo auction mới.
   *
   * @param seller seller tạo auction
   * @param item item đấu giá
   * @param startingPrice giá khởi điểm
   * @param durationSeconds thời gian đấu giá
   * @return auction mới
   */
  public Auction createAuction(
      Seller seller,
      AuctionItem item,
      double startingPrice,
      long durationSeconds) {

    validateSeller(seller);

    Auction auction =
        new Auction(
            IdGenerator.generateAuctionId(),
            seller,
            item,
            startingPrice);

    auctionValidator.validateAuctionCreation(
        auction);

    seller.incrementAuctionCreated();

    auction.start();

    auctionManager.addAuction(
        auction);

    publishAuctionEvent(
        AuctionEventType.AUCTION_CREATED,
        auction,
        "Auction được tạo.");

    auctionScheduler.scheduleAuctionFinish(
        auction,
        durationSeconds);

    return auction;
  }

  /**
   * Hủy auction.
   *
   * @param auctionId mã auction
   * @return auction sau khi hủy
   */
  public Auction cancelAuction(
      String auctionId) {

    Auction auction =
        getRequiredAuction(
            auctionId);

    auction.cancel();

    publishAuctionEvent(
        AuctionEventType.AUCTION_CANCELLED,
        auction,
        "Auction đã bị hủy.");

    return auction;
  }

  /**
   * Kết thúc auction thủ công.
   *
   * @param auctionId mã auction
   * @return auction sau khi kết thúc
   */
  public Auction finishAuction(
      String auctionId) {

    Auction auction =
        getRequiredAuction(
            auctionId);

    auction.finish();

    publishAuctionEvent(
        AuctionEventType.AUCTION_FINISHED,
        auction,
        "Auction đã kết thúc.");

    return auction;
  }

  /**
   * Lấy auction manager.
   *
   * @return auction manager
   */
  public AuctionManager getAuctionManager() {
    return auctionManager;
  }

  /**
   * Lấy auction bắt buộc tồn tại.
   *
   * @param auctionId mã auction
   * @return auction
   */
  private Auction getRequiredAuction(
      String auctionId) {

    Auction auction =
        auctionManager.findAuction(
            auctionId);

    if (auction == null) {
      throw new AuctionClosedException(
          "Không tìm thấy auction.");
    }

    return auction;
  }

  /**
   * Publish auction event.
   *
   * @param eventType loại event
   * @param auction auction liên quan
   * @param message nội dung event
   */
  private void publishAuctionEvent(
      AuctionEventType eventType,
      Auction auction,
      String message) {

    eventPublisher.publishEvent(
        new AuctionEvent(
            eventType,
            auction.getAuctionId(),
            message,
            auction));
  }

  /**
   * Validate seller trước khi tạo auction.
   *
   * @param seller seller cần kiểm tra
   */
  private void validateSeller(
      Seller seller) {

    if (seller == null) {
      throw new AuctionException(
          "Seller không được null.");
    }

    if (!seller.canCreateAuction()) {
      throw new UnauthorizedException(
          "Seller không có quyền tạo auction.");
    }
  }
}