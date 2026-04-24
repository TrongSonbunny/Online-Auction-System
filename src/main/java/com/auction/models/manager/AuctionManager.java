package com.auction.models.manager;

import com.auction.models.auction.Auction;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp quản lý danh sách các phiên đấu giá (Singleton).
 */
public class AuctionManager {
  private static AuctionManager instance;
  private List<Auction> auctions = new ArrayList<>();
  
  private AuctionManager() {
  }
  
  /**
   * Lấy instance duy nhất của AuctionManager.
   */
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