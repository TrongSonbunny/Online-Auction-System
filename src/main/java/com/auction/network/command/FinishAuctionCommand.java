package com.auction.network.command;

import com.auction.models.auction.Auction;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;

/**
 * Command xử lý kết thúc auction.
 */
public class FinishAuctionCommand extends BaseClientCommand {

  /**
   * Constructor finish auction command.
   *
   * @param context command context
   */
  public FinishAuctionCommand(
      CommandContext context) {

    super(context);
  }

  /**
   * Kết thúc auction và cập nhật SQLite.
   *
   * @param message dữ liệu client gửi lên
   * @return auction sau khi kết thúc
   */
  @Override
  public Object execute(
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
}