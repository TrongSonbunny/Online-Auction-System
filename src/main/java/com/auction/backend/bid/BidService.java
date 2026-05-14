package com.auction.backend.bid;

import com.auction.backend.auction.AuctionScheduler;
import com.auction.backend.util.IdGenerator;
import com.auction.models.auction.Auction;
import com.auction.models.auction.AuctionRules;
import com.auction.models.bid.BidTransaction;
import com.auction.models.user.Bidder;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service xử lý logic bid, tích hợp anti-snipe và auto-bid.
 */
public class BidService {

  private final BidValidator bidValidator;

  private final BidHistoryManager bidHistoryManager;

  private final AuctionScheduler auctionScheduler;

  private final AutoBidService autoBidService;

  /**
   * Constructor mặc định (không có anti-snipe và auto-bid).
   */
  public BidService() {

    this.bidValidator = new BidValidator();
    this.bidHistoryManager = new BidHistoryManager();
    this.auctionScheduler = null;
    this.autoBidService = null;
  }

  /**
   * Constructor đầy đủ với anti-snipe và auto-bid.
   *
   * <p>Lưu ý: {@code bidHistoryManager} phải là cùng instance được truyền
   * vào {@link AutoBidService} để lịch sử bid (cả manual lẫn auto) đồng nhất.
   *
   * @param bidHistoryManager manager lịch sử bid dùng chung
   * @param auctionScheduler scheduler để gia hạn auction khi anti-snipe
   * @param autoBidService service xử lý auto-bid
   */
  public BidService(
      BidHistoryManager bidHistoryManager,
      AuctionScheduler auctionScheduler,
      AutoBidService autoBidService) {

    this.bidValidator = new BidValidator();
    this.bidHistoryManager = bidHistoryManager;
    this.auctionScheduler = auctionScheduler;
    this.autoBidService = autoBidService;
  }

  /**
   * Thực hiện đặt giá, sau đó kích hoạt anti-snipe và auto-bid (nếu có).
   *
   * @param auction auction cần bid
   * @param bidder bidder thực hiện
   * @param amount số tiền bid
   * @return BidTransaction được tạo
   */
  public BidTransaction placeBid(
      Auction auction,
      Bidder bidder,
      double amount) {

    BidTransaction transaction;

    synchronized (auction) {

      bidValidator.validateBid(
          auction,
          bidder,
          amount);

      auction.updateHighestBid(
          bidder,
          amount);

      bidder.incrementTotalBidsPlaced();

      transaction =
          createTransaction(
              bidder,
              auction.getAuctionId(),
              amount);

      bidHistoryManager.addTransaction(
          transaction);
    }

    // Anti-snipe: kiểm tra ngoài synchronized để tránh giữ lock lâu
    checkAndApplyAntiSnipe(auction);

    // Auto-bid cascade: tự động trả giá thay các bidder đã đăng ký
    if (autoBidService != null) {
      autoBidService.processAutoBids(auction);
    }

    return transaction;
  }

  /**
   * Kiểm tra và áp dụng anti-snipe nếu bid đặt trong khoảng thời gian cuối.
   *
   * <p>Nếu bid đặt trong {@code ANTI_SNIPE_WINDOW_SECONDS} giây cuối,
   * tự động gia hạn thêm {@code ANTI_SNIPE_EXTENSION_SECONDS} giây.
   *
   * @param auction auction vừa nhận bid
   */
  private void checkAndApplyAntiSnipe(
      Auction auction) {

    if (auctionScheduler == null) {
      return;
    }

    LocalDateTime scheduledEnd =
        auction.getScheduledEndTime();

    if (scheduledEnd == null) {
      return;
    }

    long secondsRemaining =
        Duration
            .between(
                LocalDateTime.now(),
                scheduledEnd)
            .getSeconds();

    if (secondsRemaining >= 0
        && secondsRemaining
        <= AuctionRules.ANTI_SNIPE_WINDOW_SECONDS) {

      auctionScheduler.rescheduleAuctionFinish(
          auction,
          AuctionRules.ANTI_SNIPE_EXTENSION_SECONDS);
    }
  }

  /**
   * Tạo bid transaction.
   *
   * @param bidder bidder
   * @param auctionId auctionId
   * @param amount bid amount
   * @return BidTransaction mới
   */
  private BidTransaction createTransaction(
      Bidder bidder,
      String auctionId,
      double amount) {

    return new BidTransaction(
        IdGenerator.generateTransactionId(),
        bidder,
        auctionId,
        amount);
  }

  /**
   * Lấy manager lịch sử bid.
   *
   * @return BidHistoryManager
   */
  public BidHistoryManager getBidHistoryManager() {
    return bidHistoryManager;
  }
}