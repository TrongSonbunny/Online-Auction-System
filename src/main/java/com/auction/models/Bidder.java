package com.auction.models;

/**
 * Lớp đại diện cho người tham gia đấu giá (Bidder) trong hệ thống.
 */
public class Bidder extends User {

  private double balance;

  /**
   * Khởi tạo một đối tượng người đấu giá mới.
   *
   * @param username Tên đăng nhập của người đấu giá
   * @param password Mật khẩu của người đấu giá
   * @param balance  Số dư tài khoản ban đầu
   */
  public Bidder(String username, String password, double balance) {
    super(username, password);
    this.balance = balance;
  }

  /**
   * Lấy số dư tài khoản hiện tại của người đấu giá.
   *
   * @return Số dư tài khoản
   */
  public double getBalance() {
    return balance;
  }

  /**
   * Thiết lập số dư tài khoản mới cho người đấu giá.
   *
   * @param balance Số dư mới cần thiết lập
   */
  public void setBalance(double balance) {
    this.balance = balance;
  }

  /**
   * In thông tin chi tiết của người đấu giá ra màn hình.
   */
  @Override
  public void printInfo() {
    System.out.println("[Bidder] " + getUsername() + " - Số dư: $" + balance);
  }
}