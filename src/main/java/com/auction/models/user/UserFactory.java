package com.auction.models.user;

import com.auction.exceptions.AuctionException;
import com.auction.models.payment.MomoPayment;
import java.util.UUID;

/**
 * Factory tạo user theo role.
 */
public final class UserFactory {

  /**
   * Private constructor.
   */
  private UserFactory() {
  }

  /**
   * Tạo user mới.
   *
   * @param role role
   * @param name tên user
   * @param email email
   * @return user
   */
  public static User createUser(
      UserRole role,
      String name,
      String email) {

    return createUserWithId(
        generateUserId(),
        role,
        name,
        email);
  }

  /**
   * Tạo user với id có sẵn.
   *
   * @param userId mã user
   * @param role role
   * @param name tên user
   * @param email email
   * @return user
   */
  public static User createUserWithId(
      String userId,
      UserRole role,
      String name,
      String email) {

    if (role == null) {
      throw new AuctionException(
          "Role không được null.");
    }

    switch (role) {
      case ADMIN:
        return new Admin(
            userId,
            name,
            email);

      case SELLER:
        return new Seller(
            userId,
            name,
            email);

      case BIDDER:
        return new Bidder(
            userId,
            name,
            email,
            new MomoPayment(
                "0000000000",
                "Default Bidder"));

      default:
        throw new AuctionException(
            "Role không hợp lệ.");
    }
  }

  /**
   * Sinh user id.
   *
   * @return user id
   */
  private static String generateUserId() {

    return "USER-"
        + UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .toUpperCase();
  }
}