package com.auction.backend.auth;

import com.auction.backend.database.dao.UserDao;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.user.User;
import com.auction.models.user.UserFactory;
import com.auction.network.ClientMessage;

/**
 * Service xử lý đăng nhập và đăng ký.
 */
public class AuthService {

  private final UserDao userDao;

  /**
   * Constructor auth service.
   */
  public AuthService() {
    this.userDao = new UserDao();
  }

  /**
   * Đăng ký user mới.
   *
   * @param message dữ liệu từ client
   * @return user vừa tạo
   */
  public User register(
      ClientMessage message) {

    validateRegisterMessage(message);

    if (userDao.existsByEmail(
        message.getEmail())) {

      throw new AuctionException(
          "Email đã tồn tại.");
    }

    User user =
        UserFactory.createUser(
            message.getRole(),
            message.getName(),
            message.getEmail());

    userDao.saveUser(
        user,
        message.getPassword());

    return user;
  }

  /**
   * Đăng nhập.
   *
   * @param message dữ liệu từ client
   * @return user đăng nhập thành công
   */
  public User login(
      ClientMessage message) {

    validateLoginMessage(message);

    User user =
        userDao.findByEmail(
            message.getEmail());

    if (user == null) {
      throw new UnauthorizedException(
          "Email không tồn tại.");
    }

    boolean correctPassword =
        userDao.checkPassword(
            message.getEmail(),
            message.getPassword());

    if (!correctPassword) {
      throw new UnauthorizedException(
          "Sai mật khẩu.");
    }

    return user;
  }

  private void validateRegisterMessage(
      ClientMessage message) {

    if (message == null) {
      throw new AuctionException(
          "ClientMessage không được null.");
    }

    if (message.getRole() == null) {
      throw new AuctionException(
          "Role không được null.");
    }

    if (message.getName() == null
        || message.getName().isBlank()) {

      throw new AuctionException(
          "Tên không hợp lệ.");
    }

    validateLoginMessage(message);
  }

  private void validateLoginMessage(
      ClientMessage message) {

    if (message == null) {
      throw new AuctionException(
          "ClientMessage không được null.");
    }

    if (message.getEmail() == null
        || message.getEmail().isBlank()
        || !message.getEmail().contains("@")) {

      throw new AuctionException(
          "Email không hợp lệ.");
    }

    if (message.getPassword() == null
        || message.getPassword().isBlank()) {

      throw new AuctionException(
          "Password không hợp lệ.");
    }
  }
}