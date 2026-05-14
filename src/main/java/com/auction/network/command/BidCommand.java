package com.auction.network.command;

import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.BidTransaction;
import com.auction.models.user.Bidder;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;

/**
 * Command xử lý đặt giá.
 */
public class BidCommand extends BaseClientCommand {

  /**
   * Constructor bid command.
   *
   * @param context command context
   */
  public BidCommand(
      CommandContext context) {

    super(context);
  }

  /**
   * Thực hiện đặt giá, lưu bid transaction và cập nhật auction trong SQLite.
   *
   * @param message dữ liệu client gửi lên
   * @return bid transaction được tạo
   */
  @Override
  public Object execute(
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
}