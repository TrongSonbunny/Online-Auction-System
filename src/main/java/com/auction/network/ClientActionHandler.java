package com.auction.network;

import com.auction.exceptions.AuctionException;
import com.auction.network.command.ClientCommand;
import com.auction.network.command.ClientCommandFactory;
import java.util.Map;

/**
 * Xử lý action nhận từ client.
 *
 * <p>Class này dùng static method để có thể gọi trực tiếp mà không cần tạo
 * object ClientActionHandler.
 */
public final class ClientActionHandler {

  private static final Map<ActionType, ClientCommand> COMMAND_MAP =
      ClientCommandFactory.createDefaultCommands();

  /**
   * Private constructor.
   */
  private ClientActionHandler() {
  }

  /**
   * Buộc khởi tạo sớm command map (và do đó là shared {@link CommandContext} cùng
   * toàn bộ observer dùng chung). Gọi lúc server khởi động để đảm bảo publisher
   * đã sẵn sàng trước khi client đầu tiên kết nối và đăng ký observer real-time.
   *
   * @return số lượng command đã đăng ký
   */
  public static int warmUp() {
    return COMMAND_MAP.size();
  }

  /**
   * Xử lý action từ client.
   *
   * @param clientMessage dữ liệu client gửi lên
   * @return kết quả xử lý
   */
  public static Object doAction(
      ClientMessage clientMessage) {

    validateClientMessage(
        clientMessage);

    ClientCommand command =
        COMMAND_MAP.get(
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
  private static void validateClientMessage(
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