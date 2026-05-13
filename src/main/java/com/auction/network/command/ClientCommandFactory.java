package com.auction.network.command;

import com.auction.network.ActionType;
import java.util.EnumMap;
import java.util.Map;

/**
 * Factory tạo command theo action.
 */
public final class ClientCommandFactory {

  /**
   * Private constructor.
   */
  private ClientCommandFactory() {
  }

  /**
   * Tạo map command mặc định.
   *
   * @return map action và command
   */
  public static Map<ActionType, ClientCommand> createDefaultCommands() {

    CommandContext context =
        new CommandContext();

    Map<ActionType, ClientCommand> commands =
        new EnumMap<>(ActionType.class);

    commands.put(
        ActionType.LOGIN,
        new LoginCommand(context));

    commands.put(
        ActionType.REGISTER,
        new RegisterCommand(context));

    commands.put(
        ActionType.BID,
        new BidCommand(context));

    commands.put(
        ActionType.CREATE_AUCTION,
        new CreateAuctionCommand(context));

    commands.put(
        ActionType.CANCEL_AUCTION,
        new CancelAuctionCommand(context));

    commands.put(
        ActionType.FINISH_AUCTION,
        new FinishAuctionCommand(context));

    commands.put(
        ActionType.GET_ALL_AUCTIONS,
        new GetAllAuctionsCommand(context));

    return commands;
  }
}