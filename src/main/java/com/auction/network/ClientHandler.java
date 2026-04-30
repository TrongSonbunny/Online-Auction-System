package com.auction.network;

import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xử lý vòng đời kết nối mạng của từng máy khách riêng biệt. Được thiết kế để chạy bên trong một
 * Luồng ảo (Virtual Thread) của Java 21.
 */
public class ClientHandler implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
  private final Socket clientSocket;

  /**
   * Khởi tạo một ClientHandler mới.
   *
   * @param socket kết nối TCP đang hoạt động tới máy khách
   */
  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    try {
      logger.info("Luồng ảo đang xử lý máy khách từ: {}", clientSocket.getRemoteSocketAddress());

      // Mã nguồn của Cột mốc 2 (Milestone 2) sẽ được đặt ở đây
      // (Bao gồm BufferedReader và PrintWriter để đọc/ghi chuỗi JSON)

    } catch (Exception e) {
      logger.error("Máy khách đã ngắt kết nối đột ngột: {}", e.getMessage(), e);
    } finally {
      // DỌN DẸP: Đóng socket một cách an toàn khi máy khách rời đi hoặc xảy ra lỗi
      try {
        if (clientSocket != null && !clientSocket.isClosed()) {
          logger.info("Đang đóng kết nối cho: {}", clientSocket.getRemoteSocketAddress());
          clientSocket.close();
        }
      } catch (Exception e) {
        logger.error("Không thể đóng kết nối socket một cách an toàn: {}", e.getMessage(), e);
      }
    }
  }
}
