package com.auction.models.state;

import com.auction.exceptions.InvalidBidException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.Bid;


/**
 * Trạng thái đang diễn ra của Auction.
 */
public class RunningState implements AuctionState {

  private static final String STATE_NAME = "RUNNING";

  @Override
  public void start(Auction auction) {
  }

  @Override
  public void placeBid(Auction auction, Bid bid)
      throws InvalidBidException {

    double currentPrice = auction.getItem().getCurrentPrice();

    if (bid.getAmount() <= currentPrice) {
      throw new InvalidBidException(
          "Giá thầu phải cao hơn giá hiện tại: " + currentPrice);
    }

    auction.getBids().add(bid);
    auction.getItem().setCurrentPrice(bid.getAmount());
  }

  @Override
  public void end(Auction auction) {
    auction.setState(new FinishedState());
  }

  @Override
  public String getStateName() {
    return STATE_NAME;
  }
}