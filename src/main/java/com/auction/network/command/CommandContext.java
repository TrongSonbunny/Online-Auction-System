package com.auction.network.command;

import com.auction.backend.auction.AuctionManager;
import com.auction.backend.auction.AuctionScheduler;
import com.auction.backend.auction.AuctionService;
import com.auction.backend.auth.AuthService;
import com.auction.backend.bid.AutoBidManager;
import com.auction.backend.bid.AutoBidService;
import com.auction.backend.bid.BidHistoryManager;
import com.auction.backend.bid.BidService;
import com.auction.backend.database.dao.AuctionDao;
import com.auction.backend.database.dao.BidDao;
import com.auction.backend.database.dao.ItemDao;
import com.auction.backend.database.dao.UserDao;
import com.auction.backend.observer.AuctionEventPublisher;
import com.auction.backend.observer.observers.DataPersistenceObserver;

/**
 * Gom các service, manager, DAO và observer dùng chung cho command.
 *
 * <p>Context này đảm bảo toàn bộ service dùng chung dependency, đặc biệt là
 * {@link AuctionScheduler} và {@link AuctionEventPublisher}.
 */
public class CommandContext {

  private final AuctionManager auctionManager;

  private final AuctionScheduler auctionScheduler;

  private final AuctionService auctionService;

  private final BidHistoryManager bidHistoryManager;

  private final AutoBidManager autoBidManager;

  private final AutoBidService autoBidService;

  private final BidService bidService;

  private final AuthService authService;

  private final UserDao userDao;

  private final ItemDao itemDao;

  private final AuctionDao auctionDao;

  private final BidDao bidDao;

  private final AuctionEventPublisher eventPublisher;

  /**
   * Constructor context mặc định.
   */
  public CommandContext() {

    this.userDao =
        new UserDao();

    this.itemDao =
        new ItemDao();

    this.auctionDao =
        new AuctionDao();

    this.bidDao =
        new BidDao();

    this.eventPublisher =
        new AuctionEventPublisher();

    this.eventPublisher.addObserver(
        new DataPersistenceObserver(
            itemDao,
            auctionDao,
            bidDao));

    this.auctionManager =
        AuctionManager.getInstance();

    this.auctionScheduler =
        new AuctionScheduler(
            eventPublisher);

    this.auctionService =
        new AuctionService(
            auctionScheduler,
            eventPublisher);

    this.bidHistoryManager =
        new BidHistoryManager();

    this.autoBidManager =
        new AutoBidManager();

    this.autoBidService =
        new AutoBidService(
            autoBidManager,
            bidHistoryManager);

    this.bidService =
        new BidService(
            bidHistoryManager,
            auctionScheduler,
            autoBidService,
            eventPublisher);

    this.authService =
        new AuthService(
            userDao);
  }

  /**
   * Lấy auction manager.
   *
   * @return auction manager
   */
  public AuctionManager getAuctionManager() {
    return auctionManager;
  }

  /**
   * Lấy auction scheduler.
   *
   * @return auction scheduler
   */
  public AuctionScheduler getAuctionScheduler() {
    return auctionScheduler;
  }

  /**
   * Lấy auction service.
   *
   * @return auction service
   */
  public AuctionService getAuctionService() {
    return auctionService;
  }

  /**
   * Lấy bid history manager.
   *
   * @return bid history manager
   */
  public BidHistoryManager getBidHistoryManager() {
    return bidHistoryManager;
  }

  /**
   * Lấy auto-bid manager.
   *
   * @return auto-bid manager
   */
  public AutoBidManager getAutoBidManager() {
    return autoBidManager;
  }

  /**
   * Lấy auto-bid service.
   *
   * @return auto-bid service
   */
  public AutoBidService getAutoBidService() {
    return autoBidService;
  }

  /**
   * Lấy bid service.
   *
   * @return bid service
   */
  public BidService getBidService() {
    return bidService;
  }

  /**
   * Lấy auth service.
   *
   * @return auth service
   */
  public AuthService getAuthService() {
    return authService;
  }

  /**
   * Lấy user DAO.
   *
   * @return user DAO
   */
  public UserDao getUserDao() {
    return userDao;
  }

  /**
   * Lấy item DAO.
   *
   * @return item DAO
   */
  public ItemDao getItemDao() {
    return itemDao;
  }

  /**
   * Lấy auction DAO.
   *
   * @return auction DAO
   */
  public AuctionDao getAuctionDao() {
    return auctionDao;
  }

  /**
   * Lấy bid DAO.
   *
   * @return bid DAO
   */
  public BidDao getBidDao() {
    return bidDao;
  }

  /**
   * Lấy event publisher.
   *
   * @return event publisher
   */
  public AuctionEventPublisher getEventPublisher() {
    return eventPublisher;
  }
}