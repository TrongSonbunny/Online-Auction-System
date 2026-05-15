package com.auction.models;

import com.auction.models.item.AuctionItem;
import com.google.gson.annotations.SerializedName;

/**
 * Lớp DTO đại diện cho mọi gói tin JSON giao tiếp giữa Client và Server.
 * Sử dụng SerializedName để ép kiểu tên biến JSON đúng với tài liệu API.
 */
public class Message {

  @SerializedName(value = "action", alternate = { "type" })
  private String action;

  private String role;
  private String status;
  private String userId;

  @SerializedName("userName")
  private String username;

  @SerializedName("passWord")
  private String password;

  @SerializedName(value = "auctionId", alternate = { "auctionID" })
  private String auctionId;

  private Double bidAmount;
  private AuctionItem item;

  private Object data;

  /**
   * Constructor mặc định cho Gson.
   */
  public Message() {
  }

  // --- GETTERS & SETTERS ---

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
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

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  public Double getBidAmount() {
    return bidAmount;
  }

  public void setBidAmount(Double bidAmount) {
    this.bidAmount = bidAmount;
  }

  public AuctionItem getItem() {
    return item;
  }

  public void setItem(AuctionItem item) {
    this.item = item;
  }
}