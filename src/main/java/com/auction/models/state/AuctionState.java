package com.auction.models.state;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.Bid;

/**
 * State cho các trạng thái của Auction.
 */
public interface AuctionState {

  void start(Auction auction) throws AuctionClosedException;

  void placeBid(Auction auction, Bid bid)
      throws InvalidBidException, AuctionClosedException;

  void end(Auction auction) throws AuctionClosedException;

  String getStateName();
}