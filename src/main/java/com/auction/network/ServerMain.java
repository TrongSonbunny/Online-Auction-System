package com.auction.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Điểm bắt đầu chính của Máy chủ Đấu giá (TCP Server). Khởi tạo server socket và quản lý các kết
 * nối đến thông qua Luồng ảo (Virtual Threads).
 */
public class ServerMain {

  private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);
    
  private static final int PORT = 8080;

  // THE ROSTER: Thread-safe list of all active client "speaking pipes"
  public static final CopyOnWriteArrayList<PrintWriter> activeClients =
      new CopyOnWriteArrayList<>();

  /**
   * Phương thức main để khởi chạy máy chủ.
   *
   * @param args các tham số dòng lệnh (không sử dụng)
   */
  public static void main(String[] args) {
    logger.info("Đang khởi động Máy chủ Đấu giá trên cổng {}...", PORT);

    // Khởi tạo ServerSocket và Trình quản lý Luồng ảo
    try (ServerSocket server = new ServerSocket(PORT);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

      logger.info("Máy chủ đã hoạt động và đang chờ kết nối từ máy khách...");

      while (true) {
        try {
          Socket clientConnection = server.accept();
          logger.info("Máy khách mới đã kết nối từ: {}", clientConnection.getRemoteSocketAddress());

          // Chuyển giao máy khách cho một Luồng ảo xử lý
          executor.submit(new ClientHandler(clientConnection));

        } catch (IOException ex) {
          logger.error("Lỗi khi chấp nhận kết nối từ máy khách: {}", ex.getMessage(), ex);
        }
      }
    } catch (IOException ex) {
      logger.error("NGHIÊM TRỌNG: Không thể khởi động máy chủ trên cổng {}. Có thể cổng này đang "
          + "được sử dụng?", PORT, ex);
    }
  }
}
