package com.auction.backend.bid;

import com.auction.backend.auction.AuctionScheduler;
import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionEventPublisher;
import com.auction.backend.observer.AuctionEventType;
import com.auction.backend.observer.observers.BidEventPayload;
import com.auction.backend.util.IdGenerator;
import com.auction.models.auction.Auction;
import com.auction.models.auction.AuctionRules;
import com.auction.models.bid.BidTransaction;
import com.auction.models.user.Bidder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Service xử lý logic bid, tích hợp anti-snipe, auto-bid và event publishing.
 */
public class BidService {

  private final BidValidator bidValidator;

  private final BidHistoryManager bidHistoryManager;

  private final AuctionScheduler auctionScheduler;

  private final AutoBidService autoBidService;

  private final AuctionEventPublisher eventPublisher;

  /**
   * Constructor mặc định.
   */
  public BidService() {

    this.bidValidator =
        new BidValidator();

    this.bidHistoryManager =
        new BidHistoryManager();

    this.auctionScheduler =
        null;

    this.autoBidService =
        null;

    this.eventPublisher =
        new AuctionEventPublisher();
  }

  /**
   * Constructor đầy đủ với anti-snipe, auto-bid và event publisher.
   *
   * @param bidHistoryManager manager lịch sử bid dùng chung
   * @param auctionScheduler scheduler để gia hạn auction
   * @param autoBidService service xử lý auto-bid
   * @param eventPublisher publisher phát event
   */
  public BidService(
      BidHistoryManager bidHistoryManager,
      AuctionScheduler auctionScheduler,
      AutoBidService autoBidService,
      AuctionEventPublisher eventPublisher) {

    this.bidValidator =
        new BidValidator();

    this.bidHistoryManager =
        bidHistoryManager;

    this.auctionScheduler =
        auctionScheduler;

    this.autoBidService =
        autoBidService;

    this.eventPublisher =
        eventPublisher == null
            ? new AuctionEventPublisher()
            : eventPublisher;
  }

  /**
   * Thực hiện đặt giá, sau đó kích hoạt anti-snipe và auto-bid nếu có.
   *
   * @param auction auction cần bid
   * @param bidder bidder thực hiện
   * @param amount số tiền bid
   * @return kết quả gồm manual bid và auto-bid phát sinh
   */
  public BidResult placeBid(
      Auction auction,
      Bidder bidder,
      double amount) {

    BidTransaction manualTransaction;
    final Bidder previousHighestBidder;

    auction.getLock().lock();
    try {

      bidValidator.validateBid(
          auction,
          bidder,
          amount);

      previousHighestBidder =
          auction.getCurrentHighestBidder();

      auction.updateHighestBid(
          bidder,
          amount);

      bidder.incrementTotalBidsPlaced();

      manualTransaction =
          createTransaction(
              bidder,
              auction.getAuctionId(),
              amount);

      bidHistoryManager.addTransaction(
          manualTransaction);

    } finally {
      auction.getLock().unlock();
    }

    publishBidEvent(
        AuctionEventType.NEW_BID,
        auction,
        manualTransaction,
        "Có bid mới.");

    checkAndApplyAntiSnipe(
        auction);

    List<BidTransaction> autoBidTransactions =
        processAutoBids(
            auction,
            previousHighestBidder);

    return new BidResult(
        manualTransaction,
        autoBidTransactions);
  }

  /**
   * Xử lý auto-bid và publish event cho từng auto-bid transaction.
   *
   * @param auction auction cần xử lý
   * @return danh sách auto-bid transaction
   */
  private List<BidTransaction> processAutoBids(
      Auction auction,
      Bidder previousHighestBidder) {

    if (autoBidService == null) {
      return Collections.emptyList();
    }

    List<BidTransaction> transactions =
        autoBidService.processAutoBids(
            auction,
            previousHighestBidder);

    for (BidTransaction transaction : transactions) {
      publishBidEvent(
          AuctionEventType.AUTO_BID_PLACED,
          auction,
          transaction,
          "Auto-bid được đặt.");
    }

    return transactions;
  }

  /**
   * Kiểm tra và áp dụng anti-snipe nếu bid đặt trong khoảng thời gian cuối.
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
   * Publish bid event.
   *
   * @param eventType loại event
   * @param auction auction liên quan
   * @param transaction bid transaction
   * @param message nội dung event
   */
  private void publishBidEvent(
      AuctionEventType eventType,
      Auction auction,
      BidTransaction transaction,
      String message) {

    eventPublisher.publishEvent(
        new AuctionEvent(
            eventType,
            auction.getAuctionId(),
            message,
            new BidEventPayload(
                auction,
                transaction)));
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