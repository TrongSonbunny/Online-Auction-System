package com.auction.network.command;

import com.auction.exceptions.BidException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.AutoBid;
import com.auction.models.user.Bidder;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;

/**
 * Command xử lý đăng ký auto-bid.
 */
public class RegisterAutoBidCommand
    extends BaseClientCommand {

  /**
   * Constructor register auto-bid command.
   *
   * @param context command context
   */
  public RegisterAutoBidCommand(
      CommandContext context) {

    super(context);
  }

  /**
   * Đăng ký auto-bid cho bidder.
   *
   * @param message dữ liệu client gửi lên
   * @return auto-bid vừa đăng ký
   */
  @Override
  public Object execute(
      ClientMessage message) {

    User user =
        getRequiredUser(
            message.getUserId());

    if (!(user instanceof Bidder)) {
      throw new UnauthorizedException(
          "Chỉ Bidder mới được đăng ký auto-bid.");
    }

    validateAutoBidMessage(
        message);

    Auction auction =
        getRequiredAuction(
            message.getAuctionId());

    AutoBid autoBid =
        autoBidService.registerAutoBid(
            auction,
            (Bidder) user,
            message.getMaxBid(),
            message.getIncrement());

    context.getEventPublisher()
        .publishEvent(
            new com.auction.backend.observer.AuctionEvent(
                com.auction.backend.observer.AuctionEventType
                    .AUTO_BID_REGISTERED,
                auction.getAuctionId(),
                "Auto-bid được đăng ký.",
                auction));

    return autoBid;
  }

  /**
   * Validate dữ liệu auto-bid.
   *
   * @param message dữ liệu client
   */
  private void validateAutoBidMessage(
      ClientMessage message) {

    if (message.getMaxBid() <= 0) {
      throw new BidException(
          "MaxBid phải lớn hơn 0.");
    }

    if (message.getIncrement() <= 0) {
      throw new BidException(
          "Increment phải lớn hơn 0.");
    }
  }
}