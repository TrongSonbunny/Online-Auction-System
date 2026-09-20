package com.auction.network;

import com.auction.backend.database.DatabaseManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Điểm bắt đầu chính của Máy chủ Đấu giá (TCP Server).
 * Khởi tạo server socket, quản lý các kết nối đến qua Luồng ảo (Virtual Threads),
 * và xử lý tắt server an toàn (Graceful Shutdown).
 */
public class ServerMain {

  private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);
  private static final DatabaseManager databaseManager = new DatabaseManager();
  private static final int PORT = 8080;

  // Cờ trạng thái để kiểm soát vòng lặp server
  private static volatile boolean isRunning = true;

  /**
   * Phương thức main để khởi chạy máy chủ.
   *
   * @param args các tham số dòng lệnh
   */
  public static void main(String[] args) {
    logger.info("Đang khởi động Máy chủ Đấu giá trên cổng {}...", PORT);

    // 1. Khởi tạo cơ sở dữ liệu và các core services (Fail-Fast)
    try {
      databaseManager.initializeDatabase();
      logger.info("Khởi tạo cơ sở dữ liệu thành công.");

      // Khởi tạo sớm command system + observer dùng chung (persistence, admin
      // audit, personal notifications) để publisher sẵn sàng trước khi có client.
      int commandCount = ClientActionHandler.warmUp();
      logger.info("Khởi tạo {} command và hệ thống observer thành công.", commandCount);
    } catch (Exception e) {
      logger.error("Lỗi khởi tạo hệ thống. Đang dừng server.", e);
      System.exit(1);
    }

    // 2. Đăng ký Hook để xử lý Graceful Shutdown khi ấn Ctrl+C
    registerShutdownHook();

    // 3. Khởi tạo ServerSocket và Trình quản lý Luồng ảo
    try (ServerSocket server = new ServerSocket(PORT);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

      logger.info("Máy chủ đã hoạt động và đang chờ kết nối từ máy khách...");

      while (isRunning) {
        try {
          Socket clientConnection = server.accept();
          logger.info("Máy khách kết nối từ: {}", clientConnection.getRemoteSocketAddress());

          // Chuyển giao máy khách cho một Luồng ảo xử lý thông qua ClientHandler
          executor.submit(new ClientHandler(clientConnection));

        } catch (IOException ex) {
          if (isRunning) {
            logger.error("Lỗi khi chấp nhận kết nối từ máy khách: {}", ex.getMessage());
          }
        }
      }
    } catch (IOException ex) {
      logger.error("NGHIÊM TRỌNG: Không thể khởi động máy chủ trên cổng {}.", PORT, ex);
    }
    
    logger.info("Máy chủ đã tắt hoàn toàn.");
  }

  /**
   * Đăng ký một luồng chạy ngầm để dọn dẹp tài nguyên khi JVM nhận tín hiệu tắt.
   */
  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Nhận tín hiệu tắt máy chủ. Đang tiến hành Graceful Shutdown...");
      isRunning = false;
      // TODO: Thêm logic lưu trạng thái bộ nhớ (RAM) xuống Database nếu cần thiết
    }));
  }
}