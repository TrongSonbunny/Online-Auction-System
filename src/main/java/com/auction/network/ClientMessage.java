package com.auction.network;

import com.auction.models.user.UserRole;
import java.io.Serializable;

/**
 * Dữ liệu nhận từ frontend sau khi Network parse JSON.
 * Cần implement Serializable để có thể truyền tải qua luồng mạng (Socket).
 */
public class ClientMessage implements Serializable {

  // Thêm serialVersionUID để đảm bảo tính đồng nhất khi truyền qua mạng
  private static final long serialVersionUID = 1L;

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

  /**
   * Constructor mặc định cho JSON parser.
   */
  public ClientMessage() {
  }

  /**
   * Tạo builder cho ClientMessage.
   *
   * @return builder
   */
  public static Builder builder() {
    return new Builder();
  }

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

  /**
   * Builder tạo ClientMessage.
   */
  public static final class Builder {

    private final ClientMessage message;

    private Builder() {
      this.message = new ClientMessage();
    }

    public Builder action(ActionType action) {
      message.setAction(action);
      return this;
    }

    public Builder userId(String userId) {
      message.setUserId(userId);
      return this;
    }

    public Builder role(UserRole role) {
      message.setRole(role);
      return this;
    }

    public Builder name(String name) {
      message.setName(name);
      return this;
    }

    public Builder email(String email) {
      message.setEmail(email);
      return this;
    }

    public Builder password(String password) {
      message.setPassword(password);
      return this;
    }

    public Builder auctionId(String auctionId) {
      message.setAuctionId(auctionId);
      return this;
    }

    public Builder bidAmount(double bidAmount) {
      message.setBidAmount(bidAmount);
      return this;
    }

    public Builder itemName(String itemName) {
      message.setItemName(itemName);
      return this;
    }

    public Builder itemDescription(String itemDescription) {
      message.setItemDescription(itemDescription);
      return this;
    }

    public Builder itemCategory(String itemCategory) {
      message.setItemCategory(itemCategory);
      return this;
    }

    public Builder itemCondition(String itemCondition) {
      message.setItemCondition(itemCondition);
      return this;
    }

    public Builder estimatedPrice(double estimatedPrice) {
      message.setEstimatedPrice(estimatedPrice);
      return this;
    }

    public Builder startingPrice(double startingPrice) {
      message.setStartingPrice(startingPrice);
      return this;
    }

    public Builder durationSeconds(long durationSeconds) {
      message.setDurationSeconds(durationSeconds);
      return this;
    }

    public ClientMessage build() {
      return message;
    }
  }
}