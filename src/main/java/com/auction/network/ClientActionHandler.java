package com.auction.network;

import com.auction.exceptions.AuctionException;
import com.auction.network.command.ClientCommand;
import com.auction.network.command.ClientCommandFactory;
import java.util.Map;

/**
 * Xử lý action nhận từ client.
 */
public class ClientActionHandler {

  private final Map<ActionType, ClientCommand> commandMap;

  /**
   * Constructor action handler.
   */
  public ClientActionHandler() {

    this.commandMap = ClientCommandFactory.createDefaultCommands();
  }

  /**
   * Xử lý action từ client.
   *
   * @param clientMessage dữ liệu client gửi lên
   * @return kết quả xử lý
   */
  public Object doAction(
      ClientMessage clientMessage) {

    validateClientMessage(
        clientMessage);

    ClientCommand command = commandMap.get(
        clientMessage.getAction());

    if (command == null) {
      throw new AuctionException(
          "Action không hợp lệ.");
    }

    return command.execute(
        clientMessage);
  }

  /**
   * Validate client message.
   *
   * @param clientMessage dữ liệu client gửi lên
   */
  private void validateClientMessage(
      ClientMessage clientMessage) {

    if (clientMessage == null) {
      throw new AuctionException(
          "ClientMessage không được null.");
    }

    if (clientMessage.getAction() == null) {
      throw new AuctionException(
          "Action không được null.");
    }
  }
}