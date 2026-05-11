package com.auction.models.user;

import com.auction.models.user.permission.AdminPermission;

/**
 * User admin.
 */
public class Admin extends User {

  /**
   * Constructor admin.
   *
   * @param userId mã admin
   * @param name tên admin
   * @param email email admin
   */
  public Admin(
      String userId,
      String name,
      String email) {

    super(
        userId,
        name,
        email,
        UserRole.ADMIN,
        new AdminPermission());
  }
}