package com.auction.network;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the TCP connection from the client to the server.
 * Handles sending ClientMessages and asynchronously receiving ServerMessages.
 */
public class NetworkClient {

  private static final Logger logger = LoggerFactory.getLogger(NetworkClient.class);
  private final Gson gson;
  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;
  private volatile boolean isRunning = true;

  /**
   * Constructor to allow converting the Json.
   */
  public NetworkClient() {
    this.gson = new Gson();
  }

  /**
   * Establishes a connection to the server using system properties for IP and Port.
   */
  public void connect() {
    String serverIp = System.getProperty("server.ip", "localhost");
    int port = Integer.getInteger("server.port", 8080);

    try {
      logger.info("Connecting to server at {}:{}...", serverIp, port);
      socket = new Socket(serverIp, port);
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
   * Listens for incoming JSON strings from the server on a separate thread.
   */
  private void startListeningThread() {
    Thread listener = Thread.ofVirtual().start(() -> {
      try {
        String jsonLine;
        while (isRunning && (jsonLine = in.readLine()) != null) {
          ServerMessage response = gson.fromJson(jsonLine, ServerMessage.class);
          handleServerResponse(response);
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
   * Logic to route the server response to the UI or controller.
   */
  private void handleServerResponse(ServerMessage response) {
    logger.info("Received action: {} with status: {}", response.getAction(), response.getStatus());
    // TODO: Connect this to your UI or Observer pattern
  }

  /**
   * Closes the network resources gracefully.
   */
  public void close() {
    isRunning = false;
    try {
      if (socket != null) {
        socket.close();
      }
      logger.info("Network client closed.");
    } catch (IOException e) {
      logger.error("Error while closing socket: {}", e.getMessage());
    }
  }
}