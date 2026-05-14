package com.auction.network.command;

import com.auction.backend.auction.AuctionManager;
import com.auction.backend.auction.AuctionService;
import com.auction.backend.auth.AuthService;
import com.auction.backend.bid.BidService;
import com.auction.backend.database.dao.AuctionDao;
import com.auction.backend.database.dao.BidDao;
import com.auction.backend.database.dao.ItemDao;
import com.auction.backend.database.dao.UserDao;

/**
 * Gom các service và DAO dùng chung cho command.
 */
public class CommandContext {

  private final AuctionManager auctionManager;

  private final AuctionService auctionService;

  private final BidService bidService;

  private final AuthService authService;

  private final UserDao userDao;

  private final ItemDao itemDao;

  private final AuctionDao auctionDao;

  private final BidDao bidDao;

  /**
   * Constructor context mặc định.
   */
  public CommandContext() {

    this.auctionManager =
        AuctionManager.getInstance();

    this.auctionService =
        new AuctionService();

    this.bidService =
        new BidService();

    this.authService =
        new AuthService();

    this.userDao =
        new UserDao();

    this.itemDao =
        new ItemDao();

    this.auctionDao =
        new AuctionDao();

    this.bidDao =
        new BidDao();
  }

  public AuctionManager getAuctionManager() {
    return auctionManager;
  }

  public AuctionService getAuctionService() {
    return auctionService;
  }

  public BidService getBidService() {
    return bidService;
  }

  public AuthService getAuthService() {
    return authService;
  }

  public UserDao getUserDao() {
    return userDao;
  }

  public ItemDao getItemDao() {
    return itemDao;
  }

  public AuctionDao getAuctionDao() {
    return auctionDao;
  }

  public BidDao getBidDao() {
    return bidDao;
  }
}