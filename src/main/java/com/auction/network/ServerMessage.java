package com.auction.network;

import com.auction.models.user.UserRole;
import java.util.List;

/**
 * Dữ liệu gửi từ backend về frontend (Response).
 */
public class ServerMessage {

  private String action;
  private String status;
  private String message;
  private String userId;
  private UserRole role;
  private String auctionId;
  private double currentPrice;
  private String currentLeaderId;
  private String endTime;
  private Object item;
  private List<Object> auctions;

  /**
   * Constructor mặc định cho JSON parser.
   */
  public ServerMessage() {
    // Constructor rỗng dành cho các thư viện parse JSON như Gson/Jackson
  }

  /**
   * Tạo builder cho ServerMessage.
   *
   * @return builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Lấy action của message.
   *
   * @return action
   */
  public String getAction() {
    return action;
  }

  /**
   * Cập nhật action cho message.
   *
   * @param action action cần gán
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Lấy trạng thái của message.
   *
   * @return status (SUCCESS hoặc ERROR)
   */
  public String getStatus() {
    return status;
  }

  /**
   * Cập nhật trạng thái cho message.
   *
   * @param status trạng thái cần gán
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Lấy thông báo (thường dùng cho lỗi).
   *
   * @return thông báo
   */
  public String getMessage() {
    return message;
  }

  /**
   * Cập nhật thông báo cho message.
   *
   * @param message thông báo cần gán
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Lấy mã người dùng.
   *
   * @return user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Cập nhật mã người dùng.
   *
   * @param userId mã người dùng cần gán
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Lấy vai trò của người dùng.
   *
   * @return role
   */
  public UserRole getRole() {
    return role;
  }

  /**
   * Cập nhật vai trò của người dùng.
   *
   * @param role vai trò cần gán
   */
  public void setRole(UserRole role) {
    this.role = role;
  }

  /**
   * Lấy mã phiên đấu giá.
   *
   * @return auction id
   */
  public String getAuctionId() {
    return auctionId;
  }

  /**
   * Cập nhật mã phiên đấu giá.
   *
   * @param auctionId mã phiên đấu giá cần gán
   */
  public void setAuctionId(String auctionId) {
    this.auctionId = auctionId;
  }

  /**
   * Lấy giá hiện tại (hoặc giá bid mới).
   *
   * @return giá hiện tại
   */
  public double getCurrentPrice() {
    return currentPrice;
  }

  /**
   * Cập nhật giá hiện tại (hoặc giá bid mới).
   *
   * @param currentPrice giá hiện tại cần gán
   */
  public void setCurrentPrice(double currentPrice) {
    this.currentPrice = currentPrice;
  }

  /**
   * Lấy mã người đang dẫn đầu phiên đấu giá.
   *
   * @return user id của leader
   */
  public String getCurrentLeaderId() {
    return currentLeaderId;
  }

  /**
   * Cập nhật mã người đang dẫn đầu phiên đấu giá.
   *
   * @param currentLeaderId mã leader cần gán
   */
  public void setCurrentLeaderId(String currentLeaderId) {
    this.currentLeaderId = currentLeaderId;
  }

  /**
   * Lấy thời gian kết thúc phiên đấu giá.
   *
   * @return thời gian kết thúc (chuỗi)
   */
  public String getEndTime() {
    return endTime;
  }

  /**
   * Cập nhật thời gian kết thúc phiên đấu giá.
   *
   * @param endTime thời gian kết thúc cần gán
   */
  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  /**
   * Lấy vật phẩm đấu giá.
   *
   * @return đối tượng item
   */
  public Object getItem() {
    return item;
  }

  /**
   * Cập nhật vật phẩm đấu giá.
   *
   * @param item đối tượng item cần gán
   */
  public void setItem(Object item) {
    this.item = item;
  }

  /**
   * Lấy danh sách các phiên đấu giá.
   *
   * @return danh sách auctions
   */
  public List<Object> getAuctions() {
    return auctions;
  }

  /**
   * Cập nhật danh sách các phiên đấu giá.
   *
   * @param auctions danh sách auctions cần gán
   */
  public void setAuctions(List<Object> auctions) {
    this.auctions = auctions;
  }

  /**
   * Kiểm tra xem phản hồi từ server có thành công hay không.
   *
   * @return true nếu trạng thái là SUCCESS, ngược lại là false
   */
  public boolean isSuccess() {
    return "SUCCESS".equalsIgnoreCase(this.status);
  }

  /**
   * Builder tạo ServerMessage.
   */
  public static final class Builder {

    private final ServerMessage message;

    /**
     * Constructor của builder.
     */
    private Builder() {
      this.message = new ServerMessage();
    }

    /**
     * Gán action cho message.
     *
     * @param action action
     * @return builder hiện tại
     */
    public Builder action(String action) {
      message.setAction(action);
      return this;
    }

    /**
     * Gán trạng thái cho message.
     *
     * @param status trạng thái
     * @return builder hiện tại
     */
    public Builder status(String status) {
      message.setStatus(status);
      return this;
    }

    /**
     * Gán thông báo cho message.
     *
     * @param errorMsg thông báo
     * @return builder hiện tại
     */
    public Builder message(String errorMsg) {
      message.setMessage(errorMsg);
      return this;
    }

    /**
     * Gán mã người dùng cho message.
     *
     * @param userId mã người dùng
     * @return builder hiện tại
     */
    public Builder userId(String userId) {
      message.setUserId(userId);
      return this;
    }

    /**
     * Gán vai trò người dùng cho message.
     *
     * @param role vai trò
     * @return builder hiện tại
     */
    public Builder role(UserRole role) {
      message.setRole(role);
      return this;
    }

    /**
     * Gán mã phiên đấu giá cho message.
     *
     * @param auctionId mã phiên đấu giá
     * @return builder hiện tại
     */
    public Builder auctionId(String auctionId) {
      message.setAuctionId(auctionId);
      return this;
    }

    /**
     * Gán giá hiện tại (hoặc giá bid mới) cho message.
     *
     * @param currentPrice giá hiện tại
     * @return builder hiện tại
     */
    public Builder currentPrice(double currentPrice) {
      message.setCurrentPrice(currentPrice);
      return this;
    }

    /**
     * Gán mã leader cho message.
     *
     * @param currentLeaderId mã leader
     * @return builder hiện tại
     */
    public Builder currentLeaderId(String currentLeaderId) {
      message.setCurrentLeaderId(currentLeaderId);
      return this;
    }

    /**
     * Gán thời gian kết thúc cho message.
     *
     * @param endTime thời gian kết thúc
     * @return builder hiện tại
     */
    public Builder endTime(String endTime) {
      message.setEndTime(endTime);
      return this;
    }

    /**
     * Gán vật phẩm đấu giá cho message.
     *
     * @param item vật phẩm đấu giá
     * @return builder hiện tại
     */
    public Builder item(Object item) {
      message.setItem(item);
      return this;
    }

    /**
     * Gán danh sách các phiên đấu giá cho message.
     *
     * @param auctions danh sách auctions
     * @return builder hiện tại
     */
    public Builder auctions(List<Object> auctions) {
      message.setAuctions(auctions);
      return this;
    }

    /**
     * Tạo ServerMessage hoàn chỉnh.
     *
     * @return server message
     */
    public ServerMessage build() {
      return message;
    }
  }
}