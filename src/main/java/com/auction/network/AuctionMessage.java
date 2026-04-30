package com.auction.network;

/**
 * A blueprint class that Gson uses to convert incoming JSON into a Java Object.
 */
public class AuctionMessage {

  private String action;
  private String username;
  private int amount;

  /**
   * Gson REQUIRES an empty, default constructor to do its magic.
   */
  public AuctionMessage() {}

  // Getters and Setters (Required for Gson to inject the data)
  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }
}
