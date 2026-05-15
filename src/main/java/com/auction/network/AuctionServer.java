package com.auction.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Lớp khởi chạy Backend Server, lắng nghe các kết nối từ Client qua Socket.
 */
public class AuctionServer {

  private static final int PORT = 8080;
  private static final ClientActionHandler ACTION_HANDLER = new ClientActionHandler();

  /**
   * Điểm neo (entry point) để chạy Server.
   *
   * @param args Tham số dòng lệnh
   */
  public static void main(String[] args) {

    // Khởi tạo cấu trúc bảng trong Database
    new com.auction.backend.database.DatabaseManager().initializeDatabase();

    System.out.println("Đang khởi động hệ thống Backend...");

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      System.out.println("=== SERVER ĐÃ SẴN SÀNG TẠI CỔNG " + PORT + " ===");

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Có Client mới kết nối từ IP: " + clientSocket.getInetAddress());

        new Thread(() -> handleClient(clientSocket)).start();
      }

    } catch (IOException e) {
      System.err.println("Lỗi khởi động Server: " + e.getMessage());
    }
  }

  /**
   * Phương thức xử lý luồng giao tiếp với một Client cụ thể.
   *
   * @param clientSocket Socket kết nối với Client
   */
  private static void handleClient(Socket clientSocket) {
    // Lưu ý: Luôn khởi tạo ObjectOutputStream trước InputStream để tránh Deadlock
    // mạng
    try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

      // Flush buffer ngay khi tạo OutputStream
      out.flush();

      while (true) {
        // 1. Đọc yêu cầu (ClientMessage) từ Frontend
        Object request = in.readObject();

        if (request instanceof ClientMessage) {
          ClientMessage clientMessage = (ClientMessage) request;

          try {
            // 2. Giao cho bộ xử lý trung tâm giải quyết
            Object response = ACTION_HANDLER.doAction(clientMessage);

            // 3. Nếu THÀNH CÔNG, gửi User/Auction về cho Frontend
            out.writeObject(response);
            out.flush();

          } catch (Exception ex) {
            // FIX LỖI Ở ĐÂY: Nếu THẤT BẠI (Trùng email, sai pass...),
            // Bắt buộc phải gửi cục lỗi này về cho Client để nó biết đường hiển thị!
            out.writeObject(ex);
            out.flush();
          }
        }
      }

    } catch (Exception e) {
      System.out.println("Client đã ngắt kết nối: " + clientSocket.getInetAddress());
    } finally {
      try {
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("Lỗi đóng Socket: " + e.getMessage());
      }
    }
  }
}