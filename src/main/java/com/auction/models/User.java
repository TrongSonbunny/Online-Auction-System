package com.auction.models;

/**
 * Lớp cơ sở trừu tượng đại diện cho một người dùng trong hệ thống.
 */
public abstract class User extends Entity {

  protected String username;
  protected String password;

  /**
   * Khởi tạo một người dùng mới.
   *
   * @param username Tên đăng nhập của người dùng
   * @param password Mật khẩu của người dùng
   */
  public User(String username, String password) {
    super();
    this.username = username;
    this.password = password;
  }

  /**
   * Lấy tên đăng nhập của người dùng.
   *
   * @return Tên đăng nhập
   */
  public String getUsername() {
    return username;
  }

  /**
   * Thiết lập tên đăng nhập cho người dùng.
   *
   * @param username Tên đăng nhập mới cần thiết lập
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Thiết lập mật khẩu cho người dùng.
   *
   * @param password Mật khẩu mới cần thiết lập
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * In thông tin chi tiết của người dùng ra màn hình.
   * Các lớp con kế thừa phải tự triển khai phương thức này.
   */
  public abstract void printInfo();
}