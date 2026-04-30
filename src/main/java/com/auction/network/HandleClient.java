package com.auction.network;

import java.net.Socket;

public class HandleClient implements Runnable {
  private final Socket clientSocket;

  public HandleClient(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public void run() {
    try {
      System.out.println("Hello world");

    }

    catch (Exception ex) {
      System.err.println("Client handle crashed");

    }

    finally {

      try {

        if (clientSocket != null && !clientSocket.isClosed()) {
          System.out.println("Closing connection for: " + clientSocket.getRemoteSocketAddress());
          clientSocket.close();
        }

      }

      catch (Exception e) {
        System.err.println("Cannot close socket " + e.getMessage());

      }

    }
  }
}
