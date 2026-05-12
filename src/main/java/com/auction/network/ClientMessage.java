package com.auction.network;

import java.util.Map;

/**
 * Data Transfer Object representing messages sent from the client to the server.
 * This class is designed to be parsed from JSON using GSON.
 */
public class ClientMessage {

  /**
   * The type of operation requested (e.g., "PLACE_BID", "CREATE_AUCTION").
   */
  private String action;

  /**
   * The unique identifier of the user sending the request.
   */
  private String userId;

  /**
   * The unique identifier for the auction auctionId.
   */
  private String auctionId;

  /**
   * The display name of the auction item.
   */
  private String name;

  /**
   * A detailed description of the auction item.
   */
  private String description;

  /**
   * The category the item belongs to (e.g., ELECTRONICS, ART).
   */
  private String category;

  /**
   * The physical condition of the item.
   */
  private String itemCondition;

  /**
   * The estimated starting or appraisal price of the item.
   */
  private double estimatedPrice;

  /**
   * The amount of the bid being placed.
   */
  private double bidAmount;

  /**
   * Flexible map for specific item attributes (Path B implementation).
   */
  private Map<String, String> extraAttributes;

  /**
   * Default constructor required for GSON deserialization.
   */
  public ClientMessage() {
  }

  public String getAction() {
    return action;
  }

  public String getUserId() {
    return userId;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public String getItemCondition() {
    return itemCondition;
  }

  public double getEstimatedPrice() {
    return estimatedPrice;
  }

  public double getBidAmount() {
    return bidAmount;
  }

  public Map<String, String> getExtraAttributes() {
    return extraAttributes;
  }
}