package com.auction.models.user;

import com.auction.models.user.permission.SellerPermission;

/**
 * User seller.
 */
public class Seller extends User {

  private int totalAuctionsCreated;

  /**
   * Constructor seller.
   *
   * @param userId mã seller
   * @param name tên seller
   * @param email email seller
   */
  public Seller(
      String userId,
      String name,
      String email) {

    super(
        userId,
        name,
        email,
        UserRole.SELLER,
        new SellerPermission());

    this.totalAuctionsCreated = 0;
  }

  /**
   * Tăng số auction đã tạo.
   */
  public void incrementAuctionCreated() {
    totalAuctionsCreated++;
  }

  public int getTotalAuctionsCreated() {
    return totalAuctionsCreated;
  }

  @Override
  public String toString() {

    return "Seller{"
        + "totalAuctionsCreated="
        + totalAuctionsCreated
        + "} "
        + super.toString();
  }
}