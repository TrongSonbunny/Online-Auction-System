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
import com.auction.models.item.ItemCategory;
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
   * Khởi tạo AuctionService với dependency mặc định.
   */
  public AuctionService() {
    this(new AuctionScheduler(), new AuctionEventPublisher());
  }

  /**
   * Khởi tạo AuctionService cho phép inject scheduler và event publisher (dễ test
   * hơn).
   *
   * @param auctionScheduler Scheduler của auction
   * @param eventPublisher   Publisher để phát event
   */
  public AuctionService(AuctionScheduler auctionScheduler, AuctionEventPublisher eventPublisher) {
    if (auctionScheduler == null) {
      throw new AuctionException("AuctionScheduler không được null.");
    }
    this.auctionManager = AuctionManager.getInstance();
    this.auctionValidator = new AuctionValidator();
    this.auctionScheduler = auctionScheduler;
    this.eventPublisher = eventPublisher == null ? new AuctionEventPublisher() : eventPublisher;
  }

  /**
   * Tạo auction mới (Chỉ lưu ở trạng thái PENDING/NOT OPEN).
   */
  public Auction createAuction(
      Seller seller,
      AuctionItem item,
      double startingPrice,
      long durationSeconds) {

    validateSeller(seller);

    Auction auction = new Auction(
        IdGenerator.generateAuctionId(),
        seller,
        item,
        startingPrice);

    // ĐÃ FIX: Lưu trữ thời lượng nhưng KHÔNG gọi auction.start()
    auction.setDurationSeconds(durationSeconds);

    auctionValidator.validateAuctionCreation(auction);
    seller.incrementAuctionCreated();
    auctionManager.addAuction(auction);

    publishAuctionEvent(AuctionEventType.AUCTION_CREATED, auction, "Auction được tạo (Nháp).");

    return auction;
  }

  /**
   * Bắt đầu auction (OPEN).
   */
  public Auction startAuction(String auctionId) {
    Auction auction = getRequiredAuction(auctionId);

    // Đổi trạng thái thành ACTIVE và set startTime
    auction.start();

    // ĐÃ FIX: Thiết lập thời gian kết thúc = startTime + duration
    long duration = auction.getDurationSeconds();
    auction.setScheduledEndTime(auction.getStartTime().plusSeconds(duration));
    auctionScheduler.scheduleAuctionFinish(auction, duration);

    publishAuctionEvent(
        AuctionEventType.AUCTION_STARTED, auction,
        "Auction đã chính thức bắt đầu.");

    return auction;
  }

  /**
   * Cập nhật thông số auction.
   */
  public Auction updateAuction(
      String auctionId,
      String itemName,
      String itemDescription,
      ItemCategory category,
      String itemCondition,
      double estimatedPrice,
      double startingPrice,
      long durationSeconds) {

    Auction auction = getRequiredAuction(auctionId);

    auction.setStartingPrice(startingPrice);
    auction.setDurationSeconds(durationSeconds);

    AuctionItem item = auction.getItem();
    if (item != null) {
      item.setName(itemName);
      item.setDescription(itemDescription);
      item.setCategory(category);
      item.setItemCondition(itemCondition);
      item.setEstimatedPrice(estimatedPrice);
    }

    publishAuctionEvent(
        AuctionEventType.AUCTION_UPDATED,
        auction, "Auction đã được cập nhật chi tiết.");
    return auction;
  }

  /**
   * Hủy auction.
   */
  public Auction cancelAuction(String auctionId) {
    Auction auction = getRequiredAuction(auctionId);
    auction.cancel();
    publishAuctionEvent(AuctionEventType.AUCTION_CANCELLED, auction, "Auction đã bị hủy.");
    return auction;
  }

  /**
   * Kết thúc auction thủ công.
   */
  public Auction finishAuction(String auctionId) {
    Auction auction = getRequiredAuction(auctionId);
    auction.finish();
    publishAuctionEvent(AuctionEventType.AUCTION_FINISHED, auction, "Auction đã kết thúc.");
    return auction;
  }

  public AuctionManager getAuctionManager() {
    return auctionManager;
  }

  private Auction getRequiredAuction(String auctionId) {
    Auction auction = auctionManager.findAuction(auctionId);
    if (auction == null) {
      throw new AuctionClosedException("Không tìm thấy auction.");
    }
    return auction;
  }

  private void publishAuctionEvent(AuctionEventType eventType, Auction auction, String message) {
    eventPublisher.publishEvent(
        new AuctionEvent(
            eventType,
            auction.getAuctionId(),
            message, auction));
  }

  private void validateSeller(Seller seller) {
    if (seller == null) {
      throw new AuctionException("Seller không được null.");
    }
    if (!seller.canCreateAuction()) {
      throw new UnauthorizedException("Seller không có quyền tạo auction.");
    }
  }
}