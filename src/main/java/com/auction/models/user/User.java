package com.auction.models.user;

import com.auction.exceptions.BidException;
import com.auction.models.user.permission.PermissionStrategy;
import java.io.Serializable;
import java.util.Objects;

/**
 * Lớp cơ sở trừu tượng cho mọi loại user trong hệ thống.
 *
 * <p>
 * Xác định danh tính (userId, name, email, role) và ủy quyền hành động
 * qua {@link com.auction.models.user.permission.PermissionStrategy} (Strategy
 * pattern).
 * Subclass: {@link Bidder}, {@link Seller}, {@link Admin}.
 */
public abstract class User implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String userId;
  private String name;
  private String email;
  private final UserRole role;
  private final transient PermissionStrategy permissionStrategy;

  /**
   * Constructor tạo user.
   *
   * @param userId             mã user
   * @param name               tên user
   * @param email              email
   * @param role               role user
   * @param permissionStrategy strategy quyền
   */
  protected User(
      String userId,
      String name,
      String email,
      UserRole role,
      PermissionStrategy permissionStrategy) {

    validateUserId(userId);
    validateName(name);
    validateEmail(email);

    this.userId = userId;
    this.name = name;
    this.email = email;
    this.role = Objects.requireNonNull(
        role,
        "Role không được null.");

    this.permissionStrategy = Objects.requireNonNull(
        permissionStrategy,
        "Permission strategy không được null.");
  }

  /**
   * Kiểm tra quyền tạo auction.
   *
   * @return true nếu có quyền
   */
  public boolean canCreateAuction() {
    return permissionStrategy.canCreateAuction();
  }

  /**
   * Kiểm tra quyền đặt giá.
   *
   * @return true nếu có quyền
   */
  public boolean canPlaceBid() {
    return permissionStrategy.canPlaceBid();
  }

  /**
   * Kiểm tra quyền xóa auction.
   *
   * @return true nếu có quyền
   */
  public boolean canDeleteAuction() {
    return permissionStrategy.canDeleteAuction();
  }

  /**
   * Kiểm tra quyền khóa user.
   *
   * @return true nếu có quyền
   */
  public boolean canBanUser() {
    return permissionStrategy.canBanUser();
  }

  /**
   * Kiểm tra quyền quản lý hệ thống.
   *
   * @return true nếu có quyền
   */
  public boolean canManageSystem() {
    return permissionStrategy.canManageSystem();
  }

  /**
   * Validate userId.
   *
   * @param id userId
   */
  private void validateUserId(String id) {
    if (id == null || id.isBlank()) {
      throw new BidException("UserId không hợp lệ.");
    }
  }

  /**
   * Validate name.
   *
   * @param userName tên user
   */
  private void validateName(String userName) {
    if (userName == null || userName.isBlank()) {
      throw new BidException("Tên user không hợp lệ.");
    }
  }

  /**
   * Validate email.
   *
   * @param userEmail email
   */
  private void validateEmail(String userEmail) {
    if (userEmail == null || userEmail.isBlank() || !userEmail.contains("@")) {
      throw new BidException("Email không hợp lệ.");
    }
  }

  /**
   * Lấy mã userId.
   *
   * @return String chứa userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Lấy tên user.
   *
   * @return String chứa tên
   */
  public String getName() {
    return name;
  }

  /**
   * Lấy email user.
   *
   * @return String chứa email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Lấy quyền (Role) của user.
   *
   * @return UserRole của user
   */
  public UserRole getRole() {
    return role;
  }

  /**
   * Cập nhật tên user.
   *
   * @param newName tên mới
   */
  public void setName(String newName) {
    validateName(newName);
    this.name = newName;
  }

  /**
   * Cập nhật email.
   *
   * @param newEmail email mới
   */
  public void setEmail(String newEmail) {
    validateEmail(newEmail);
    this.email = newEmail;
  }

  @Override
  public String toString() {
    return "User{"
        + "userId='" + userId + '\''
        + ", name='" + name + '\''
        + ", email='" + email + '\''
        + ", role=" + role
        + '}';
  }
}