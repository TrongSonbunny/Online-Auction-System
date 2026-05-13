package com.auction.network.command;

import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemCategory;
import com.auction.models.item.ItemFactory;
import com.auction.models.user.Seller;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;

/**
 * Command xử lý tạo auction.
 */
public class CreateAuctionCommand extends BaseClientCommand {

  /**
   * Constructor create auction command.
   *
   * @param context command context
   */
  public CreateAuctionCommand(
      CommandContext context) {

    super(context);
  }

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

    AuctionItem item =
        ItemFactory.createItem(
            message.getItemName(),
            message.getItemDescription(),
            ItemCategory.valueOf(
                message.getItemCategory()),
            message.getItemCondition(),
            message.getEstimatedPrice());

    itemDao.saveItem(item);

    Auction auction =
        auctionService.createAuction(
            (Seller) user,
            item,
            message.getStartingPrice(),
            message.getDurationSeconds());

    auctionDao.saveAuction(
        auction);

    return auction;
  }
}