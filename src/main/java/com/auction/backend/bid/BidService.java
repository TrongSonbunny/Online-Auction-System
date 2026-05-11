package com.auction.backend.bid;

import com.auction.backend.util.IdGenerator;
import com.auction.models.auction.Auction;
import com.auction.models.bid.BidTransaction;
import com.auction.models.user.Bidder;

/**
 * Service xử lý logic bid.
 */
public class BidService {

  private final BidValidator bidValidator;

  private final BidHistoryManager bidHistoryManager;

  /**
   * Constructor bid service.
   */
  public BidService() {
    this.bidValidator = new BidValidator();
    this.bidHistoryManager = new BidHistoryManager();
  }

  /**
   * Thực hiện đặt giá.
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

    synchronized (auction) {

      bidValidator.validateBid(
          auction,
          bidder,
          amount);

      auction.updateHighestBid(
          bidder,
          amount);

      bidder.incrementTotalBidsPlaced();

      BidTransaction transaction =
          createTransaction(
              bidder,
              auction.getAuctionId(),
              amount);

      bidHistoryManager.addTransaction(
          transaction);

      return transaction;
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