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

  @Override
  public Object execute(
      ClientMessage message) {

    return authService.register(message);
  }
}