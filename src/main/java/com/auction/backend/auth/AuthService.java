package com.auction.backend.auth;

import com.auction.backend.database.dao.UserDao;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.user.User;
import com.auction.models.user.UserFactory;
import com.auction.models.user.UserRole;
import com.auction.network.ClientMessage;

/**
 * Service xử lý các nghiệp vụ xác thực người dùng trong hệ thống đấu giá.
 *
 * <p>Class này chịu trách nhiệm đăng ký tài khoản mới và đăng nhập người dùng.
 * Dữ liệu người dùng được truy xuất và lưu trữ thông qua {@link UserDao}.
 */
public class AuthService {
  private static final String DEFAULT_ADMIN_USERNAME = "ADMIN";
  private static final String DEFAULT_ADMIN_EMAIL = "admin@auction.local";

  private final UserDao userDao;

  /**
   * Khởi tạo service xác thực với đối tượng truy cập dữ liệu người dùng.
   *
   * @param userDao đối tượng dùng để thao tác với dữ liệu người dùng
   * @throws AuctionException nếu {@code userDao} là {@code null}
   */
  public AuthService(UserDao userDao) {
    if (userDao == null) {
      throw new AuctionException("UserDao không được null.");
    }
    this.userDao = userDao;
  }

  /**
   * Đăng ký một người dùng mới dựa trên dữ liệu nhận từ client.
   *
   * <p>Phương thức sẽ kiểm tra tính hợp lệ của dữ liệu đăng ký, kiểm tra email
   * đã tồn tại hay chưa, sau đó tạo người dùng mới bằng {@link UserFactory}
   * và lưu thông tin người dùng vào cơ sở dữ liệu.
   *
   * <p>Không cho đăng ký tài khoản ADMIN từ client. Tài khoản ADMIN mặc định
   * được tạo sẵn khi khởi tạo database.
   *
   * @param message thông điệp từ client chứa role, tên, email và mật khẩu
   * @return người dùng vừa được tạo
   * @throws AuctionException nếu dữ liệu đăng ký không hợp lệ hoặc email đã tồn
   *                          tại
   */
  public User register(
      ClientMessage message) {
    validateRegisterMessage(message);
    validateNotAdminRegister(message);

    if (userDao.existsByEmail(
        message.getEmail())) {
      throw new AuctionException(
          "Email đã tồn tại.");
    }

    User user = UserFactory.createUser(
        message.getRole(),
        message.getName(),
        message.getEmail());

    userDao.saveUser(
        user,
        message.getPassword());

    return user;
  }

  /**
   * Đăng nhập người dùng dựa trên email/username và mật khẩu nhận từ client.
   *
   * <p>Với admin mặc định, client có thể nhập username là {@code ADMIN}.
   * Backend sẽ tự ánh xạ thành email nội bộ {@code admin@auction.local}.
   *
   * @param message thông điệp từ client chứa email hoặc username và mật khẩu
   * @return người dùng đăng nhập thành công
   * @throws AuctionException      nếu dữ liệu đăng nhập không hợp lệ
   * @throws UnauthorizedException nếu email không tồn tại hoặc mật khẩu không
   *                               đúng
   */
  public User login(
      ClientMessage message) {
    validateLoginMessage(message);

    User user = userDao.findByEmail(
        message.getEmail());

    if (user == null) {
      throw new UnauthorizedException(
          "Tài khoản không tồn tại.");
    }

    boolean correctPassword = userDao.checkPassword(
        message.getEmail(),
        message.getPassword());

    if (!correctPassword) {
      throw new UnauthorizedException(
          "Sai mật khẩu.");
    }

    return user;
  }

  /**
   * Chặn đăng ký tài khoản ADMIN từ client.
   *
   * @param message thông điệp đăng ký
   * @throws UnauthorizedException nếu role đăng ký là ADMIN
   */
  private void validateNotAdminRegister(
      ClientMessage message) {
    if (message.getRole() == UserRole.ADMIN) {
      throw new UnauthorizedException(
          "Không thể đăng ký Admin. Hệ thống chỉ có một tài khoản Admin mặc định.");
    }
  }

  /**
   * Chuẩn hóa thông tin đăng nhập.
   *
   * <p>Nếu người dùng nhập {@code ADMIN}, hệ thống sẽ đổi sang email nội bộ
   * của admin mặc định để truy vấn database.
   *
   * @param loginIdentifier email hoặc username người dùng nhập
   * @return email dùng để truy vấn database
   */
  private String normalizeLoginIdentifier(
      String loginIdentifier) {
    if (DEFAULT_ADMIN_USERNAME.equals(loginIdentifier)) {
      return DEFAULT_ADMIN_EMAIL;
    }

    return loginIdentifier;
  }

  /**
   * Kiểm tra tính hợp lệ của dữ liệu đăng ký.
   *
   * <p>Dữ liệu đăng ký hợp lệ khi thông điệp không null, role không null,
   * tên không rỗng, email hợp lệ và mật khẩu hợp lệ.
   *
   * @param message thông điệp từ client cần kiểm tra
   * @throws AuctionException nếu thông điệp, role, tên, email hoặc mật khẩu không
   *                          hợp lệ
   */
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

  /**
   * Kiểm tra tính hợp lệ của dữ liệu đăng nhập.
   *
   * <p>Dữ liệu đăng nhập hợp lệ khi thông điệp không null, email/username không
   * rỗng và mật khẩu không rỗng. Riêng admin mặc định được phép đăng nhập bằng
   * username {@code ADMIN}.
   *
   * @param message thông điệp từ client cần kiểm tra
   * @throws AuctionException nếu thông điệp, tài khoản hoặc mật khẩu không hợp lệ
   */
  private void validateLoginMessage(
      ClientMessage message) {
    if (message == null) {
      throw new AuctionException(
          "ClientMessage không được null.");
    }

    if (message.getEmail() == null
        || message.getEmail().isBlank()) {
      throw new AuctionException(
          "Tài khoản không hợp lệ.");
    }

    if (!DEFAULT_ADMIN_USERNAME.equals(message.getEmail())
        && !message.getEmail().contains("@")) {
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