package com.auction.models;

/**
 * Lớp đại diện cho sản phẩm nghệ thuật (Art) trong hệ thống đấu giá.
 */
public class Art extends Item {

  private String artist;
  private int creationYear;

  /**
   * Khởi tạo một sản phẩm nghệ thuật mới.
   *
   * @param name          Tên sản phẩm nghệ thuật
   * @param startingPrice Giá khởi điểm
   * @param artist        Tên tác giả/nghệ sĩ
   * @param creationYear  Năm sáng tác
   */
  public Art(String name, double startingPrice, String artist, int creationYear) {
    super(name, startingPrice); // Chỉ truyền name và startingPrice lên Item
    this.artist = artist;
    this.creationYear = creationYear;
  }

  /**
   * Lấy tên tác giả của tác phẩm.
   *
   * @return Tên tác giả
   */
  public String getArtist() {
    return artist;
  }

  /**
   * Thiết lập tên tác giả cho tác phẩm.
   *
   * @param artist Tên tác giả mới
   */
  public void setArtist(String artist) {
    this.artist = artist;
  }

  /**
   * Lấy năm sáng tác của tác phẩm.
   *
   * @return Năm sáng tác
   */
  public int getCreationYear() {
    return creationYear;
  }

  /**
   * Thiết lập năm sáng tác cho tác phẩm.
   *
   * @param creationYear Năm sáng tác mới
   */
  public void setCreationYear(int creationYear) {
    this.creationYear = creationYear;
  }

  /**
   * Lấy chuỗi thông tin chi tiết về tác phẩm nghệ thuật.
   *
   * @return Thông tin chi tiết của sản phẩm
   */
  @Override
  public String getItemDetails() {
    return "Nghệ thuật: " + name + " (Tác giả: " + artist + ", Năm: " + creationYear
        + ") - Giá khởi điểm: $" + startingPrice;
  }
}