package com.auction.backend.observer;

/**
 * Enum loại event trong hệ thống auction.
 */
public enum AuctionEventType {

  AUCTION_CREATED,

  AUCTION_STARTED,

  NEW_BID,

  AUCTION_FINISHED,

  AUCTION_CANCELLED
}