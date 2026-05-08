package com.auction.models.user;

import com.auction.models.observer.AuctionEvent;
import com.auction.models.observer.AuctionObserver;

/**
 * Lớp trừu tượng đại diện cho người dùng trong hệ thống đấu giá.
 * Chứa các thuộc tính chung và triển khai IAuctionObserver để nhận thông báo.
 *
 * <p>Cây kế thừa:
 * User (abstract)
 * ├── Bidder (người mua)
 * ├── Seller (người bán)
 * └── Admin (quản trị viên)
 */
public abstract class User implements AuctionObserver {

    private final String userId;
    private String name;
    private String email;
    private String passwordHash;
    private String phoneNumber;

    public User(String userId, String name, String email, String passwordHash) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @Override
    public abstract void update(AuctionEvent event);

    public abstract String getRole();

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return getRole()
                + "{userId='" + userId + '\''
                + ", name='" + name + '\''
                + ", email='" + email + '\'' + '}';
    }
}