package com.auction.models.state;

import com.auction.models.auction.Auction;
import com.auction.models.bid.Bid;

/**
 * Trạng thái đã kết thúc của Auction.
 */
public class FinishedState implements AuctionState {
  @Override
  public void start(Auction auction) {}

  @Override
  public void placeBid(Auction auction, Bid bid) {
    System.out.println("Lỗi: Phiên đấu giá đã kết thúc.");
  }

  @Override
  public void end(Auction auction) {}

  @Override
  public String getStateName() { 
    return "FINISHED"; 
  }
}
