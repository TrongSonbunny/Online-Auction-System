package com.auction.network;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the TCP connection from the client to the server.
 * Handles sending ClientMessages and asynchronously receiving ServerMessages.
 */
public class NetworkClient {

  private static final Logger logger = LoggerFactory.getLogger(NetworkClient.class);
  
  private final Gson gson;
  private final List<MessageListener> listeners;
  
  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  private volatile boolean isRunning = true;

  /**
   * Constructor initializes JSON converter and thread-safe listener list.
   */
  public NetworkClient() {
    this.gson = new Gson();
    this.listeners = new CopyOnWriteArrayList<>();
  }

  /**
   * Interface để giao diện người dùng (UI) đăng ký nhận dữ liệu từ server.
   */
  public interface MessageListener {
    /**
     * Called when a message is received from the server.
     *
     * @param message The ServerMessage received from the server.
     */
    void onMessageReceived(ServerMessage message);
  }

  /**
   * Đăng ký một listener (thường là một Controller của UI).
   *
   * @param listener controller muốn nhận dữ liệu
   */
  public void addListener(MessageListener listener) {
    listeners.add(listener);
  }

  /**
   * Establishes a connection to the server using system properties for IP and Port.
   */
  public void connect() {
    // Thay "trongson-laptop.local" bằng tên máy tính TẬT của bạn
    String serverIp = System.getProperty("server.ip", "trongson-ThinkPad-T450.local"); 
    int port = Integer.getInteger("server.port", 8080);
    
    try {
      logger.info("Connecting to server at {}:{}...", serverIp, port);
      socket = new Socket(serverIp, port);
      // ... phần còn lại giữ nguyên 
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      logger.info("Connected successfully.");
      startListeningThread();
    } catch (IOException e) {
      logger.error("Failed to connect to the server: {}", e.getMessage());
    }
  }

  /**
   * Sends a message to the server in JSON format.
   *
   * @param message The ClientMessage object to be sent.
   */
  public void sendMessage(ClientMessage message) {
    if (socket != null && socket.isConnected() && !socket.isClosed()) {
      String json = gson.toJson(message);
      out.println(json);
      logger.debug("Sent: {}", json);
    } else {
      logger.warn("Cannot send message: Not connected to server.");
    }
  }

  /**
   * Listens for incoming JSON strings from the server on a separate Virtual Thread.
   */
  private void startListeningThread() {
    Thread listenerThread = Thread.ofVirtual().start(() -> {
      try {
        String jsonLine;
        while (isRunning && (jsonLine = in.readLine()) != null) {
          ServerMessage response = gson.fromJson(jsonLine, ServerMessage.class);
          notifyListeners(response);
        }
      } catch (IOException e) {
        if (isRunning) {
          logger.error("Connection lost: {}", e.getMessage());
        }
      } finally {
        close();
      }
    });
  }

  /**
   * Chuyển tiếp ServerMessage cho tất cả các màn hình UI đang lắng nghe.
   */
  private void notifyListeners(ServerMessage response) {
    logger.debug("Received action: {} with status: {}", response.getAction(), response.getStatus());
    for (MessageListener listener : listeners) {
      listener.onMessageReceived(response);
    }
  }

  /**
   * Closes the network resources gracefully.
   */
  public void close() {
    isRunning = false;
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
      logger.info("Network client closed.");
    } catch (IOException e) {
      logger.error("Error while closing socket: {}", e.getMessage());
    }
  }
}