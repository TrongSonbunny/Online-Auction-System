package com.auction.network;

import com.auction.backend.observer.FrontendNotifier;
import com.auction.backend.observer.observers.FrontendNotificationObserver;
import com.auction.models.bid.Transaction;
import com.auction.network.command.CommandContext;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge between the Network (TCP), Business Logic (Commands), and UI
 * (Observers).
 * Each instance of ClientHandler manages one TCP connection to a client and
 * runs
 * in its own Virtual Thread. It listens for JSON messages, processes them using
 * Each client connection is handled by a separate instance of ClientHandler
 * running
 * Lifecycle:
 * 1. Created by ServerMain when a TCP connection is accepted.
 * 2. Runs in a Java 21 Virtual Thread.
 * 3. Listens for JSON strings, converts them to ClientMessage.
 * 4. Delegates logic to ClientActionHandler (Command Pattern).
 * 5. Pushes real-time updates back via the FrontendNotifier interface (Observer
 * Pattern).
 */
public class ClientHandler implements Runnable, FrontendNotifier {

  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

  // Registry userId -> handler: dùng để định tuyến thông báo cá nhân
  // (seller/winner) tới đúng socket và để dọn dẹp khi client ngắt kết nối.
  private static final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

  private final Socket clientSocket;
  private final Gson gson;

  // Observer đẩy cập nhật real-time (giá/trạng thái) tới CHÍNH client này.
  // Đăng ký vào publisher dùng chung khi kết nối, gỡ ra khi ngắt kết nối —
  // chống "ghost observer" và rò rỉ bộ nhớ.
  private FrontendNotificationObserver frontendObserver;

  private PrintWriter writer;
  private String authenticatedUserId;

