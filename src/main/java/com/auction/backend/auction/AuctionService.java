package com.auction.backend.auction;

import com.auction.backend.util.IdGenerator;
import com.auction.models.auction.Auction;
import com.auction.models.item.AuctionItem;
import com.auction.models.user.Seller;

/**
 * Service xử lý logic auction.
 */
public class AuctionService {

  private final AuctionManager auctionManager;

  private final AuctionValidator
      auctionValidator;

  private final AuctionScheduler
      auctionScheduler;

  /**
   * Constructor auction service.
   */
  public AuctionService() {

    this.auctionManager =
        AuctionManager.getInstance();

    this.auctionValidator =
        new AuctionValidator();

    this.auctionScheduler =
        new AuctionScheduler();
  }

  /**
   * Tạo auction mới.
   *
   * @param seller seller tạo auction
   * @param item item đấu giá
   * @param startingPrice giá khởi điểm
   * @param durationSeconds thời gian đấu giá
   * @return Auction mới
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

    auctionManager.addAuction(auction);

    auctionScheduler.scheduleAuctionFinish(
        auction,
        durationSeconds);

    return auction;
  }

  /**
   * Hủy auction.
   *
   * @param auctionId mã auction
   */
  public void cancelAuction(
      String auctionId) {

    Auction auction =
        auctionManager.findAuction(
            auctionId);

    if (auction == null) {

      throw new IllegalArgumentException(
          "Không tìm thấy auction.");
    }

    auction.cancel();
  }

  /**
   * Finish auction thủ công.
   *
   * @param auctionId mã auction
   */
  public void finishAuction(
      String auctionId) {

    Auction auction =
        auctionManager.findAuction(
            auctionId);

    if (auction == null) {

      throw new IllegalArgumentException(
          "Không tìm thấy auction.");
    }

    auction.finish();
  }

  /**
   * Validate seller.
   *
   * @param seller seller
   */
  private void validateSeller(
      Seller seller) {

    if (seller == null) {

      throw new IllegalArgumentException(
          "Seller không được null.");
    }

    if (!seller.canCreateAuction()) {

      throw new IllegalStateException(
          "Seller không có quyền tạo auction.");
    }
  }

  /**
   * Lấy auction manager.
   *
   * @return AuctionManager
   */
  public AuctionManager getAuctionManager() {
    return auctionManager;
  }
}