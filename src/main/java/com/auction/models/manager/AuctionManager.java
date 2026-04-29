package com.auction.models.manager;

import com.auction.models.auction.Auction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuctionManager {

  private static AuctionManager instance;

  private final List<Auction> auctions =
      Collections.synchronizedList(new ArrayList<>());

  private AuctionManager() {}

  public static AuctionManager getInstance() {
    if (instance == null) {
      instance = new AuctionManager();
    }
    return instance;
  }

  public void addAuction(Auction auction) {
    auctions.add(auction);
  }

  public List<Auction> getAuctions() {
    return auctions;
  }
}