package com.auction.network.command;

import com.auction.models.auction.Auction;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;

/**
 * Command xử lý hủy auction.
 */
public class CancelAuctionCommand
    extends BaseClientCommand {

  /**
   * Constructor cancel auction command.
   *
   * @param context command context
   */
  public CancelAuctionCommand(
      CommandContext context) {

    super(context);
  }

  /**
   * Hủy auction.
   *
   * @param message dữ liệu client gửi lên
   * @return auction sau khi hủy
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

    return auctionService.cancelAuction(
        message.getAuctionId());
  }
}