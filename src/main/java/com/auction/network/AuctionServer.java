package com.auction.network;

import com.auction.models.Message;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Máy chủ đấu giá đóng vai trò làm Backend xử lý logic và cơ sở dữ liệu SQLite.
 * Giao tiếp với Client hoàn toàn bằng giao thức JSON.
 */
public class AuctionServer {

  private static final int PORT = 8080;
  private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
  private static final ExecutorService pool = Executors.newFixedThreadPool(100);
  private static final Gson gson = new Gson();

  /**
   * Khởi chạy Server và chuẩn bị cơ sở dữ liệu.
   *
   * @param args Tham số dòng lệnh.
   */
  public static void main(String[] args) {
    System.out.println("Máy chủ AUCTION X đang khởi động...");
    DatabaseManager.initialize();

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      System.out.println("Server đang lắng nghe tại cổng: " + PORT);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        ClientHandler handler = new ClientHandler(clientSocket);
        clients.add(handler);
        pool.execute(handler);
      }
    } catch (IOException e) {
      System.err.println("Lỗi Server nghiêm trọng: " + e.getMessage());
    }
  }

  /**
   * Lớp xử lý giao tiếp đa luồng với từng Client riêng biệt.
   */
  private static class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try {
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String requestJson;
        while ((requestJson = in.readLine()) != null) {
          Message request = gson.fromJson(requestJson, Message.class);
          Message response = processRequest(request);
          if (out != null) {
            out.println(gson.toJson(response));
          }
        }
      } catch (Exception e) {
        System.out.println("Một Client đã ngắt kết nối an toàn.");
      } finally {
        clients.remove(this);
        try {
          if (socket != null && !socket.isClosed()) {
            socket.close();
          }
        } catch (IOException e) {
          System.err.println("Lỗi khi đóng socket: " + e.getMessage());
        }
      }
    }

    /**
     * Bộ định tuyến xử lý logic Backend dựa trên Action trong JSON Payload.
     *
     * @param req Gói tin yêu cầu từ Client.
     * @return Gói tin phản hồi đã xử lý chuẩn format.
     */
    private Message processRequest(Message req) {
      String action = req.getAction();
      Message res = new Message();

      if (action == null) {
        res.setAction("UNKNOWN_RESPONSE");
        res.setStatus("ERROR");
        return res;
      }

      switch (action) {
        case "LOGIN_REQUEST":
          res.setAction("LOGIN_RESPONSE");
          // Lấy trực tiếp username và password từ trường dữ liệu ngoài cùng!
          boolean auth = DatabaseManager.authenticateUser(req.getUsername(), req.getPassword());
          if (auth) {
            res.setStatus("SUCCESS");
            Map<String, String> data = new HashMap<>();
            data.put("userId", req.getUsername());
            data.put("role", req.getRole());
            res.setData(data);
          } else {
            res.setStatus("ERROR");
          }
          break;

        case "REGISTER":
          res.setAction("REGISTER_RESPONSE");
          boolean reg = DatabaseManager.registerUser(req.getUsername(), req.getPassword());
          res.setStatus(reg ? "SUCCESS" : "ERROR");
          break;

        default:
          res.setAction("UNKNOWN_RESPONSE");
          res.setStatus("ERROR");
          break;
      }
      return res;
    }
  }
}