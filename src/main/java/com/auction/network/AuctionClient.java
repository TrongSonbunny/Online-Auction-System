package com.auction.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Máy khách (Console Test), dùng để kiểm tra đường truyền kết nối tới Server.
 * Lưu ý: Client chính thức của người dùng đang được chạy qua JavaFX
 * (PrimaryController).
 */
public class AuctionClient {

  private static final String SERVER_ADDRESS = "127.0.0.1";
  private static final int SERVER_PORT = 8080;

  /**
   * Khởi chạy Client để test.
   *
   * @param args Tham số dòng lệnh
   */
  public static void main(String[] args) {
    try (
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      System.out.println("Đã kết nối thành công tới Server!");

      // ĐÃ XÓA LOGIC TỰ ĐỘNG TẠO "DELL XPS 15" ĐỂ KHÔNG LÀM RÁC HỆ THỐNG

      // Chỉ đơn giản là lắng nghe phản hồi đầu tiên từ Server (Danh sách gốc)
      Object response = in.readObject();
      System.out.println("Nhận được cục dữ liệu đầu tiên từ Server kiểu: "
          + response.getClass().getSimpleName());

    } catch (IOException | ClassNotFoundException e) {
      System.err.println("Không thể kết nối đến máy chủ: " + e.getMessage());
    }
  }
}