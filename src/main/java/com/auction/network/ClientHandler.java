package com.auction.network;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

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

      // 1. SUBSCRIBE: Add this client's pipe to the global roster
      ServerMain.activeClients.add(writer);
      logger.info("Client joined! Total active clients: {}", ServerMain.activeClients.size());

      String clientMessage;
      while ((clientMessage = reader.readLine()) != null) {
        logger.info("Nhận được dữ liệu Json từ client: {}", clientMessage);

        try {
          // Gson magic: convert the text from a json format to the real object with its attribut
          // including action, username, amount
          AuctionMessage message = gson.fromJson(clientMessage, AuctionMessage.class);

          // Lợi khi dùng Gson: Ví dụ như khi Client nhập thiếu một trường dữ liệu ({"action":
          // "BID"} nhưng không có username,...) thì những biến bị bỏ trống đó sẽ được cho vào thành
          // null/0/false/... mà không làm crash chương trình.
          logger.info("Nhận action: {}", message.getAction());
          logger.info("Nhận username: {}", message.getUsername());

          if ("BID".equalsIgnoreCase(message.getAction())) {
            // Format the announcement
            String announcement = String.format("ANNOUNCEMENT: %s just placed a bid of $%d!",
                message.getUsername(), message.getAmount());

            logger.info("Broadcasting to {} clients: {}", ServerMain.activeClients.size(),
                announcement);

            // 2. NOTIFY: Loop through the thread-safe list and tell everyone
            for (PrintWriter clientWriter : ServerMain.activeClients) {
              clientWriter.println(announcement);
            }

          } else {
            writer.println("Private Server Msg: Unknown command.");
          }

        } catch (com.google.gson.JsonSyntaxException jsonError) {
          // Gson throws a specific JsonSyntaxException when the JSON is malformed
          logger.warn("Máy khách thiết lập Dữ liệu Json sai định dạng: {}", clientMessage);
          writer.println("ERROR: Không đúng định dạng Json");
        }
      }

      logger.info("Client đã ngắt kết nối chủ động.");


    } catch (Exception e) {
      logger.error("Máy khách đã ngắt kết nối đột ngột: {}", e.getMessage(), e);

    } finally {
      // 3. UNSUBSCRIBE: Safely remove them from the roster so we don't broadcast to a dead pipe
      if (writer != null) {
        ServerMain.activeClients.remove(writer);
        logger.info("Client left. Total active clients remaining: {}",
            ServerMain.activeClients.size());
      }

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
