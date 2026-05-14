package com.auction.network.command;

import com.auction.backend.auction.AuctionManager;
import com.auction.backend.auction.AuctionService;
import com.auction.backend.auth.AuthService;
import com.auction.backend.bid.AutoBidService;
import com.auction.backend.bid.BidService;
import com.auction.backend.database.dao.UserDao;
import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.user.Admin;
import com.auction.models.user.Seller;
import com.auction.models.user.User;

/**
 * Base class cho các command xử lý request từ client.
 */
public abstract class BaseClientCommand
    implements ClientCommand {

  protected final CommandContext context;

  protected final AuctionManager auctionManager;

  protected final AuctionService auctionService;

  protected final BidService bidService;

  protected final AutoBidService autoBidService;

  protected final AuthService authService;

  protected final UserDao userDao;

  /**
   * Constructor base command.
   *
   * @param context command context
   */
  protected BaseClientCommand(
      CommandContext context) {

    this.context = context;
    this.auctionManager = context.getAuctionManager();
    this.auctionService = context.getAuctionService();
    this.bidService = context.getBidService();
    this.autoBidService = context.getAutoBidService();
    this.authService = context.getAuthService();
    this.userDao = context.getUserDao();
  }

  /**
   * Lấy user bắt buộc tồn tại.
   *
   * @param userId mã user
   * @return user
   */
  protected User getRequiredUser(
      String userId) {

    User user =
        userDao.findById(
            userId);

    if (user == null) {
      throw new UnauthorizedException(
          "Không tìm thấy user.");
    }

    return user;
  }

  /**
   * Lấy auction bắt buộc tồn tại trong RAM.
   *
   * @param auctionId mã auction
   * @return auction
   */
  protected Auction getRequiredAuction(
      String auctionId) {

    Auction auction =
        auctionManager.findAuction(
            auctionId);

    if (auction == null) {
      throw new AuctionClosedException(
          "Không tìm thấy auction.");
    }

    return auction;
  }

  /**
   * Kiểm tra user có quyền quản lý auction hay không.
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
        && auction.getSeller().getUserId()
            .equals(user.getUserId())) {
      return;
    }

    throw new UnauthorizedException(
        "User không có quyền quản lý auction này.");
  }
}