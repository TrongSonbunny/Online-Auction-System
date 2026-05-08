package com.auction.network;

import com.auction.models.Item;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Máy chủ đấu giá đa luồng, hỗ trợ nhiều Client kết nối cùng lúc và Real-time
 * update.
 */
public class AuctionServer {

  private static final int PORT = 8080;

  // Danh sách an toàn cho đa luồng, lưu trữ các Client đang kết nối (Observer
  // Pattern)
  private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

  // Thread pool để quản lý các luồng Client
  private static final ExecutorService pool = Executors.newFixedThreadPool(100);

  // Danh sách sản phẩm "Gốc" của toàn hệ thống
  private static final List<Item> masterInventory = new CopyOnWriteArrayList<>();

  /**
   * Khởi chạy Server.
   *
   * @param args Tham số dòng lệnh
   */
  public static void main(String[] args) {
    System.out.println("Máy chủ đang chạy trên cổng " + PORT + "...");

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client mới kết nối: " + clientSocket.getInetAddress());

        ClientHandler clientThread = new ClientHandler(clientSocket);
        clients.add(clientThread);
        pool.execute(clientThread);
      }
    } catch (IOException e) {
      System.err.println("Lỗi Server: " + e.getMessage());
    }
  }

  /**
   * Gửi dữ liệu tới tất cả các Client đang kết nối (Broadcast / Observer Update).
   *
   * @param message Dữ liệu cần gửi
   */
  public static void broadcast(Object message) {
    for (ClientHandler client : clients) {
      client.sendMessage(message);
    }
  }

  /**
   * Lớp con xử lý giao tiếp riêng biệt với từng Client.
   */
  private static class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket) {
      this.socket = socket;
      try {
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
      } catch (IOException e) {
        System.err.println("Lỗi khởi tạo luồng Client: " + e.getMessage());
      }
    }

    @Override
    public void run() {
      try {
        // Gửi danh sách gốc cho Client ngay khi vừa kết nối
        sendMessage(masterInventory);

        Object request;
        while ((request = in.readObject()) != null) {
          if (request instanceof Item) {
            Item incomingItem = (Item) request;
            boolean isNewItem = true;

            // Kiểm tra xem Item này đã tồn tại trong masterInventory chưa
            for (Item item : masterInventory) {
              if (item.getId().equals(incomingItem.getId())) {
                item.setStartingPrice(incomingItem.getStartingPrice());
                isNewItem = false;
                break;
              }
            }

            // Nếu ID chưa từng xuất hiện -> Đây là SẢN PHẨM MỚI từ Seller
            if (isNewItem) {
              masterInventory.add(incomingItem);
              System.out.println("Đã thêm sản phẩm mới từ Seller: " + incomingItem.getName());
              AuctionServer.broadcast(masterInventory);
            } else {
              // Chỉ cập nhật giá, broadcast lại đúng item đó
              AuctionServer.broadcast(incomingItem);
            }
          }
        }
      } catch (IOException | ClassNotFoundException e) {
        // Chủ động bắt lỗi EOFException khi Client ngắt kết nối (Đăng xuất)
        System.out.println("Một Client đã ngắt kết nối an toàn.");
      } finally {
        // Luôn luôn đảm bảo xóa Client khỏi danh sách và đóng Socket
        clients.remove(this);
        System.out.println("Đã xóa Client khỏi danh sách quản lý. Số Client hiện tại: "
            + clients.size());
        try {
          if (socket != null && !socket.isClosed()) {
            socket.close();
          }
        } catch (IOException e) {
          System.err.println("Lỗi khi đóng Socket của Client: " + e.getMessage());
        }
      }
    }

    /**
     * Gửi dữ liệu cụ thể cho Client này.
     *
     * @param message Dữ liệu cần gửi
     */
    public void sendMessage(Object message) {
      try {
        out.reset(); // Xóa cache để ép gửi object mới
        out.writeObject(message);
        out.flush();
      } catch (IOException e) {
        System.err.println("Lỗi gửi tin tới Client: " + e.getMessage());
      }
    }
  }
}