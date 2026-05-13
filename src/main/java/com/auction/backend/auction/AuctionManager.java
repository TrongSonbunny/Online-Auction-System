package com.auction.backend.auction;

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
import com.auction.network.ClientMessage;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý auction trong RAM và điều phối action từ client.
 */
public class AuctionManager {

  private static AuctionManager instance;

  private final Map<String, Auction>
      auctionMap;

  private final AuctionService
      auctionService;

  private final BidService
      bidService;

  private final AuthService
      authService;

  private final UserDao
      userDao;

  private final ItemDao
      itemDao;

  private final AuctionDao
      auctionDao;

  private final BidDao
      bidDao;

  /**
   * Private constructor singleton.
   */
  private AuctionManager() {

    this.auctionMap =
        new ConcurrentHashMap<>();

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
   * Lấy instance singleton.
   *
   * @return AuctionManager
   */
  public static synchronized AuctionManager
      getInstance() {

    if (instance == null) {
      instance = new AuctionManager();
    }

    return instance;
  }

  /**
   * Xử lý action từ frontend.
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
        return getAllAuctions();

      default:
        throw new AuctionException(
            "Action không hợp lệ.");
    }
  }

  /**
   * Đăng nhập.
   *
   * @param message dữ liệu client
   * @return user đăng nhập
   */
  private User handleLogin(
      ClientMessage message) {

    return authService.login(message);
  }

  /**
   * Đăng ký.
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
        findAuction(
            message.getAuctionId());

    if (auction == null) {
      throw new AuctionException(
          "Không tìm thấy auction.");
    }

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

    itemDao.saveItem(
        item);

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
   * @return auction sau khi finish
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
   * Thêm auction.
   *
   * @param auction auction cần thêm
   */
  public void addAuction(
      Auction auction) {

    if (auction == null) {

      throw new AuctionException(
          "Auction không được null.");
    }

    auctionMap.put(
        auction.getAuctionId(),
        auction);
  }

  /**
   * Xóa auction.
   *
   * @param auctionId mã auction
   */
  public void removeAuction(
      String auctionId) {

    auctionMap.remove(auctionId);
  }

  /**
   * Tìm auction theo id.
   *
   * @param auctionId mã auction
   * @return Auction tìm được
   */
  public Auction findAuction(
      String auctionId) {

    return auctionMap.get(auctionId);
  }

  /**
   * Lấy toàn bộ auction.
   *
   * @return collection immutable
   */
  public Collection<Auction>
      getAllAuctions() {

    return Collections.unmodifiableCollection(
        auctionMap.values());
  }

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

  private Auction getRequiredAuction(
      String auctionId) {

    Auction auction =
        findAuction(auctionId);

    if (auction == null) {
      throw new AuctionException(
          "Không tìm thấy auction.");
    }

    return auction;
  }

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