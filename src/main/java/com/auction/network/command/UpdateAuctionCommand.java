package com.auction.network.command;

import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import com.auction.models.item.ItemCategory;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;

/**
 * Command xử lý cập nhật thông tin auction khi chưa mở (PENDING / NOT OPEN).
 */
public class UpdateAuctionCommand extends BaseClientCommand {

  /**
   * Constructor update auction command.
   *
   * @param context command context
   */
  public UpdateAuctionCommand(CommandContext context) {
    super(context);
  }

  /**
   * Cập nhật thông tin auction.
   *
   * @param message dữ liệu client gửi lên
   * @return auction sau khi cập nhật
   */
  @Override
  public Object execute(ClientMessage message) {
    User user = getRequiredUser(message.getUserId());
    Auction auction = getRequiredAuction(message.getAuctionId());

    validateCanManageAuction(user, auction);

    if (!"PENDING".equals(auction.getStatus().name())) {
      throw new AuctionException(
          "Chỉ có thể cập nhật thông tin khi phiên đấu giá chưa mở (NOT OPEN).");
    }

    // Chuyển giao cho AuctionService xử lý nghiệp vụ cập nhật để đảm bảo tính đóng
    // gói (Encapsulation)
    return auctionService.updateAuction(
        message.getAuctionId(),
        message.getItemName(),
        message.getItemDescription(),
        ItemCategory.valueOf(message.getItemCategory()),
        message.getItemCondition(),
        message.getEstimatedPrice(),
        message.getStartingPrice(),
        message.getDurationSeconds());
  }
}