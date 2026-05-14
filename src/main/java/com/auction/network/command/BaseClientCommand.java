package com.auction.network.command;

import com.auction.backend.auction.AuctionManager;
import com.auction.backend.auction.AuctionService;
import com.auction.backend.auth.AuthService;
import com.auction.backend.bid.AutoBidService;
import com.auction.backend.bid.BidService;
import com.auction.backend.database.dao.AuctionDao;
import com.auction.backend.database.dao.BidDao;
import com.auction.backend.database.dao.ItemDao;
import com.auction.backend.database.dao.UserDao;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.user.Admin;
import com.auction.models.user.Seller;
import com.auction.models.user.User;

/**
 * Base class cho các command xử lý ClientMessage.
 *
 * <p>Class này gom các dependency dùng chung và cung cấp helper để lấy user,
 * lấy auction, kiểm tra quyền quản lý auction.
 */
public abstract class BaseClientCommand
    implements ClientCommand {

  protected final AuctionManager auctionManager;

  protected final AuctionService auctionService;

  protected final BidService bidService;

  protected final AutoBidService autoBidService;

  protected final AuthService authService;

  protected final UserDao userDao;

  protected final ItemDao itemDao;

  protected final AuctionDao auctionDao;

  protected final BidDao bidDao;

  /**
   * Constructor base command.
   *
   * @param context command context
   */
  protected BaseClientCommand(
      CommandContext context) {

    this.auctionManager =
        context.getAuctionManager();

    this.auctionService =
        context.getAuctionService();

    this.bidService =
        context.getBidService();

    this.autoBidService =
        context.getAutoBidService();

    this.authService =
        context.getAuthService();

    this.userDao =
        context.getUserDao();

    this.itemDao =
        context.getItemDao();

    this.auctionDao =
        context.getAuctionDao();

    this.bidDao =
        context.getBidDao();
  }

  /**
   * Lấy user bắt buộc phải tồn tại.
   *
   * @param userId mã user
   * @return user tìm được
   */
  protected User getRequiredUser(
      String userId) {

    if (userId == null || userId.isBlank()) {
      throw new AuctionException(
          "UserId không hợp lệ.");
    }

    User user =
        userDao.findById(
            userId);

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
  protected Auction getRequiredAuction(
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
  protected void validateCanManageAuction(
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