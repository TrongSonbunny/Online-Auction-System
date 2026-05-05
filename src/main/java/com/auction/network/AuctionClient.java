package com.auction.network;

import com.auction.factory.ItemFactory;
import com.auction.models.Item;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Máy khách, kết nối tới Server và gửi một sản phẩm qua mạng.
 */
public class AuctionClient {

  private static final String SERVER_ADDRESS = "127.0.0.1";
  private static final int SERVER_PORT = 8080;

  /**
   * Khởi chạy Client.
   *
   * @param args Tham số dòng lệnh
   */
  public static void main(String[] args) {
    try (
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
      System.out.println("Đã kết nối thành công tới Server!");

      // Tạo một món đồ mẫu (Hãy chắc chắn bạn đã có lớp Electronics hoặc Factory phù
      // hợp)
      Item itemToSend = ItemFactory.createItem("electronics", "Dell XPS 15", 1200.0, 10);

      // Serialize và gửi sang Server
      out.writeObject(itemToSend);
      out.flush();
      System.out.println("Đã gửi sản phẩm: " + itemToSend.getName());

      // Lắng nghe phản hồi từ Server
      String response = (String) in.readObject();
      System.out.println("Server nói: " + response);

    } catch (IOException | ClassNotFoundException e) {
      System.err.println("Không thể kết nối đến máy chủ: " + e.getMessage());
    }
  }
}