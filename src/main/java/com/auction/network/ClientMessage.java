package com.auction.network;

import com.auction.models.user.UserRole;
import java.io.Serializable;

/**
 * Dữ liệu nhận từ frontend sau khi Network parse JSON.
 * Cần implement Serializable để có thể truyền tải qua luồng mạng (Socket).
 */
public class ClientMessage implements Serializable {

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
   * @return Đối tượng Builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Lấy loại hành động (Action).
   *
   * @return Loại hành động
   */
  public ActionType getAction() {
    return action;
  }

  /**
   * Cài đặt loại hành động.
   *
   * @param action Loại hành động
   */
  public void setAction(ActionType action) {
    this.action = action;
  }

  /**
   * Lấy mã người dùng.
   *
   * @return Mã người dùng
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Cài đặt mã người dùng.
   *
   * @param userId Mã người dùng
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Lấy vai trò người dùng.
   *
   * @return Vai trò người dùng
   */
  public UserRole getRole() {
    return role;
  }

  /**
   * Cài đặt vai trò người dùng.
   *
   * @param role Vai trò người dùng
   */
  public void setRole(UserRole role) {
    this.role = role;
  }

  /**
   * Lấy tên người dùng.
   *
   * @return Tên người dùng
   */
  public String getName() {
    return name;
  }

  /**
   * Cài đặt tên người dùng.
   *
   * @param name Tên người dùng
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Lấy email người dùng.
   *
   * @return Email người dùng
   */
  public String getEmail() {
    return email;
  }

  /**
   * Cài đặt email người dùng.
   *
   * @param email Email người dùng
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Lấy mật khẩu người dùng.
   *
   * @return Mật khẩu người dùng
   */
  public String getPassword() {
    return password;
  }

  /**
   * Cài đặt mật khẩu người dùng.
   *
   * @param password Mật khẩu người dùng
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Lấy mã phiên đấu giá.
   *
   * @return Mã phiên đấu giá
   */
  public String getAuctionId() {
    return auctionId;
  }

  /**
   * Cài đặt mã phiên đấu giá.
   *
   * @param auctionId Mã phiên đấu giá
   */
  public void setAuctionId(String auctionId) {
    this.auctionId = auctionId;
  }

  /**
   * Lấy số tiền đặt giá.
   *
   * @return Số tiền đặt giá
   */
  public double getBidAmount() {
    return bidAmount;
  }

  /**
   * Cài đặt số tiền đặt giá.
   *
   * @param bidAmount Số tiền đặt giá
   */
  public void setBidAmount(double bidAmount) {
    this.bidAmount = bidAmount;
  }

  /**
   * Lấy tên sản phẩm.
   *
   * @return Tên sản phẩm
   */
  public String getItemName() {
    return itemName;
  }

  /**
   * Cài đặt tên sản phẩm.
   *
   * @param itemName Tên sản phẩm
   */
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /**
   * Lấy mô tả sản phẩm.
   *
   * @return Mô tả sản phẩm
   */
  public String getItemDescription() {
    return itemDescription;
  }

  /**
   * Cài đặt mô tả sản phẩm.
   *
   * @param itemDescription Mô tả sản phẩm
   */
  public void setItemDescription(String itemDescription) {
    this.itemDescription = itemDescription;
  }

  /**
   * Lấy danh mục sản phẩm.
   *
   * @return Danh mục sản phẩm
   */
  public String getItemCategory() {
    return itemCategory;
  }

  /**
   * Cài đặt danh mục sản phẩm.
   *
   * @param itemCategory Danh mục sản phẩm
   */
  public void setItemCategory(String itemCategory) {
    this.itemCategory = itemCategory;
  }

  /**
   * Lấy tình trạng sản phẩm.
   *
   * @return Tình trạng sản phẩm
   */
  public String getItemCondition() {
    return itemCondition;
  }

  /**
   * Cài đặt tình trạng sản phẩm.
   *
   * @param itemCondition Tình trạng sản phẩm
   */
  public void setItemCondition(String itemCondition) {
    this.itemCondition = itemCondition;
  }

  /**
   * Lấy giá ước tính của sản phẩm.
   *
   * @return Giá ước tính
   */
  public double getEstimatedPrice() {
    return estimatedPrice;
  }

  /**
   * Cài đặt giá ước tính của sản phẩm.
   *
   * @param estimatedPrice Giá ước tính
   */
  public void setEstimatedPrice(double estimatedPrice) {
    this.estimatedPrice = estimatedPrice;
  }

  /**
   * Lấy giá khởi điểm.
   *
   * @return Giá khởi điểm
   */
  public double getStartingPrice() {
    return startingPrice;
  }

  /**
   * Cài đặt giá khởi điểm.
   *
   * @param startingPrice Giá khởi điểm
   */
  public void setStartingPrice(double startingPrice) {
    this.startingPrice = startingPrice;
  }

