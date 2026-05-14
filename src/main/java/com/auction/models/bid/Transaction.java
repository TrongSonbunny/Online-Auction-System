package com.auction.models.bid;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Lớp cơ sở bất biến cho mọi transaction trong hệ thống.
 *
 * <p>Đảm bảo mỗi transaction có ID duy nhất và timestamp tạo không thay đổi.
 * Subclass mở rộng với thông tin nghiệp vụ cụ thể (ví dụ: {@link BidTransaction}).
 */
public abstract class Transaction {

  private final String transactionId;

  private final LocalDateTime createdAt;

  /**
   * Constructor transaction.
   *
   * @param transactionId mã transaction
   */
  protected Transaction(String transactionId) {

    this.transactionId = Objects.requireNonNull(
        transactionId,
        "TransactionId không được null.");

    this.createdAt = LocalDateTime.now();
  }

  public String getTransactionId() {
    return transactionId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {

    return "Transaction{"
        + "transactionId='"
        + transactionId
        + '\''
        + ", createdAt="
        + createdAt
        + '}';
  }
}