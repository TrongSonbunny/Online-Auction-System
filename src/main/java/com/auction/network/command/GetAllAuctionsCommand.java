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

  /**
   * Trả về danh sách toàn bộ auction đang tồn tại trong RAM.
   *
   * <p>Không yêu cầu xác thực user — bất kỳ client nào cũng có thể gọi.
   *
   * @param message dữ liệu client gửi lên (không dùng)
   * @return danh sách auction hiện có
   */
  @Override
  public Object execute(
      ClientMessage message) {

    return auctionManager.getAllAuctions();
  }
}