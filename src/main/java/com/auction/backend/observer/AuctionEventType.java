package com.auction.backend.observer;

/**
 * Các loại event trong hệ thống đấu giá.
 */
public enum AuctionEventType {

  AUCTION_CREATED,

  AUCTION_STARTED,

  AUCTION_FINISHED,

  AUCTION_CANCELLED,

  AUCTION_EXTENDED,

  NEW_BID,

  AUTO_BID_PLACED,

  AUTO_BID_REGISTERED,

  AUTO_BID_CANCELLED
}