  /**
   * Khởi tạo một ClientHandler mới.
   *
   * @param socket kết nối TCP đang hoạt động tới máy khách
   */
  public ClientHandler(Socket socket) {
    this.clientSocket = socket;

    // ĐÃ FIX TẬN GỐC: Sử dụng ExclusionStrategy để CẤM Gson dùng Reflection quét
    // vào các biến nhạy cảm!
    this.gson = new GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime.class,
            (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        .registerTypeAdapter(
            LocalDateTime.class,
            (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(
                json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .setExclusionStrategies(
            new ExclusionStrategy() {
              @Override
              public boolean shouldSkipField(FieldAttributes f) {
                // Ngăn chặn Gson chọc ngoáy vào các trường gây lỗi Crash của BidResult
                String fieldName = f.getName();
                return fieldName.equals("manualTransaction")
                    || fieldName.equals("autoBidsPlaced")
                    || fieldName.equals("autoBidTransactions"); // ĐÃ THÊM: Chặn vĩnh viễn biến này
              }

              @Override
              public boolean shouldSkipClass(Class<?> clazz) {
                // Ngăn chặn serialize bất kỳ class nào là Transaction hoặc kế thừa từ
                // Transaction
                return Transaction.class.isAssignableFrom(clazz);
              }
            })
        .create();
  }

  /** Main execution loop for the client connection. */
  @Override
  public void run() {
    // Try-with-resources ensures the reader/writer close automatically if an error
    // occurs.
    try {
      java.io.InputStream in = clientSocket.getInputStream();
      java.io.OutputStream os = clientSocket.getOutputStream();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));
          PrintWriter out = new PrintWriter(os, true)) {
        this.writer = out;
        logger.info("New connection established from: {}", clientSocket.getRemoteSocketAddress());

        // Đăng ký observer real-time: từ giờ mọi AuctionEvent (bid mới, mở/đóng
        // phiên, gia hạn...) sẽ được đẩy thẳng tới client này qua sendNotification.
        registerFrontendObserver();

        String jsonInput;
        while ((jsonInput = reader.readLine()) != null) {
          handleIncomingRequest(jsonInput);
        }
      }
    } catch (IOException e) {
      logger.warn("Connection lost for user {}: {}", authenticatedUserId, e.getMessage());
    } finally {
      cleanup();
    }
  }

  /**
   * Processes a single JSON request from the client.
   *
   * @param rawJson The string received from the TCP socket.
   */
  private void handleIncomingRequest(String rawJson) {
    try {
      // 1. Parse the JSON into our standard ClientMessage object
      ClientMessage request = gson.fromJson(rawJson, ClientMessage.class);

      // 2. Identity Management: Once logged in, we track the userId for this socket
      // and publish it to the online-user registry (for personal notifications).
      if (authenticatedUserId == null && request.getUserId() != null) {
        this.authenticatedUserId = request.getUserId();
        onlineUsers.put(this.authenticatedUserId, this);
      }

      // 3. Command Pattern Execution:
      // We pass the request to ClientActionHandler, which finds the right Command
      // class.
      Object result = ClientActionHandler.doAction(request);

      // 4. Response Routing: Wrap the result into a ServerMessage and send it back.
      ServerMessage response = ServerMessage.builder()
          .action(request.getAction().name())
          .status(ServerMessage.STATUS_SUCCESS)
          .data(result) // The result might be a User, Auction, or BidResult
          .build();

      sendToClient(response);

      // Real-time sync giờ do Observer pattern đảm nhiệm: các service backend
      // publish AuctionEvent, và FrontendNotificationObserver (đăng ký ở mỗi
      // ClientHandler) đẩy thẳng payload tới mọi client đang online. Không cần
      // broadcast "UPDATE_PRICE" thủ công và bắt client refresh toàn bộ nữa.

    } catch (Exception e) {
      logger.error("Error processing request: {}", e.getMessage());
      sendToClient(ServerMessage.error("EXECUTION_ERROR", e.getMessage()));
    }
  }

  /**
   * This method is the "Magic Link" to the Observer Pattern.
   * When an AuctionEvent is published in the backend, the Observer calls this
   * method
   * to push data to the frontend without the frontend having to ask for it.
   */
  @Override
  public void sendNotification(String auctionId, String jsonPayload) {
    // Wrap the raw payload from the observer into our standard ServerMessage
    // envelope.
    ServerMessage eventMessage = ServerMessage.builder()
        .action(ServerMessage.ACTION_EVENT)
        .status(ServerMessage.STATUS_SUCCESS)
        .auctionId(auctionId)
        .message(jsonPayload)
        .build();

    sendToClient(eventMessage);
  }

  /** Utility to safely send a ServerMessage object as JSON over the wire. */
  private synchronized void sendToClient(ServerMessage message) {
    if (writer != null && !clientSocket.isClosed()) {
      writer.println(gson.toJson(message));
    }
  }

  /**
   * Tra cứu socket của một user đang online để gửi thông báo cá nhân.
   * Dùng bởi {@link PersonalNotificationObserver} để định tuyến tới seller/winner.
   *
   * @param userId mã user cần tìm
   * @return notifier (ClientHandler) của user, hoặc {@code null} nếu họ offline
   */
  public static FrontendNotifier getOnlineUser(String userId) {
    if (userId == null) {
      return null;
    }
    return onlineUsers.get(userId);
  }

  /**
   * Đăng ký observer real-time của connection này vào publisher dùng chung.
   * Nếu publisher chưa sẵn sàng (server chưa khởi tạo command context) thì bỏ qua
   * an toàn — client vẫn nhận response trực tiếp như bình thường.
   */
  private void registerFrontendObserver() {
    CommandContext context = CommandContext.getShared();
    if (context == null) {
      logger.warn("Shared CommandContext chưa sẵn sàng; bỏ qua đăng ký observer real-time.");
      return;
    }
    this.frontendObserver = new FrontendNotificationObserver(this);
    context.getEventPublisher().addObserver(this.frontendObserver);
  }

  /**
   * Prevents "Ghost Observers" and memory leaks.
   * When a user disconnects, we unregister their real-time observer from the
   * shared publisher and drop them from the online-user registry.
   */
  private void cleanup() {
    logger.info("Cleaning up resources for user: {}", authenticatedUserId);

    // Gỡ observer real-time khỏi publisher dùng chung (chống ghost observer).
    CommandContext context = CommandContext.getShared();
    if (context != null && frontendObserver != null) {
      context.getEventPublisher().removeObserver(frontendObserver);
    }
    this.frontendObserver = null;

    // Gỡ khỏi registry online — chỉ gỡ nếu mapping vẫn trỏ tới chính handler này
    // (tránh xóa nhầm khi user mở connection mới).
    if (authenticatedUserId != null) {
      onlineUsers.remove(authenticatedUserId, this);
    }

    try {
      if (!clientSocket.isClosed()) {
        clientSocket.close();
      }
    } catch (IOException e) {
      logger.error("Failed to close socket: {}", e.getMessage());
    }
  }
}