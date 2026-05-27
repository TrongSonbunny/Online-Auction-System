package com.auction.backend.database.dao;

import com.auction.backend.database.DatabaseConnection;
import com.auction.exceptions.BidException;
import com.auction.models.bid.BidTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * DAO thực hiện INSERT/DELETE bid transaction trong bảng {@code bid_transactions}.
 *
 * <p>Cùng cơ chế kết nối với {@link AuctionDao}: mỗi thao tác dùng
 * try-with-resources, lỗi SQL in ra stderr.
 */
public class BidDao {

  /**
   * Lưu bid transaction.
   *
   * @param transaction transaction cần lưu
   */
  public void saveBidTransaction(
      BidTransaction transaction) {

    validateTransaction(transaction);

    String sql =
        "INSERT INTO bid_transactions "
            + "(transaction_id,"
            + " bidder_id,"
            + " auction_id,"
            + " bid_amount,"
            + " created_at)"
            + " VALUES (?, ?, ?, ?, ?)";

    try (
        Connection connection =
            DatabaseConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          transaction.getTransactionId());

      statement.setString(
          2,
          transaction.getBidder()
              .getUserId());

      statement.setString(
          3,
          transaction.getAuctionId());

      statement.setDouble(
          4,
          transaction.getBidAmount());

      statement.setTimestamp(
          5,
          Timestamp.valueOf(
              transaction.getCreatedAt()));

      statement.executeUpdate();

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }

  /**
   * Xóa transaction.
   *
   * @param transactionId mã transaction
   */
  public void deleteTransaction(
      String transactionId) {

    String sql =
        "DELETE FROM bid_transactions "
            + "WHERE transaction_id = ?";

    try (
        Connection connection =
            DatabaseConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          transactionId);

      statement.executeUpdate();

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }

  /**
   * Validate transaction.
   *
   * @param transaction transaction
   */
  private void validateTransaction(
      BidTransaction transaction) {

    if (transaction == null) {

      throw new BidException(
          "Transaction không được null.");
    }
  }
}