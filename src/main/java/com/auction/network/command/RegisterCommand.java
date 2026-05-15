package com.auction.network.command;

import com.auction.network.ClientMessage;

/**
 * Command xử lý đăng ký.
 */
public class RegisterCommand extends BaseClientCommand {

  /**
   * Constructor register command.
   *
   * @param context command context
   */
  public RegisterCommand(
      CommandContext context) {

    super(context);
  }

  /**
   * Đăng ký tài khoản mới và trả về user vừa tạo.
   *
   * <p>Ủy quyền toàn bộ logic cho {@link com.auction.backend.auth.AuthService#register}.
   * Ném {@link com.auction.exceptions.AuctionException} nếu email đã tồn tại hoặc
   * dữ liệu đầu vào không hợp lệ.
   *
   * @param message dữ liệu client chứa {@code role}, {@code name}, {@code email}, {@code password}
   * @return user vừa được tạo
   */
  @Override
  public Object execute(
      ClientMessage message) {

    return authService.register(message);
  }
}