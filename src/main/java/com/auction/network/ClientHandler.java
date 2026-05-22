package com.auction.network;

import com.auction.backend.observer.AuctionObserver;
import com.auction.backend.observer.FrontendNotifier;
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
 * * Lifecycle:
 * 1. Created by ServerMain when a TCP connection is accepted.
 * 2. Runs in a Java 21 Virtual Thread.
 * 3. Listens for JSON strings, converts them to ClientMessage.
 * 4. Delegates logic to ClientActionHandler (Command Pattern).
 * 5. Pushes real-time updates back via the FrontendNotifier interface (Observer
 * Pattern).
 */
public class ClientHandler implements Runnable, FrontendNotifier {

  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

  private final Socket clientSocket;
  private final Gson gson;

  // High-performance thread-safe map to track which auctions this user is
  // watching.
  // This allows us to remove observers cleanly if the client disconnects.
  private final Map<String, AuctionObserver> activeObservers = new ConcurrentHashMap<>();

  private PrintWriter writer;
  private String authenticatedUserId;

  /**
   * Khởi tạo một ClientHandler mới.
   *
   * @param socket kết nối TCP đang hoạt động tới máy khách
   */
  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
    // ĐÃ FIX: Tích hợp "Bộ dịch giả" TypeAdapter để Gson không bị chặn bởi bảo mật
    // của Java 16+
    this.gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class,
            (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        .registerTypeAdapter(LocalDateTime.class,
            (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(
                json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .create();
  }

  /**
   * Main execution loop for the client connection.
   */
  @Override
  public void run() {
    // Try-with-resources ensures the reader/writer close automatically if an error
    // occurs.
    try (
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
      this.writer = out;
      logger.info("New connection established from: {}", clientSocket.getRemoteSocketAddress());

      String jsonInput;
      while ((jsonInput = reader.readLine()) != null) {
        handleIncomingRequest(jsonInput);
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

      // 2. Identity Management: Once logged in, we track the userId for this socket.
      if (authenticatedUserId == null && request.getUserId() != null) {
        this.authenticatedUserId = request.getUserId();
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

  /**
   * Utility to safely send a ServerMessage object as JSON over the wire.
   */
  private synchronized void sendToClient(ServerMessage message) {
    if (writer != null && !clientSocket.isClosed()) {
      writer.println(gson.toJson(message));
    }
  }

  /**
   * Prevents "Ghost Observers" and memory leaks.
   * When a user disconnects, we must remove their observers from the backend
   * Auction objects.
   */
  private void cleanup() {
    logger.info("Cleaning up resources for user: {}", authenticatedUserId);

    // Iterate through all auctions this client was watching and unregister them.
    activeObservers.forEach((auctionId, observer) -> {
      // Access your AuctionManager/Service here to remove the observer.
      // Example: auctionService.removeObserver(auctionId, observer);
    });

    activeObservers.clear();

    try {
      if (!clientSocket.isClosed()) {
        clientSocket.close();
      }
    } catch (IOException e) {
      logger.error("Failed to close socket: {}", e.getMessage());
    }
  }
}