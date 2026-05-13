package com.auction.network;

import com.auction.models.user.UserRole;

/**
 * Dữ liệu nhận từ frontend sau khi Network parse JSON.
 */
public class ClientMessage {

  private ActionType action;

  private String userId;

  private UserRole role;

  private String name;

  private String email;

  private String password;

  private String auctionId;

  private double bidAmount;

  private String itemName;

  private String itemDescription;

  private String itemCategory;

  private String itemCondition;

  private double estimatedPrice;

  private double startingPrice;

  private long durationSeconds;

  public ActionType getAction() {
    return action;
  }

  public void setAction(ActionType action) {
    this.action = action;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public void setAuctionId(String auctionId) {
    this.auctionId = auctionId;
  }

  public double getBidAmount() {
    return bidAmount;
  }

  public void setBidAmount(double bidAmount) {
    this.bidAmount = bidAmount;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public String getItemDescription() {
    return itemDescription;
  }

  public void setItemDescription(String itemDescription) {
    this.itemDescription = itemDescription;
  }

  public String getItemCategory() {
    return itemCategory;
  }

  public void setItemCategory(String itemCategory) {
    this.itemCategory = itemCategory;
  }

  public String getItemCondition() {
    return itemCondition;
  }

  public void setItemCondition(String itemCondition) {
    this.itemCondition = itemCondition;
  }

  public double getEstimatedPrice() {
    return estimatedPrice;
  }

  public void setEstimatedPrice(double estimatedPrice) {
    this.estimatedPrice = estimatedPrice;
  }

  public double getStartingPrice() {
    return startingPrice;
  }

  public void setStartingPrice(double startingPrice) {
    this.startingPrice = startingPrice;
  }

  public long getDurationSeconds() {
    return durationSeconds;
  }

  public void setDurationSeconds(long durationSeconds) {
    this.durationSeconds = durationSeconds;
  }
}