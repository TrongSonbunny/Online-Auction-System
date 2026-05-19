package com.auction.backend.bid;

import com.auction.exceptions.BidException;
import com.auction.models.bid.BidTransaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Quản lý lịch sử bid transaction trong RAM.
 *
 * <p>Dùng {@link ReentrantLock} thay vì {@code synchronized} để đảm bảo thread-safety
 * với virtual thread mà không pin carrier thread.
 * {@link #getBidHistory()} trả về unmodifiable list để tránh sửa ngoài.
 */
public class BidHistoryManager {

  private final List<BidTransaction> bidHistory;

  private final ReentrantLock lock = new ReentrantLock();

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
  public void addTransaction(
      BidTransaction transaction) {

    if (transaction == null) {
      throw new BidException(
          "Transaction không được null.");
    }

    lock.lock();
    try {
      bidHistory.add(transaction);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Lấy toàn bộ lịch sử bid.
   *
   * @return danh sách immutable
   */
  public List<BidTransaction> getBidHistory() {

    lock.lock();
    try {
      return Collections.unmodifiableList(
          bidHistory);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Lấy số lượng bid.
   *
   * @return tổng số bid
   */
  public int getTotalBids() {

    lock.lock();
    try {
      return bidHistory.size();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Xóa toàn bộ lịch sử bid.
   */
  public void clearHistory() {

    lock.lock();
    try {
      bidHistory.clear();
    } finally {
      lock.unlock();
    }
  }
}
