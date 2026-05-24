package com.auction.network.command;

import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;

/**
 * Command xử lý bắt đầu phiên đấu giá (chuyển từ PENDING sang ACTIVE).
 */
public class StartAuctionCommand extends BaseClientCommand {

  /**
   * Constructor start auction command.
   *
   * @param context command context
   */
  public StartAuctionCommand(CommandContext context) {
    super(context);
  }

  /**
   * Bắt đầu phiên đấu giá.
   *
   * @param message dữ liệu client gửi lên
   * @return auction sau khi bắt đầu
   */
  @Override
  public Object execute(ClientMessage message) {
    User user = getRequiredUser(message.getUserId());
    Auction auction = getRequiredAuction(message.getAuctionId());

    validateCanManageAuction(user, auction);

    if (!"PENDING".equals(auction.getStatus().name())) {
      throw new AuctionException("Phiên đấu giá này đã được mở hoặc đã kết thúc.");
    }

    // Chuyển giao cho AuctionService xử lý logic đổi trạng thái và kích hoạt Timer
    return auctionService.startAuction(message.getAuctionId());
  }
}