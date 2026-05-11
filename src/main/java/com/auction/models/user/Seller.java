package com.auction.models.user;

import com.auction.models.user.permission.SellerPermission;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User seller.
 */
public class Seller extends User {

  private final AtomicInteger totalAuctionsCreated;

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

    this.totalAuctionsCreated = new AtomicInteger(0);
  }

  /**
   * Tăng số auction đã tạo.
   */
  public void incrementAuctionCreated() {
    totalAuctionsCreated.incrementAndGet();
  }

  public int getTotalAuctionsCreated() {
    return totalAuctionsCreated.get();
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