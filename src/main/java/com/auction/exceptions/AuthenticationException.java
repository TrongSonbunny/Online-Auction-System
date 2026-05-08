package com.auction.exceptions;

/**
 * Exception khi xác thực thất bại.
 */
public class AuthenticationException extends AuctionException {

  public AuthenticationException(String message) {
    super(message);
  }
}