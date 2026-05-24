package com.auction.backend.auth;

import com.auction.backend.database.dao.UserDao;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.user.User;
import com.auction.models.user.UserFactory;
import com.auction.network.ClientMessage;

/**
 * Service xử lý các nghiệp vụ xác thực người dùng trong hệ thống đấu giá.
 *
 * <p>Class này chịu trách nhiệm đăng ký tài khoản mới và đăng nhập người dùng.
 * Dữ liệu người dùng được truy xuất và lưu trữ thông qua {@link UserDao}.
 */
public class AuthService {

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
   * @param message thông điệp từ client chứa role, tên, email và mật khẩu
   * @return người dùng vừa được tạo
   * @throws AuctionException nếu dữ liệu đăng ký không hợp lệ hoặc email đã tồn
   *                          tại
   */
  public User register(
      ClientMessage message) {

    validateRegisterMessage(message);

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
   * Đăng nhập người dùng dựa trên email và mật khẩu nhận từ client.
   *
   * <p>Phương thức sẽ kiểm tra dữ liệu đăng nhập, tìm người dùng theo email,
   * sau đó kiểm tra mật khẩu. Nếu email không tồn tại hoặc mật khẩu không đúng,
   * phương thức sẽ ném ra ngoại lệ xác thực.
   *
   * @param message thông điệp từ client chứa email và mật khẩu
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
          "Email không tồn tại.");
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
   * <p>Dữ liệu đăng nhập hợp lệ khi thông điệp không null, email không rỗng,
   * email có chứa ký tự {@code @} và mật khẩu không rỗng.
   *
   * @param message thông điệp từ client cần kiểm tra
   * @throws AuctionException nếu thông điệp, email hoặc mật khẩu không hợp lệ
   */
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