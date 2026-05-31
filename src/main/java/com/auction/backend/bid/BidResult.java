package com.auction.backend.bid;

import com.auction.models.bid.BidTransaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Kết quả của một lần đặt bid.
 *
 * <p>Bao gồm transaction thủ công và các transaction auto-bid phát sinh sau đó.
 */
public class BidResult {

  private final BidTransaction manualTransaction;

  private final List<BidTransaction> autoBidTransactions;

  /**
   * Constructor bid result.
   *
   * @param manualTransaction transaction của bid thủ công
   * @param autoBidTransactions danh sách transaction auto-bid phát sinh
   */
  public BidResult(
      BidTransaction manualTransaction,
      List<BidTransaction> autoBidTransactions) {

    this.manualTransaction =
        Objects.requireNonNull(
            manualTransaction,
            "Manual transaction không được null.");

    if (autoBidTransactions == null) {
      this.autoBidTransactions =
          Collections.emptyList();
    } else {
      this.autoBidTransactions =
          Collections.unmodifiableList(
              new ArrayList<>(autoBidTransactions));
    }
  }

  /**
   * Lấy transaction thủ công.
   *
   * @return manual transaction
   */
  public BidTransaction getManualTransaction() {
    return manualTransaction;
  }

  /**
   * Lấy danh sách transaction auto-bid.
   *
   * @return danh sách immutable
   */
  public List<BidTransaction> getAutoBidTransactions() {
    return autoBidTransactions;
  }

  /**
   * Lấy toàn bộ transaction.
   *
   * @return manual transaction và auto-bid transactions
   */
  public List<BidTransaction> getAllTransactions() {

    List<BidTransaction> allTransactions =
        new ArrayList<>();

    allTransactions.add(manualTransaction);
    allTransactions.addAll(autoBidTransactions);

    return Collections.unmodifiableList(
        allTransactions);
  }
}