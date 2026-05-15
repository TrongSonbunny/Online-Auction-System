package com.auction.backend.database.dao;

import com.auction.backend.database.DatabaseConnection;
import com.auction.exceptions.BidException;
import com.auction.models.bid.BidTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO xử lý bid transaction database SQLite.
 */
public class BidDao {

	/**
	 * Lưu bid transaction.
	 *
	 * @param transaction transaction cần lưu
	 */
	public void saveBidTransaction(BidTransaction transaction) {

		validateTransaction(transaction);

		String sql = "INSERT INTO bid_transactions "
				+ "(transaction_id, bidder_id, auction_id, bid_amount, created_at) "
				+ "VALUES (?, ?, ?, ?, ?)";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, transaction.getTransactionId());
			statement.setString(2, transaction.getBidder().getUserId());
			statement.setString(3, transaction.getAuctionId());
			statement.setDouble(4, transaction.getBidAmount());
			statement.setString(5, transaction.getCreatedAt().toString());

			statement.executeUpdate();

		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new BidException("Không thể lưu thông tin đặt giá (bid transaction).");
		}
	}

	/**
	 * Xóa transaction.
	 *
	 * @param transactionId mã transaction
	 */
	public void deleteTransaction(String transactionId) {

		String sql = "DELETE FROM bid_transactions WHERE transaction_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, transactionId);
			statement.executeUpdate();

		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new BidException("Không thể xóa thông tin đặt giá.");
		}
	}

	/**
	 * Validate transaction.
	 *
	 * @param transaction transaction
	 */
	private void validateTransaction(BidTransaction transaction) {
		if (transaction == null) {
			throw new BidException("Transaction không được null.");
		}
	}
}