  /**
   * Lấy thời lượng đấu giá tính bằng giây.
   *
   * @return Thời lượng (giây)
   */
  public long getDurationSeconds() {
    return durationSeconds;
  }

  /**
   * Cài đặt thời lượng đấu giá.
   *
   * @param durationSeconds Thời lượng (giây)
   */
  public void setDurationSeconds(long durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  /**
   * Lớp Builder hỗ trợ tạo ClientMessage linh hoạt.
   */
  public static final class Builder {

    private final ClientMessage message;

    /**
     * Khởi tạo Builder mới.
     */
    private Builder() {
      this.message = new ClientMessage();
    }

    /**
     * Gán loại hành động.
     *
     * @param action Loại hành động
     * @return Đối tượng Builder hiện tại
     */
    public Builder action(ActionType action) {
      message.setAction(action);
      return this;
    }

    /**
     * Gán mã người dùng.
     *
     * @param userId Mã người dùng
     * @return Đối tượng Builder hiện tại
     */
    public Builder userId(String userId) {
      message.setUserId(userId);
      return this;
    }

    /**
     * Gán vai trò người dùng.
     *
     * @param role Vai trò người dùng
     * @return Đối tượng Builder hiện tại
     */
    public Builder role(UserRole role) {
      message.setRole(role);
      return this;
    }

    /**
     * Gán tên người dùng.
     *
     * @param name Tên người dùng
     * @return Đối tượng Builder hiện tại
     */
    public Builder name(String name) {
      message.setName(name);
      return this;
    }

    /**
     * Gán email người dùng.
     *
     * @param email Email người dùng
     * @return Đối tượng Builder hiện tại
     */
    public Builder email(String email) {
      message.setEmail(email);
      return this;
    }

    /**
     * Gán mật khẩu người dùng.
     *
     * @param password Mật khẩu người dùng
     * @return Đối tượng Builder hiện tại
     */
    public Builder password(String password) {
      message.setPassword(password);
      return this;
    }

    /**
     * Gán mã phiên đấu giá.
     *
     * @param auctionId Mã phiên đấu giá
     * @return Đối tượng Builder hiện tại
     */
    public Builder auctionId(String auctionId) {
      message.setAuctionId(auctionId);
      return this;
    }

    /**
     * Gán số tiền đặt giá.
     *
     * @param bidAmount Số tiền đặt giá
     * @return Đối tượng Builder hiện tại
     */
    public Builder bidAmount(double bidAmount) {
      message.setBidAmount(bidAmount);
      return this;
    }

    /**
     * Gán tên sản phẩm.
     *
     * @param itemName Tên sản phẩm
     * @return Đối tượng Builder hiện tại
     */
    public Builder itemName(String itemName) {
      message.setItemName(itemName);
      return this;
    }

    /**
     * Gán mô tả sản phẩm.
     *
     * @param itemDescription Mô tả sản phẩm
     * @return Đối tượng Builder hiện tại
     */
    public Builder itemDescription(String itemDescription) {
      message.setItemDescription(itemDescription);
      return this;
    }

    /**
     * Gán danh mục sản phẩm.
     *
     * @param itemCategory Danh mục sản phẩm
     * @return Đối tượng Builder hiện tại
     */
    public Builder itemCategory(String itemCategory) {
      message.setItemCategory(itemCategory);
      return this;
    }

    /**
     * Gán tình trạng sản phẩm.
     *
     * @param itemCondition Tình trạng sản phẩm
     * @return Đối tượng Builder hiện tại
     */
    public Builder itemCondition(String itemCondition) {
      message.setItemCondition(itemCondition);
      return this;
    }

    /**
     * Gán giá ước tính.
     *
     * @param estimatedPrice Giá ước tính
     * @return Đối tượng Builder hiện tại
     */
    public Builder estimatedPrice(double estimatedPrice) {
      message.setEstimatedPrice(estimatedPrice);
      return this;
    }

    /**
     * Gán giá khởi điểm.
     *
     * @param startingPrice Giá khởi điểm
     * @return Đối tượng Builder hiện tại
     */
    public Builder startingPrice(double startingPrice) {
      message.setStartingPrice(startingPrice);
      return this;
    }

    /**
     * Gán thời lượng đấu giá.
     *
     * @param durationSeconds Thời lượng (giây)
     * @return Đối tượng Builder hiện tại
     */
    public Builder durationSeconds(long durationSeconds) {
      message.setDurationSeconds(durationSeconds);
      return this;
    }

    /**
     * Hoàn thành và trả về đối tượng ClientMessage đã được xây dựng.
     *
     * @return Đối tượng ClientMessage
     */
    public ClientMessage build() {
      return message;
    }
  }
}