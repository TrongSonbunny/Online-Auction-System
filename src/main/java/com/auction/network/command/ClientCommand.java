package com.auction.network.command;

import com.auction.network.ClientMessage;

/**
 * Command xử lý một action từ client.
 */
public interface ClientCommand {

  /**
   * Thực thi action.
   *
   * @param message dữ liệu client gửi lên
   * @return kết quả xử lý
   */
  Object execute(
      ClientMessage message);
}