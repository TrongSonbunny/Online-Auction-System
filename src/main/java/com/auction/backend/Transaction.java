package com.auction.backend;

import java.time.LocalDateTime;

/**
 * Lớp trừu tượng đại diện cho một giao dịch chung trong hệ thống.
 * Mọi loại giao dịch (đặt giá, thanh toán...) đều kế thừa từ lớp này.
 *
 * <p>Cây kế thừa:
 * Transaction (abstract)
 *   └── BidTransaction (giao dịch đặt giá)
 */
public abstract class Transaction {

  private final String transactionId; // Mã giao dịch duy nhất
  private final LocalDateTime timestamp; // Thời điểm giao dịch diễn ra

  /** Constructor khởi tạo giao dịch với ID và thời điểm hiện tại. */
  public Transaction(String transactionId) {
    this.transactionId = transactionId;
    this.timestamp = LocalDateTime.now(); // Ghi lại thời điểm tạo giao dịch
  }

  /** Mô tả chi tiết giao dịch - mỗi lớp con tự định nghĩa. */
  public abstract String getTransactionDetails();

  // ==================== Getter ====================

  public String getTransactionId() {
    return transactionId;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "Transaction{"
        + "transactionId='" + transactionId + '\''
        + ", timestamp=" + timestamp
        + '}';
  }
}