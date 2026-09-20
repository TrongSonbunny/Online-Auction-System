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
import com.auction.backend.observer.observers.AdminObserver;
import com.auction.backend.observer.observers.DataPersistenceObserver;
import com.auction.backend.payment.PaymentLogger;
import com.auction.backend.payment.PaymentProcessor;
import com.auction.backend.payment.PaymentValidator;
import com.auction.network.PersonalNotificationObserver;

/**
 * Gom các service, manager, DAO và observer dùng chung cho command.
 *
 * <p>Context này đảm bảo toàn bộ service dùng chung dependency, đặc biệt là
 * {@link AuctionScheduler} và {@link AuctionEventPublisher}.
 */
public class CommandContext {

  /**
   * Instance dùng chung được tạo bởi {@link ClientCommandFactory}. Cho phép tầng
   * network ({@link com.auction.network.ClientHandler}) lấy đúng publisher đang
   * được toàn bộ command sử dụng, thay vì tạo một publisher rời rạc.
   */
  private static volatile CommandContext shared;

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

  private final PaymentProcessor paymentProcessor;

  private final PaymentValidator paymentValidator;

  private final PaymentLogger paymentLogger;

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

    // 1) Persistence trước (lưu DB), 2) Admin audit log, 3) định tuyến thông báo
    // cá nhân tới seller/winner. Observer broadcast real-time (Frontend) được
    // mỗi ClientHandler tự đăng ký khi kết nối.
    this.eventPublisher.addObserver(
        new DataPersistenceObserver(
            itemDao,
            auctionDao,
            bidDao));

    this.eventPublisher.addObserver(
        new AdminObserver("SYSTEM-AUDIT"));

    this.eventPublisher.addObserver(
        new PersonalNotificationObserver());

    this.paymentProcessor =
        new PaymentProcessor();

    this.paymentValidator =
        new PaymentValidator();

    this.paymentLogger =
        new PaymentLogger();

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

    shared = this;
  }

  /**
   * Lấy context dùng chung mà toàn bộ command đang sử dụng.
   *
   * @return shared context, hoặc {@code null} nếu chưa được khởi tạo
   */
  public static CommandContext getShared() {
    return shared;
  }

  /**
   * Lấy payment processor.
   *
   * @return payment processor
   */
  public PaymentProcessor getPaymentProcessor() {
    return paymentProcessor;
  }

  /**
   * Lấy payment validator.
   *
   * @return payment validator
   */
  public PaymentValidator getPaymentValidator() {
    return paymentValidator;
  }

  /**
   * Lấy payment logger.
   *
   * @return payment logger
   */
  public PaymentLogger getPaymentLogger() {
    return paymentLogger;
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