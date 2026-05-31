package com.auction.network;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the TCP connection using the Singleton Pattern.
 * Reads the server IP address and port from the config.properties file.
 */
public class NetworkClient {

  private static final Logger logger = LoggerFactory.getLogger(NetworkClient.class);
  private static NetworkClient instance;

  private final Gson gson;
  private final List<MessageListener> listeners;

  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  private volatile boolean isRunning = false;

  private NetworkClient() {
    this.gson = new Gson();
    this.listeners = new CopyOnWriteArrayList<>();

  }

  /**
   * 3. Hàm public static để lấy instance duy nhất (Double-Checked Locking an toàn
   * cho Thread).
   *
   * @return The single instance of NetworkClient.
   */
  public static synchronized NetworkClient getInstance() {
    if (instance == null) {
      instance = new NetworkClient();
    }
    return instance;
  }

  /**
   * Adds a listener to be notified when a new message is received.
   *
   * @param listener The message listener to add.
   */
  public void addListener(MessageListener listener) {
    // ĐÃ FIX: Tận dụng danh sách có sẵn, dọn sạch Listener cũ trước khi thêm mới
    // Giải quyết triệt để lỗi "Bóng bàn" (Listener Leak) mà không cần tạo hàm mới!
    listeners.clear();
    listeners.add(listener);
  }

  /**
   * Establishes a connection to the server using system properties for IP and
   * Port.
   */
  public void connect() {
    String serverIp = System.getProperty("server.ip", "127.0.0.1");
    int port = Integer.getInteger("server.port", 8080);

    try {
      logger.info("Connecting to server at {}:{}...", serverIp, port);
      socket = new Socket(serverIp, port);
      // ... phần còn lại giữ nguyên
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      logger.info("Connected successfully.");
      isRunning = true;
      startListeningThread();
    } catch (IOException e) {
      logger.error("Failed to connect to the server: {}", e.getMessage());
    }
  }

  /**
   * Removes a previously registered message listener.
   *
   * @param listener The message listener to remove.
   */
  public void removeListener(MessageListener listener) {
    listeners.remove(listener);
  }

  /**
   * Helper method to assign a callback directly without instantiating an interface.
   *
   * @param callback The consumer function handling the server message.
   */
  public void setOnUpdateReceived(Consumer<ServerMessage> callback) {
    addListener(callback::accept);
  }

  /**
   * Serializes the given message to JSON and sends it to the server.
   *
   * @param message The object payload to send.
   */
  public void sendMessage(Object message) {
    if (isRunning && out != null) {
      String json = gson.toJson(message);
      out.println(json);
      logger.debug("Message sent: {}", json);
    } else {
      logger.error("Failed to send message: Not connected to the server.");
    }
  }

  /**
   * Listens for incoming JSON strings from the server on a separate Virtual
   * Thread.
   */
  private void startListeningThread() {
    Thread.ofVirtual().start(
        () -> {
          try {
            String jsonLine;
            while (isRunning && (jsonLine = in.readLine()) != null) {
              ServerMessage response = gson.fromJson(jsonLine, ServerMessage.class);
              for (MessageListener listener : listeners) {
                listener.onMessageReceived(response);
              }
            }
          } catch (IOException e) {
            if (isRunning) {
              logger.error("Lost connection to server: {}", e.getMessage());
            }
          } finally {
            close();
          }
        });
  }

  /**
   * Closes the network connection and gracefully stops the listening thread.
   */
  public synchronized void close() {
    if (!isRunning) {
      return;
    }
    isRunning = false;
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
      logger.info("NetworkClient connection closed safely.");
    } catch (IOException e) {
      logger.error("Error occurred while closing the socket", e);
    }
  }
}