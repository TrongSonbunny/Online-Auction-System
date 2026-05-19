package com.auction.network;

import com.auction.models.Message;
import com.auction.models.item.AuctionItem;
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
 * Máy chủ đấu giá đóng vai trò làm Backend xử lý logic và lưu trữ phiên đấu
 * giá.
 */
public class AuctionServer {

  private static final int PORT = 8080;
  private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
  private static final ExecutorService pool = Executors.newFixedThreadPool(100);
  private static final Gson gson = new Gson();

  // Bộ nhớ đệm lưu trữ tất cả các phiên đấu giá đang diễn ra trên toàn hệ thống
  private static final List<Map<String, Object>> activeAuctions = new CopyOnWriteArrayList<>();

  /**
   * Khởi chạy Server.
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
   * Lớp xử lý giao tiếp đa luồng với từng Client.
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
     * Bộ định tuyến API xử lý mọi yêu cầu từ ứng dụng.
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

        // 1. NGƯỜI BÁN TẠO SẢN PHẨM MỚI
        case "CREATE_AUCTION":
          res.setAction("ADD_ITEM_RESPONSE");
          AuctionItem newItem = req.getItem();
          if (newItem != null) {
            // Đóng gói sản phẩm thành định dạng Map để Gson dễ xử lý
            Map<String, Object> auctionSession = new HashMap<>();
            auctionSession.put("auctionId", newItem.getItemId());
            auctionSession.put("item", newItem);
            auctionSession.put("currentPrice", newItem.getEstimatedPrice());
            auctionSession.put("status", "ACTIVE");

            activeAuctions.add(auctionSession); // Lưu vào RAM Server
            res.setStatus("SUCCESS");
            System.out.println("Đã thêm SP mới lên sàn: " + newItem.getName());
          } else {
            res.setStatus("ERROR");
          }
          break;

        // 2. NGƯỜI MUA LẤY DANH SÁCH SẢN PHẨM
        case "GET_ALL_AUCTIONS_REQUEST":
          res.setAction("GET_ALL_AUCTIONS_RESPONSE");
          res.setStatus("SUCCESS");
          res.setData(activeAuctions); // Trả về toàn bộ kho đồ
          break;

        // 3. NGƯỜI MUA ĐẶT GIÁ
        case "BID":
          res.setAction("BID_RESPONSE");
          String targetAuctionId = req.getAuctionId();
          Double newBidAmount = req.getBidAmount();
          boolean isBidSuccess = false;

          for (Map<String, Object> session : activeAuctions) {
            if (session.get("auctionId").equals(targetAuctionId)) {
              double currentPrice = (Double) session.get("currentPrice");
              if (newBidAmount != null && newBidAmount > currentPrice) {
                session.put("currentPrice", newBidAmount); // Cập nhật giá mới
                isBidSuccess = true;
                System.out.println("Sản phẩm " + targetAuctionId + " được đặt giá mới: $" + newBidAmount);
              }
              break;
            }
          }

          res.setStatus(isBidSuccess ? "SUCCESS" : "ERROR");
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