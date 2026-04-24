package com.auction.backend;

/**
 * Lớp trừu tượng đại diện cho người dùng trong hệ thống đấu giá.
 * Chứa các thuộc tính chung và triển khai IAuctionObserver để nhận thông báo.
 *
 * <p>Cây kế thừa:
 * User (abstract)
 *   ├── Bidder (người mua)
 *   └── Seller (người bán)
 *   └── Admin (quản trị viên)
 */
public abstract class User implements AuctionObserver {

  private final String userId; // Mã định danh duy nhất
  private String name; // Họ tên đầy đủ
  private String email; // Địa chỉ email (dùng để đăng nhập)
  private String passwordHash; // Mật khẩu đã được hash (không lưu plaintext)
  private String phoneNumber; // Số điện thoại liên hệ

  /** Constructor khởi tạo người dùng. */
  public User(String userId, String name, String email, String passwordHash) {
    this.userId = userId;
    this.name = name;
    this.email = email;
    this.passwordHash = passwordHash;
  }

  /**
   * Mỗi loại User sẽ xử lý thông báo theo cách riêng.
   * Bidder: cập nhật GUI khi có người khác trả giá cao hơn.
   * Seller: nhận thông báo khi phiên chuyển sang FINISHED.
   *
   * @param event Sự kiện đấu giá được gửi đến Observer
   */
  @Override
  public abstract void update(AuctionEvent event);

  /**
   * Trả về vai trò của người dùng trong hệ thống.
   * Mỗi lớp con phải định nghĩa vai trò của mình.
   *
   * @return Chuỗi mô tả vai trò: "BIDDER", "SELLER", "ADMIN"
   */
  public abstract String getRole();

  // ==================== Getter & Setter ====================

  public String getUserId() {
    return userId;
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

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  @Override
  public String toString() {
    return getRole()
        + "{"
        + "userId='"
        + userId
        + '\''
        + ", name='"
        + name
        + '\''
        + ", email='"
        + email
        + '\''
        + '}';
  }
}
