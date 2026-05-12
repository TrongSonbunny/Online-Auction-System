package com.auction.backend.bid;

import com.auction.exceptions.BidException;
import com.auction.models.bid.BidTransaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quản lý lịch sử bid trong RAM.
 */
public class BidHistoryManager {

  private final List<BidTransaction> bidHistory;

  /**
   * Constructor bid history manager.
   */
  public BidHistoryManager() {
    this.bidHistory = new ArrayList<>();
  }

  /**
   * Thêm transaction vào lịch sử.
   *
   * @param transaction bid transaction
   */
  public synchronized void addTransaction(
      BidTransaction transaction) {

    if (transaction == null) {
      throw new BidException(
          "Transaction không được null.");
    }

    bidHistory.add(transaction);
  }

  /**
   * Lấy toàn bộ lịch sử bid.
   *
   * @return danh sách immutable
   */
  public synchronized List<BidTransaction>
      getBidHistory() {

    return Collections.unmodifiableList(
        bidHistory);
  }

  /**
   * Lấy số lượng bid.
   *
   * @return tổng số bid
   */
  public synchronized int getTotalBids() {
    return bidHistory.size();
  }

  /**
   * Xóa toàn bộ lịch sử bid.
   */
  public synchronized void clearHistory() {
    bidHistory.clear();
  }
}