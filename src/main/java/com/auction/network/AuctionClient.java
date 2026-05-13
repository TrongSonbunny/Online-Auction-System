package com.auction.network;

import com.auction.models.Message;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Máy khách đóng vai trò trung gian, kết nối JavaFX Controller với Server qua
 * JSON. Áp dụng mẫu thiết kế Singleton.
 */
public class AuctionClient {

  private static final String SERVER_ADDRESS = "127.0.0.1";
  private static final int SERVER_PORT = 8080;

  private static AuctionClient instance;
  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  private final Gson gson;

  /**
   * Khởi tạo Client và chuẩn bị Gson.
   */
  private AuctionClient() {
    gson = new Gson();
  }

  /**
   * Lấy đối tượng AuctionClient duy nhất (Singleton).
   *
   * @return Thể hiện của AuctionClient.
   */
  public static synchronized AuctionClient getInstance() {
    if (instance == null) {
      instance = new AuctionClient();
    }
    return instance;
  }

  /**
   * Kết nối tới Server. Đảm bảo Socket luôn sẵn sàng trước khi gửi dữ liệu.
   *
   * @throws IOException Nếu không thể kết nối.
   */
  private void connect() throws IOException {
    if (socket == null || socket.isClosed()) {
      socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      System.out.println("Đã kết nối tới Server AUCTION X thành công!");
    }
  }

  /**
   * Đóng gói Object thành JSON, gửi lên Server và chờ phản hồi đồng bộ.
   * Dùng chung cho Login, Register, Bid...
   *
   * @param request Gói tin yêu cầu.
   * @return Gói tin phản hồi từ Server.
   * @throws IOException Nếu đường truyền lỗi.
   */
  public Message sendRequest(Message request) throws IOException {
    connect();

    String jsonRequest = gson.toJson(request);
    out.println(jsonRequest);

    String jsonResponse = in.readLine();
    if (jsonResponse == null) {
      throw new IOException("Server đã ngắt kết nối đột ngột.");
    }

    return gson.fromJson(jsonResponse, Message.class);
  }
}