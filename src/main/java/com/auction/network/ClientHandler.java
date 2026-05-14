package com.auction.network;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xử lý vòng đời kết nối mạng của từng máy khách riêng biệt. Được thiết kế để chạy bên trong một
 * Luồng ảo (Virtual Thread) của Java 21.
 */
public class ClientHandler implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
  private final Socket clientSocket;

  /**
   * Khởi tạo một ClientHandler mới.
   *
   * @param socket kết nối TCP đang hoạt động tới máy khách
   */
  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    BufferedReader reader = null; // Put reader, writer outside tryblock cause if put them in tryblc
    PrintWriter writer = null; // They cant be access in the finally and the catch block.
    try {
      logger.info("Luồng ảo đang xử lý máy khách từ: {}", clientSocket.getRemoteSocketAddress());

      // Khởi tạo input
      reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      // Khởi tạo output
      writer = new PrintWriter(clientSocket.getOutputStream(), true);

      // Khởi tạo Gson để chuyển dữ liệu Json thành một object chứa dữ liệu dùng từ AuctionMessage.
      Gson gson = new Gson();

      String clientInput;
      while ((clientInput = reader.readLine()) != null) {
        logger.info("Nhận được dữ liệu Json từ client: {}", clientInput);

        try {
          // Gson magic: convert the text from a json format to the real object with its attribut
          // including action, username, amount
          ClientMessage message = gson.fromJson(clientInput, ClientMessage.class);
          // Lợi khi dùng Gson: Ví dụ như khi Client nhập thiếu một trường dữ liệu ({"action":
          // "BID"} nhưng không có username,...) thì những biến bị bỏ trống đó sẽ được cho vào thành
          // null/0/false/... mà không làm crash chương trình.
          // ClientActionHandler.doAction(message, this)
          

        } catch (com.google.gson.JsonSyntaxException jsonError) {
          // Gson throws a specific JsonSyntaxException when the JSON is malformed
          logger.warn("Máy khách thiết lập Dữ liệu Json sai định dạng: {}", clientInput);
          writer.println("ERROR: Không đúng định dạng Json");
        }
      }

      logger.info("Client đã ngắt kết nối chủ động.");


    } catch (Exception e) {
      logger.error("Máy khách đã ngắt kết nối đột ngột: {}", e.getMessage(), e);

    } finally {
      // 3. UNSUBSCRIBE: Safely remove them from the roster so we don't broadcast to a dead pipe
      // if (writer != null) {
      //   ServerMain.activeClients.remove(writer);
      //   logger.info("Client left. Total active clients remaining: {}",
      //       ServerMain.activeClients.size());
      // }

      // DỌN DẸP: Đóng socket một cách an toàn khi máy khách rời đi hoặc xảy ra lỗi
      try {
        if (clientSocket != null && !clientSocket.isClosed()) {
          logger.info("Đang đóng kết nối cho: {}", clientSocket.getRemoteSocketAddress());
          clientSocket.close();
        }
      } catch (Exception e) {
        logger.error("Không thể đóng kết nối socket một cách an toàn: {}", e.getMessage(), e);
      }
    }
  }
}
