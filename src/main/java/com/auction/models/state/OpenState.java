package com.auction.models.state;

import com.auction.models.auction.Auction;
import com.auction.models.bid.Bid;

/**
 * Trạng thái mở (chưa bắt đầu) của Auction.
 */
public class OpenState implements AuctionState {

  @Override
  public void start(Auction auction) {
    System.out.println("Bắt đầu phiên đấu giá...");
    auction.setState(new RunningState());
  }

  @Override
  public void placeBid(Auction auction, Bid bid) {
    System.out.println("Lỗi: Phiên đấu giá chưa bắt đầu!");
  }

  @Override
  public void end(Auction auction) {
    System.out.println("Phiên chưa chạy thì không thể kết thúc.");
  }

  @Override
  public String getStateName() { 
    return "OPEN"; 
  }
}