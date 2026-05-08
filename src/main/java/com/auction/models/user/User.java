package com.auction.models.user;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionObserver;

/**
 * Lớp trừu tượng đại diện cho người dùng trong hệ thống đấu giá.
 * Chứa các thuộc tính chung và triển khai AuctionObserver để nhận thông báo.
 */
public abstract class User implements AuctionObserver {

  private final String userId;
  private String name;
  private String email;
  private String passwordHash;
  private String phoneNumber;

  /**
   * Khởi tạo một người dùng mới.
   *
   * @param userId mã định danh duy nhất
   * @param name tên hiển thị
   * @param email địa chỉ email
   * @param passwordHash mật khẩu đã được mã hóa
   */
  public User(String userId, String name, String email, String passwordHash) {
    this.userId = userId;
    this.name = name;
    this.email = email;
    this.passwordHash = passwordHash;
  }

  /**
   * Nhận thông báo khi có sự kiện đấu giá xảy ra.
   * Các lớp con (Bidder, Seller, Admin) sẽ triển khai logic xử lý riêng.
   *
   * @param event đối tượng chứa thông tin sự kiện
   */
  @Override
  public abstract void update(AuctionEvent event);

  /**
   * Lấy vai trò của người dùng trong hệ thống.
   *
   * @return chuỗi đại diện cho vai trò (ví dụ: "BIDDER", "SELLER")
   */
  public abstract String getRole();

  /*
  * @return mã định danh người dùng. */
  public String getUserId() {
    return userId;
  }

  /*
  * @return tên hiển thị của người dùng. */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /*
  * @return địa chỉ email đăng ký. */
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  /*
  * @return mã băm mật khẩu. */
  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  /*
  * @return số điện thoại liên lạc. */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  @Override
  public String toString() {
    return getRole()
        + "{userId='" + userId + '\''
        + ", name='" + name + '\''
        + ", email='" + email + '\'' + '}';
  }
}