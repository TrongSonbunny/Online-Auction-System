package com.auction.network;

/**
 * Enum các action frontend có thể gửi lên.
 */
public enum ActionType {

  LOGIN,

  REGISTER,

  BID,

  CREATE_AUCTION,

  CANCEL_AUCTION,

  FINISH_AUCTION,

  GET_ALL_AUCTIONS,

  REGISTER_AUTO_BID,

  CANCEL_AUTO_BID
}