package com.auction.network.command;

import com.auction.backend.util.IdGenerator;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.user.Seller;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;

/**
 * Command xử lý tạo auction.
 */
public class CreateAuctionCommand
    extends BaseClientCommand {

  /**
   * Constructor create auction command.
   *
   * @param context command context
   */
  public CreateAuctionCommand(
      CommandContext context) {

    super(context);
  }

  /**
   * Tạo auction mới.
   *
   * @param message dữ liệu client gửi lên
   * @return auction vừa tạo
   */
  @Override
  public Object execute(
      ClientMessage message) {

    User user =
        getRequiredUser(
            message.getUserId());

    if (!(user instanceof Seller)) {
      throw new UnauthorizedException(
          "Chỉ Seller mới được tạo auction.");
    }

    validateCreateAuctionMessage(
        message);

    AuctionItem item =
        new AuctionItem(
            IdGenerator.generateItemId(),
            message.getItemName(),
            message.getItemDescription(),
            ItemCategory.valueOf(
                message.getItemCategory()),
            message.getItemCondition(),
            message.getEstimatedPrice());

    Auction auction =
        auctionService.createAuction(
            (Seller) user,
            item,
            message.getStartingPrice(),
            message.getDurationSeconds());

    return auction;
  }

  /**
   * Validate dữ liệu tạo auction.
   *
   * @param message dữ liệu client gửi lên
   */
  private void validateCreateAuctionMessage(
      ClientMessage message) {

    if (message.getItemName() == null
        || message.getItemName().isBlank()) {

      throw new AuctionException(
          "Tên item không hợp lệ.");
    }

    if (message.getItemDescription() == null
        || message.getItemDescription().isBlank()) {

      throw new AuctionException(
          "Mô tả item không hợp lệ.");
    }

    if (message.getItemCategory() == null
        || message.getItemCategory().isBlank()) {

      throw new AuctionException(
          "Category không hợp lệ.");
    }

    if (message.getItemCondition() == null
        || message.getItemCondition().isBlank()) {

      throw new AuctionException(
          "Tình trạng item không hợp lệ.");
    }

    if (message.getEstimatedPrice() <= 0) {
      throw new AuctionException(
          "Giá ước tính phải lớn hơn 0.");
    }

    if (message.getStartingPrice() <= 0) {
      throw new AuctionException(
          "Giá khởi điểm phải lớn hơn 0.");
    }

    if (message.getDurationSeconds() <= 0) {
      throw new AuctionException(
          "Thời gian đấu giá phải lớn hơn 0.");
    }
  }
}