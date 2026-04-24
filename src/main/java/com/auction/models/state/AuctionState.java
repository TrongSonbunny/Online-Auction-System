package com.auction.models.state;

import com.auction.models.auction.Auction;
import com.auction.models.bid.Bid;

/**
 * State cho các trạng thái của Auction.
 */
public interface AuctionState {
  public void start(Auction auction);

  public void placeBid(Auction auction, Bid bid);
  
  public void end(Auction auction);
  
  String getStateName();
}
