package com.auction.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main entry point for the Auction TCP Server.
 * Initializes the server socket and manages incoming connections via Virtual
 * Threads.
 */
public class ServerMain {
  public static void main(String[] args) {
    try (ServerSocket server = new ServerSocket(8080);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();) {

      while (true) {
        try {
          Socket clientSocket = server.accept();
          executor.submit(new HandleClient(clientSocket));
        } catch (IOException ex) {
          System.err.println("Error in creating connection with client");
        }
      }
    } catch (IOException e) {
      System.err.println("CRITICAL: Couldn't start server on port 8080");
    }
  }
}