package com.auction.models;

/**
 * Lớp đại diện cho người quản trị (Admin) trong hệ thống đấu giá.
 */
public class Admin extends User {

  private String roleLevel;

  /**
   * Khởi tạo một đối tượng Admin mới.
   *
   * @param username  Tên đăng nhập của admin
   * @param password  Mật khẩu của admin
   * @param roleLevel Cấp bậc quyền hạn của admin
   */
  public Admin(String username, String password, String roleLevel) {
    super(username, password);
    this.roleLevel = roleLevel;
  }

  /**
   * Lấy cấp bậc quyền hạn của admin.
   *
   * @return Cấp bậc hiện tại của admin
   */
  public String getRoleLevel() {
    return roleLevel;
  }

  /**
   * Thiết lập cấp bậc quyền hạn cho admin.
   *
   * @param roleLevel Cấp bậc mới cần thiết lập
   */
  public void setRoleLevel(String roleLevel) {
    this.roleLevel = roleLevel;
  }

  /**
   * In thông tin chi tiết của admin ra màn hình console.
   */
  @Override
  public void printInfo() {
    System.out.println("[Admin] " + getUsername() + " - Cấp bậc: " + roleLevel);
  }
}