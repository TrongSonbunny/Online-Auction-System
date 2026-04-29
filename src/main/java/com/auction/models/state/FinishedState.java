package com.auction.models.state;

import com.auction.exceptions.AuctionClosedException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.Bid;

/**
 * Trạng thái đã kết thúc của Auction.
 */
public class FinishedState implements AuctionState {

  private static final String STATE_NAME = "FINISHED";

  @Override
  public void start(Auction auction)
      throws AuctionClosedException {
    throw new AuctionClosedException("Phiên đấu giá đã kết thúc.");
  }

  @Override
  public void placeBid(Auction auction, Bid bid)
      throws AuctionClosedException {
    throw new AuctionClosedException("Phiên đấu giá đã kết thúc.");
  }

  @Override
  public void end(Auction auction)
      throws AuctionClosedException {
    throw new AuctionClosedException("Phiên đấu giá đã kết thúc.");
  }

  @Override
  public String getStateName() {
    return STATE_NAME;
  }
}