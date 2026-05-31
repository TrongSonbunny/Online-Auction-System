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

  private String autoBidId;

  private double maxBid;

  private double increment;

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

  /**
   * Lấy action client yêu cầu.
   *
   * @return action
   */
  public ActionType getAction() {
    return action;
  }

  /**
   * Cập nhật action client yêu cầu.
   *
   * @param action action
   */
  public void setAction(
      ActionType action) {

    this.action = action;
  }

  /**
   * Lấy mã user.
   *
   * @return user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Cập nhật mã user.
   *
   * @param userId mã user
   */
  public void setUserId(
      String userId) {

    this.userId = userId;
  }

  /**
   * Lấy role user.
   *
   * @return role user
   */
  public UserRole getRole() {
    return role;
  }

  /**
   * Cập nhật role user.
   *
   * @param role role user
   */
  public void setRole(
      UserRole role) {

    this.role = role;
  }

  /**
   * Lấy tên user.
   *
   * @return tên user
   */
  public String getName() {
    return name;
  }

  /**
   * Cập nhật tên user.
   *
   * @param name tên user
   */
  public void setName(
      String name) {

    this.name = name;
  }

  /**
   * Lấy email user.
   *
   * @return email user
   */
  public String getEmail() {
    return email;
  }

  /**
   * Cập nhật email user.
   *
   * @param email email user
   */
  public void setEmail(
      String email) {

    this.email = email;
  }

  /**
   * Lấy password.
   *
   * @return password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Cập nhật password.
   *
   * @param password password
   */
  public void setPassword(
      String password) {

    this.password = password;
  }

  /**
   * Lấy mã auction.
   *
   * @return auction id
   */
  public String getAuctionId() {
    return auctionId;
  }

  /**
   * Cập nhật mã auction.
   *
   * @param auctionId mã auction
   */
  public void setAuctionId(
      String auctionId) {

    this.auctionId = auctionId;
  }

  /**
   * Lấy số tiền bid thủ công.
   *
   * @return số tiền bid
   */
  public double getBidAmount() {
    return bidAmount;
  }

  /**
   * Cập nhật số tiền bid thủ công.
   *
   * @param bidAmount số tiền bid
   */
  public void setBidAmount(
      double bidAmount) {

    this.bidAmount = bidAmount;
  }

  /**
   * Lấy mã auto-bid.
   *
   * @return auto-bid id
   */
  public String getAutoBidId() {
    return autoBidId;
  }

  /**
   * Cập nhật mã auto-bid.
   *
   * @param autoBidId mã auto-bid
   */
  public void setAutoBidId(
      String autoBidId) {

    this.autoBidId = autoBidId;
  }

  /**
   * Lấy giá tối đa của auto-bid.
   *
   * @return giá tối đa
   */
  public double getMaxBid() {
    return maxBid;
  }

  /**
   * Cập nhật giá tối đa của auto-bid.
   *
   * @param maxBid giá tối đa
   */
  public void setMaxBid(
      double maxBid) {

    this.maxBid = maxBid;
  }

  /**
   * Lấy bước giá của auto-bid.
   *
   * @return bước giá
   */
  public double getIncrement() {
    return increment;
  }

  /**
   * Cập nhật bước giá của auto-bid.
   *
   * @param increment bước giá
   */
  public void setIncrement(
      double increment) {

    this.increment = increment;
  }

  /**
   * Lấy tên item.
   *
   * @return tên item
   */
  public String getItemName() {
    return itemName;
  }

  /**
   * Cập nhật tên item.
   *
   * @param itemName tên item
   */
  public void setItemName(
      String itemName) {

    this.itemName = itemName;
  }

  /**
   * Lấy mô tả item.
   *
   * @return mô tả item
   */
  public String getItemDescription() {
    return itemDescription;
  }

  /**
   * Cập nhật mô tả item.
   *
   * @param itemDescription mô tả item
   */
  public void setItemDescription(
      String itemDescription) {

    this.itemDescription = itemDescription;
  }

  /**
   * Lấy category item.
   *
   * @return category item
   */
  public String getItemCategory() {
    return itemCategory;
  }

  /**
   * Cập nhật category item.
   *
   * @param itemCategory category item
   */
  public void setItemCategory(
      String itemCategory) {

    this.itemCategory = itemCategory;
  }

  /**
   * Lấy tình trạng item.
   *
   * @return tình trạng item
   */
  public String getItemCondition() {
    return itemCondition;
  }

  /**
   * Cập nhật tình trạng item.
   *
   * @param itemCondition tình trạng item
   */
  public void setItemCondition(
      String itemCondition) {

    this.itemCondition = itemCondition;
  }

  /**
   * Lấy giá ước tính.
   *
   * @return giá ước tính
   */
  public double getEstimatedPrice() {
    return estimatedPrice;
  }

  /**
   * Cập nhật giá ước tính.
   *
   * @param estimatedPrice giá ước tính
   */
  public void setEstimatedPrice(
      double estimatedPrice) {

    this.estimatedPrice = estimatedPrice;
  }

  /**
   * Lấy giá khởi điểm.
   *
   * @return giá khởi điểm
   */
  public double getStartingPrice() {
    return startingPrice;
  }

  /**
   * Cập nhật giá khởi điểm.
   *
   * @param startingPrice giá khởi điểm
   */
  public void setStartingPrice(
      double startingPrice) {

    this.startingPrice = startingPrice;
  }

  /**
   * Lấy thời lượng auction theo giây.
   *
   * @return thời lượng auction theo giây
   */
  public long getDurationSeconds() {
    return durationSeconds;
  }

  /**
   * Cập nhật thời lượng auction theo giây.
   *
   * @param durationSeconds thời lượng auction theo giây
   */
  public void setDurationSeconds(
      long durationSeconds) {

    this.durationSeconds = durationSeconds;
  }

  /**
   * Builder tạo ClientMessage.
   */
  public static final class Builder {

    private final ClientMessage message;

    /**
     * Constructor builder.
     */
    private Builder() {
      this.message = new ClientMessage();
    }

    /**
     * Gán action cho message.
     *
     * @param action action
     * @return builder hiện tại
     */
    public Builder action(
        ActionType action) {

      message.setAction(action);
      return this;
    }

    /**
     * Gán user id cho message.
     *
     * @param userId mã user
     * @return builder hiện tại
     */
    public Builder userId(
        String userId) {

      message.setUserId(userId);
      return this;
    }

    /**
     * Gán role cho message.
     *
     * @param role role user
     * @return builder hiện tại
     */
    public Builder role(
        UserRole role) {

      message.setRole(role);
      return this;
    }

    /**
     * Gán tên user cho message.
     *
     * @param name tên user
     * @return builder hiện tại
     */
    public Builder name(
        String name) {

      message.setName(name);
      return this;
    }

    /**
     * Gán email cho message.
     *
     * @param email email user
     * @return builder hiện tại
     */
    public Builder email(
        String email) {

      message.setEmail(email);
      return this;
    }

    /**
     * Gán password cho message.
     *
     * @param password password
     * @return builder hiện tại
     */
    public Builder password(
        String password) {

      message.setPassword(password);
      return this;
    }

    /**
     * Gán auction id cho message.
     *
     * @param auctionId mã auction
     * @return builder hiện tại
     */
    public Builder auctionId(
        String auctionId) {

      message.setAuctionId(auctionId);
      return this;
    }

    /**
     * Gán số tiền bid cho message.
     *
     * @param bidAmount số tiền bid
     * @return builder hiện tại
     */
    public Builder bidAmount(
        double bidAmount) {

      message.setBidAmount(bidAmount);
      return this;
    }

    /**
     * Gán auto-bid id cho message.
     *
     * @param autoBidId mã auto-bid
     * @return builder hiện tại
     */
    public Builder autoBidId(
        String autoBidId) {

      message.setAutoBidId(autoBidId);
      return this;
    }

    /**
     * Gán giá tối đa cho auto-bid.
     *
     * @param maxBid giá tối đa
     * @return builder hiện tại
     */
    public Builder maxBid(
        double maxBid) {

      message.setMaxBid(maxBid);
      return this;
    }

    /**
     * Gán bước giá cho auto-bid.
     *
     * @param increment bước giá
     * @return builder hiện tại
     */
    public Builder increment(
        double increment) {

      message.setIncrement(increment);
      return this;
    }

    /**
     * Gán tên item cho message.
     *
     * @param itemName tên item
     * @return builder hiện tại
     */
    public Builder itemName(
        String itemName) {

      message.setItemName(itemName);
      return this;
    }

    /**
     * Gán mô tả item cho message.
     *
     * @param itemDescription mô tả item
     * @return builder hiện tại
     */
    public Builder itemDescription(
        String itemDescription) {

      message.setItemDescription(itemDescription);
      return this;
    }

    /**
     * Gán category item cho message.
     *
     * @param itemCategory category item
     * @return builder hiện tại
     */
    public Builder itemCategory(
        String itemCategory) {

      message.setItemCategory(itemCategory);
      return this;
    }

    /**
     * Gán tình trạng item cho message.
     *
     * @param itemCondition tình trạng item
     * @return builder hiện tại
     */
    public Builder itemCondition(
        String itemCondition) {

      message.setItemCondition(itemCondition);
      return this;
    }

    /**
     * Gán giá ước tính cho message.
     *
     * @param estimatedPrice giá ước tính
     * @return builder hiện tại
     */
    public Builder estimatedPrice(
        double estimatedPrice) {

      message.setEstimatedPrice(estimatedPrice);
      return this;
    }

    /**
     * Gán giá khởi điểm cho message.
     *
     * @param startingPrice giá khởi điểm
     * @return builder hiện tại
     */
    public Builder startingPrice(
        double startingPrice) {

      message.setStartingPrice(startingPrice);
      return this;
    }

    /**
     * Gán thời lượng auction theo giây cho message.
     *
     * @param durationSeconds thời lượng auction theo giây
     * @return builder hiện tại
     */
    public Builder durationSeconds(
        long durationSeconds) {

      message.setDurationSeconds(durationSeconds);
      return this;
    }

    /**
     * Tạo ClientMessage hoàn chỉnh.
     *
     * @return client message
     */
    public ClientMessage build() {
      return message;
    }
  }
}