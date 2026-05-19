package com.auction.network;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
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

  private String serverHost;
  private int serverPort;

  private NetworkClient() {
    this.gson = new Gson();
    this.listeners = new CopyOnWriteArrayList<>();
    loadConfiguration();
  }

  /**
   * Retrieves the singleton instance of the NetworkClient.
   *
   * @return The single instance of NetworkClient.
   */
  public static synchronized NetworkClient getInstance() {
    if (instance == null) {
      instance = new NetworkClient();
    }
    return instance;
  }

  private void loadConfiguration() {
    this.serverHost = "127.0.0.1";
    this.serverPort = 8080;

    File configFile = new File("config.properties");

    if (configFile.exists()) {
      try (FileInputStream fis = new FileInputStream(configFile)) {
        Properties props = new Properties();
        props.load(fis);

        this.serverHost = props.getProperty("server.host", "127.0.0.1").trim();
        this.serverPort = Integer.parseInt(props.getProperty("server.port", "8080").trim());

        logger.info("Loaded config from properties file -> {}:{}", serverHost, serverPort);
      } catch (Exception e) {
        logger.warn("Malformed config.properties. Using defaults (127.0.0.1:8080).", e);
      }
    } else {
      logger.info("No config.properties found. Using default IP -> {}:{}", serverHost, serverPort);
    }
  }

  /**
   * Connects to the server using the loaded configuration.
   *
   * @throws IOException If an I/O error occurs while establishing the connection.
   */
  public synchronized void connect() throws IOException {
    if (socket == null || socket.isClosed()) {
      logger.info("Connecting to server at {}:{}...", serverHost, serverPort);
      this.socket = new Socket(this.serverHost, this.serverPort);
      this.out = new PrintWriter(socket.getOutputStream(), true);
      this.in = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      this.isRunning = true;
      startListeningThread();
      logger.info("Connection established successfully!");
    }
  }

  /**
   * Adds a listener to be notified when a new message is received.
   *
   * @param listener The message listener to add.
   */
  public void addListener(MessageListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
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