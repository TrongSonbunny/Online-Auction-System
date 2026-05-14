package com.auction.backend.auction;

import com.auction.backend.util.IdGenerator;
import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.item.AuctionItem;
import com.auction.models.user.Seller;

/**
 * Service điều phối luồng tạo, hủy và kết thúc auction.
 *
 * <p>Khi tạo auction, service sẽ validate seller, tạo auction mới, start auction,
 * lưu auction vào {@link AuctionManager} và lên lịch kết thúc tự động bằng
 * {@link AuctionScheduler}.
 */
public class AuctionService {

  private final AuctionManager auctionManager;

  private final AuctionValidator auctionValidator;

  private final AuctionScheduler auctionScheduler;

  /**
   * Constructor mặc định.
   */
  public AuctionService() {
    this(new AuctionScheduler());
  }

  /**
   * Constructor dùng chung scheduler từ bên ngoài.
   *
   * <p>Dùng constructor này khi cần để {@link AuctionService} và
   * {@link com.auction.backend.bid.BidService} dùng chung một scheduler,
   * đặc biệt khi có anti-snipe.
   *
   * @param auctionScheduler scheduler dùng để kết thúc/gia hạn auction
   */
  public AuctionService(
      AuctionScheduler auctionScheduler) {

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
      throw new AuctionClosedException(
          "Không tìm thấy auction.");
    }

    auction.cancel();
  }

  /**
   * Kết thúc auction thủ công.
   *
   * @param auctionId mã auction
   */
  public void finishAuction(
      String auctionId) {

    Auction auction =
        auctionManager.findAuction(
            auctionId);

    if (auction == null) {
      throw new AuctionClosedException(
          "Không tìm thấy auction.");
    }

    auction.finish();
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