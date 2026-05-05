package com.auction.models;

/**
 * Lớp đại diện cho người bán (Seller) trong hệ thống đấu giá.
 */
public class Seller extends User {

  private double rating;

  /**
   * Khởi tạo một đối tượng người bán mới với mức đánh giá mặc định là 5.0.
   *
   * @param username Tên đăng nhập của người bán
   * @param password Mật khẩu của người bán
   */
  public Seller(String username, String password) {
    super(username, password);
    this.rating = 5.0;
  }

  /**
   * Lấy điểm đánh giá hiện tại của người bán.
   *
   * @return Điểm đánh giá
   */
  public double getRating() {
    return rating;
  }

  /**
   * Thiết lập điểm đánh giá cho người bán.
   *
   * @param rating Điểm đánh giá mới cần thiết lập
   */
  public void setRating(double rating) {
    this.rating = rating;
  }

  /**
   * In thông tin chi tiết của người bán ra màn hình console.
   */
  @Override
  public void printInfo() {
    System.out.println("[Seller] " + getUsername() + " - Rating: " + rating + " sao");
  }
}