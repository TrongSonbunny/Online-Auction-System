package com.auction.network.command;

import com.auction.network.ClientMessage;

/**
 * Command xử lý đăng nhập.
 */
public class LoginCommand extends BaseClientCommand {

  /**
   * Constructor login command.
   *
   * @param context command context
   */
  public LoginCommand(
      CommandContext context) {

    super(context);
  }

  /**
   * Xác thực email và password, trả về thông tin user nếu hợp lệ.
   *
   * <p>Ủy quyền toàn bộ logic cho {@link com.auction.backend.auth.AuthService#login}.
   * Ném {@link com.auction.exceptions.UnauthorizedException} nếu sai thông tin.
   *
   * @param message dữ liệu client chứa {@code email} và {@code password}
   * @return user đã xác thực
   */
  @Override
  public Object execute(
      ClientMessage message) {

    return authService.login(message);
  }
}