package com.auction.backend.database.dao;

import com.auction.backend.database.MySqlConnection;
import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * DAO xử lý auction database.
 */
public class AuctionDao {

  /**
   * Lưu auction vào database.
   *
   * @param auction auction cần lưu
   */
  public void saveAuction(Auction auction) {

    validateAuction(auction);

    String sql =
        "INSERT INTO auctions "
            + "(auction_id, seller_id,"
            + " item_id, starting_price,"
            + " current_highest_bid,"
            + " current_highest_bidder_id,"
            + " status, created_at,"
            + " start_time, end_time)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (
        Connection connection =
            MySqlConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          auction.getAuctionId());

      statement.setString(
          2,
          auction.getSeller().getUserId());

      statement.setString(
          3,
          auction.getItem().getItemId());

      statement.setDouble(
          4,
          auction.getStartingPrice());

      statement.setDouble(
          5,
          auction.getCurrentHighestBid());

      if (auction.getCurrentHighestBidder()
          != null) {

        statement.setString(
            6,
            auction
                .getCurrentHighestBidder()
                .getUserId());

      } else {

        statement.setNull(
            6,
            java.sql.Types.VARCHAR);
      }

      statement.setString(
          7,
          auction.getStatus().name());

      statement.setTimestamp(
          8,
          Timestamp.valueOf(
              auction.getCreatedAt()));

      if (auction.getStartTime() != null) {

        statement.setTimestamp(
            9,
            Timestamp.valueOf(
                auction.getStartTime()));

      } else {

        statement.setNull(
            9,
            java.sql.Types.TIMESTAMP);
      }

      if (auction.getEndTime() != null) {

        statement.setTimestamp(
            10,
            Timestamp.valueOf(
                auction.getEndTime()));

      } else {

        statement.setNull(
            10,
            java.sql.Types.TIMESTAMP);
      }

      statement.executeUpdate();

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }

  /**
   * Xóa auction.
   *
   * @param auctionId mã auction
   */
  public void deleteAuction(
      String auctionId) {

    String sql =
        "DELETE FROM auctions "
            + "WHERE auction_id = ?";

    try (
        Connection connection =
            MySqlConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          auctionId);

      statement.executeUpdate();

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }

  /**
   * Validate auction.
   *
   * @param auction auction
   */
  private void validateAuction(
      Auction auction) {

    if (auction == null) {

      throw new AuctionException(
          "Auction không được null.");
    }
  }
}