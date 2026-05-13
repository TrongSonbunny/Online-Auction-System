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

  @Override
  public Object execute(
      ClientMessage message) {

    return authService.login(message);
  }
}