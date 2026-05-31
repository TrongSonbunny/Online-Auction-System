package com.auction.network.command;

import com.auction.exceptions.BidException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.user.Bidder;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;

/**
 * Command xử lý hủy auto-bid.
 */
public class CancelAutoBidCommand
    extends BaseClientCommand {

  /**
   * Constructor cancel auto-bid command.
   *
   * @param context command context
   */
  public CancelAutoBidCommand(
      CommandContext context) {

    super(context);
  }

  /**
   * Hủy auto-bid theo ID.
   *
   * @param message dữ liệu client gửi lên
   * @return true nếu hủy thành công
   */
  @Override
  public Object execute(
      ClientMessage message) {

    User user =
        getRequiredUser(
            message.getUserId());

    if (!(user instanceof Bidder)) {
      throw new UnauthorizedException(
          "Chỉ Bidder mới được hủy auto-bid.");
    }

    validateCancelAutoBidMessage(
        message);

    boolean cancelled =
        autoBidService.cancelAutoBid(
            message.getAutoBidId());

    if (!cancelled) {
      throw new BidException(
          "Không tìm thấy auto-bid cần hủy.");
    }

    return true;
  }

  /**
   * Validate dữ liệu hủy auto-bid.
   *
   * @param message dữ liệu client
   */
  private void validateCancelAutoBidMessage(
      ClientMessage message) {

    if (message.getAutoBidId() == null
        || message.getAutoBidId().isBlank()) {

      throw new BidException(
          "AutoBidId không hợp lệ.");
    }
  }
}