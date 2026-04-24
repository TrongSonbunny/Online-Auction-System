package com.auction.models.user;

import com.auction.models.item.AuctionItem;
import com.auction.models.permission.AdminPermission;
import com.auction.models.permission.BidderPermission;
import com.auction.models.permission.Permission;
import com.auction.models.permission.PermissionStrategy;
import com.auction.models.permission.SellerPermission;

/**
 * Lớp đại diện cho người dùng trong hệ thống.
 */
public class User {

  private String id;
  private String username;
  private Role role;
  private PermissionStrategy permissionStrategy;

  /**
   * Khởi tạo user.
   */
  public User(String id, String username, Role role) {
    this.id = id;
    this.username = username;
    setRole(role); 
  }

  //  GETTER 
  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public Role getRole() {
    return role;
  }

  public PermissionStrategy getPermissionStrategy() {
    return permissionStrategy;
  }

  // SETTER
  public void setUsername(String username) {
    this.username = username;
  }

  public void setRole(Role role) {
    this.role = role;
    this.permissionStrategy = initStrategy(role); // update strategy theo role
  }

  // LOGIC 
  private PermissionStrategy initStrategy(Role role) {
    switch (role) {
      case BIDDER: return new BidderPermission();
      case SELLER: return new SellerPermission();
      case ADMIN: return new AdminPermission();
      default: throw new IllegalArgumentException("Invalid role");
    }
  }

  public boolean can(Permission permission, AuctionItem item) {
    return permissionStrategy.hasPermission(permission, this, item);
  }
}