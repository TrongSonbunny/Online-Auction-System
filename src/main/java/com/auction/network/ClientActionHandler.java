package com.auction.network;

import com.auction.backend.auction.AuctionManager;
import com.auction.backend.auction.AuctionService;
import com.auction.backend.auth.AuthService;
import com.auction.backend.bid.BidService;
import com.auction.backend.database.dao.AuctionDao;
import com.auction.backend.database.dao.BidDao;
import com.auction.backend.database.dao.ItemDao;
import com.auction.backend.database.dao.UserDao;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.BidTransaction;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.item.ItemFactory;
import com.auction.models.user.Admin;
import com.auction.models.user.Bidder;
import com.auction.models.user.Seller;
import com.auction.models.user.User;
import java.util.Collection;

/**
 * Xử lý action nhận từ client.
 */
public class ClientActionHandler {

  private final AuctionManager auctionManager;

  private final AuctionService auctionService;

  private final BidService bidService;

  private final AuthService authService;

  private final UserDao userDao;

  private final ItemDao itemDao;

  private final AuctionDao auctionDao;

  private final BidDao bidDao;

  /**
   * Constructor action handler.
   */
  public ClientActionHandler() {

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

  /**
   * Xử lý action từ client.
   *
   * @param clientMessage dữ liệu client gửi lên
   * @return kết quả xử lý
   */
  public Object doAction(
      ClientMessage clientMessage) {

    validateClientMessage(clientMessage);

    switch (clientMessage.getAction()) {
      case LOGIN:
        return handleLogin(clientMessage);

      case REGISTER:
        return handleRegister(clientMessage);

      case BID:
        return handleBid(clientMessage);

      case CREATE_AUCTION:
        return handleCreateAuction(clientMessage);

      case CANCEL_AUCTION:
        return handleCancelAuction(clientMessage);

      case FINISH_AUCTION:
        return handleFinishAuction(clientMessage);

      case GET_ALL_AUCTIONS:
        return handleGetAllAuctions();

      default:
        throw new AuctionException(
            "Action không hợp lệ.");
    }
  }

  /**
   * Xử lý đăng nhập.
   *
   * @param message dữ liệu client
   * @return user đăng nhập thành công
   */
  private User handleLogin(
      ClientMessage message) {

    return authService.login(message);
  }

  /**
   * Xử lý đăng ký.
   *
   * @param message dữ liệu client
   * @return user vừa đăng ký
   */
  private User handleRegister(
      ClientMessage message) {

    return authService.register(message);
  }

  /**
   * Xử lý đặt giá.
   *
   * @param message dữ liệu client
   * @return bid transaction
   */
  private BidTransaction handleBid(
      ClientMessage message) {

    User user =
        getRequiredUser(
            message.getUserId());

    if (!(user instanceof Bidder)) {
      throw new UnauthorizedException(
          "Chỉ Bidder mới được đặt giá.");
    }

    Auction auction =
        getRequiredAuction(
            message.getAuctionId());

    BidTransaction transaction =
        bidService.placeBid(
            auction,
            (Bidder) user,
            message.getBidAmount());

    bidDao.saveBidTransaction(
        transaction);

    auctionDao.updateAuction(
        auction);

    return transaction;
  }

  /**
   * Xử lý tạo auction.
   *
   * @param message dữ liệu client
   * @return auction mới
   */
  private Auction handleCreateAuction(
      ClientMessage message) {

    User user =
        getRequiredUser(
            message.getUserId());

    if (!(user instanceof Seller)) {
      throw new UnauthorizedException(
          "Chỉ Seller mới được tạo auction.");
    }

    AuctionItem item =
        ItemFactory.createItem(
            message.getItemName(),
            message.getItemDescription(),
            ItemCategory.valueOf(
                message.getItemCategory()),
            message.getItemCondition(),
            message.getEstimatedPrice());

    itemDao.saveItem(item);

    Auction auction =
        auctionService.createAuction(
            (Seller) user,
            item,
            message.getStartingPrice(),
            message.getDurationSeconds());

    auctionDao.saveAuction(
        auction);

    return auction;
  }

  /**
   * Xử lý hủy auction.
   *
   * @param message dữ liệu client
   * @return auction sau khi hủy
   */
  private Auction handleCancelAuction(
      ClientMessage message) {

    User user =
        getRequiredUser(
            message.getUserId());

    Auction auction =
        getRequiredAuction(
            message.getAuctionId());

    validateCanManageAuction(
        user,
        auction);

    auctionService.cancelAuction(
        message.getAuctionId());

    auctionDao.updateAuction(
        auction);

    return auction;
  }

  /**
   * Xử lý kết thúc auction.
   *
   * @param message dữ liệu client
   * @return auction sau khi kết thúc
   */
  private Auction handleFinishAuction(
      ClientMessage message) {

    User user =
        getRequiredUser(
            message.getUserId());

    Auction auction =
        getRequiredAuction(
            message.getAuctionId());

    validateCanManageAuction(
        user,
        auction);

    auctionService.finishAuction(
        message.getAuctionId());

    auctionDao.updateAuction(
        auction);

    return auction;
  }

  /**
   * Lấy toàn bộ auction.
   *
   * @return danh sách auction
   */
  private Collection<Auction> handleGetAllAuctions() {

    return auctionManager.getAllAuctions();
  }

  /**
   * Validate client message.
   *
   * @param clientMessage dữ liệu client gửi lên
   */
  private void validateClientMessage(
      ClientMessage clientMessage) {

    if (clientMessage == null) {
      throw new AuctionException(
          "ClientMessage không được null.");
    }

    if (clientMessage.getAction() == null) {
      throw new AuctionException(
          "Action không được null.");
    }
  }

  /**
   * Lấy user bắt buộc phải tồn tại.
   *
   * @param userId mã user
   * @return user tìm được
   */
  private User getRequiredUser(
      String userId) {

    if (userId == null || userId.isBlank()) {
      throw new AuctionException(
          "UserId không hợp lệ.");
    }

    User user =
        userDao.findById(userId);

    if (user == null) {
      throw new AuctionException(
          "Không tìm thấy user.");
    }

    return user;
  }

  /**
   * Lấy auction bắt buộc phải tồn tại.
   *
   * @param auctionId mã auction
   * @return auction tìm được
   */
  private Auction getRequiredAuction(
      String auctionId) {

    if (auctionId == null || auctionId.isBlank()) {
      throw new AuctionException(
          "AuctionId không hợp lệ.");
    }

    Auction auction =
        auctionManager.findAuction(
            auctionId);

    if (auction == null) {
      throw new AuctionException(
          "Không tìm thấy auction.");
    }

    return auction;
  }

  /**
   * Kiểm tra user có quyền quản lý auction không.
   *
   * @param user user cần kiểm tra
   * @param auction auction cần quản lý
   */
  private void validateCanManageAuction(
      User user,
      Auction auction) {

    if (user instanceof Admin) {
      return;
    }

    if (user instanceof Seller
        && auction.getSeller()
            .getUserId()
            .equals(user.getUserId())) {
      return;
    }

    throw new UnauthorizedException(
        "User không có quyền quản lý auction này.");
  }
}