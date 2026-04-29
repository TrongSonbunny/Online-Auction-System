package com.auction.models.state;

import com.auction.exceptions.AuctionClosedException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.Bid;

/**
 * Trạng thái mở (chưa bắt đầu) của Auction.
 */
public class OpenState implements AuctionState {

  private static final String STATE_NAME = "OPEN";

  @Override
  public void start(Auction auction) {
    auction.setState(new RunningState());
  }

  @Override
  public void placeBid(Auction auction, Bid bid)
      throws AuctionClosedException {
    throw new AuctionClosedException("Lỗi: Phiên đấu giá chưa bắt đầu!");
  }

  @Override
  public void end(Auction auction)
      throws AuctionClosedException {
    throw new AuctionClosedException("Phiên chưa chạy thì không thể kết thúc.");
  }

  @Override
  public String getStateName() {
    return STATE_NAME;
  }
}