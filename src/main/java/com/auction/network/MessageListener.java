package com.auction.network;

/**
 * Interface để giao diện người dùng (UI) đăng ký nhận dữ liệu từ server.
 */
public interface MessageListener {
  /**
   * Called when a message is received from the server.
   *
   * @param message The ServerMessage received from the server.
   */
  void onMessageReceived(ServerMessage message);
}