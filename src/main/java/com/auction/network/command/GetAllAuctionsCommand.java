package com.auction.network.command;

import com.auction.network.ClientMessage;

/**
 * Command xử lý lấy toàn bộ auction.
 */
public class GetAllAuctionsCommand extends BaseClientCommand {

  /**
   * Constructor get all auctions command.
   *
   * @param context command context
   */
  public GetAllAuctionsCommand(
      CommandContext context) {

    super(context);
  }

  @Override
  public Object execute(
      ClientMessage message) {

    return auctionManager.getAllAuctions();
  }
}