package com.auction.network;

import com.auction.models.Item;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Máy chủ đấu giá, lắng nghe và nhận dữ liệu sản phẩm từ Client.
 */
public class AuctionServer {

  private static final int PORT = 8080;

  /**
   * Khởi chạy Server.
   *
   * @param args Tham số dòng lệnh
   */
  public static void main(String[] args) {
    System.out.println("Máy chủ đang khởi động trên cổng " + PORT + "...");

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Có một Client vừa kết nối: " + clientSocket.getInetAddress());
        handleClient(clientSocket);
      }
    } catch (IOException e) {
      System.err.println("Lỗi Server: " + e.getMessage());
    }
  }

  private static void handleClient(Socket socket) {
    try (
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
      // Đọc Object (đã được Serialize) từ Client
      Object request = in.readObject();

      if (request instanceof Item) {
        Item receivedItem = (Item) request;
        System.out.println("Đã nhận sản phẩm từ Client: " + receivedItem.getName());

        // Trả lời lại Client
        out.writeObject("Server xác nhận đã nhận thành công: " + receivedItem.getName());
      }
    } catch (IOException | ClassNotFoundException e) {
      System.err.println("Lỗi kết nối với Client: " + e.getMessage());
    }
  }
}