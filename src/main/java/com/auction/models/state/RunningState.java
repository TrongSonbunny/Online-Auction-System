package com.auction.models.state;

import com.auction.models.auction.Auction;
import com.auction.models.bid.Bid;

/**
 * Trạng thái đang diễn ra của Auction.
 */
public class RunningState implements AuctionState {
  @Override
  public void start(Auction auction) {
    System.out.println("Phiên đã đang chạy rồi.");
  }

  @Override
  public void placeBid(Auction auction, Bid bid) {
    double currentPrice = auction.getItem().getCurrentPrice();
    if (bid.getAmount() > currentPrice) {
      auction.getBids().add(bid);
      auction.getItem().setCurrentPrice(bid.getAmount());
      System.out.println("Đã nhận mức giá mới: " + bid.getAmount());
    } else {
      System.out.println("Giá thầu phải cao hơn giá hiện tại (" + currentPrice + ")");
    }
  }

  @Override
  public void end(Auction auction) {
    System.out.println("Kết thúc phiên đấu giá.");
    auction.setState(new FinishedState());
  }

  @Override
  public String getStateName() { 
    return "RUNNING"; 
  }
}
