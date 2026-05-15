package com.auction.backend.database.dao;

import com.auction.backend.database.DatabaseConnection;
import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO xử lý auction database SQLite.
 */
public class AuctionDao {

    /**
     * Lưu auction vào database.
     *
     * @param auction auction cần lưu
     */
	public void saveAuction(Auction auction) {

        validateAuction(auction);

        String sql = "INSERT INTO auctions "
                + "(auction_id, seller_id,"
                + " item_id, starting_price,"
                + " current_highest_bid,"
                + " current_highest_bidder_id,"
                + " status, created_at,"
                + " start_time, end_time)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            fillInsertStatement(
                    statement,
                    auction);

            statement.executeUpdate();

        } catch (SQLException exception) {

            // FIX LỖI: Chỉ in log server, không ném exception của SQLite về Client
            exception.printStackTrace();
            throw new AuctionException(
                    "Không thể lưu auction vào cơ sở dữ liệu.");
        }
    }

    /**
     * Cập nhật auction sau khi bid/cancel/finish.
     *
     * @param auction auction cần cập nhật
     */
    public void updateAuction(
            Auction auction) {

        validateAuction(auction);

        String sql = "UPDATE auctions SET "
                + "current_highest_bid = ?, "
                + "current_highest_bidder_id = ?, "
                + "status = ?, "
                + "start_time = ?, "
                + "end_time = ? "
                + "WHERE auction_id = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(
                    1,
                    auction.getCurrentHighestBid());

            if (auction.getCurrentHighestBidder() != null) {
                statement.setString(
                        2,
                        auction.getCurrentHighestBidder()
                                .getUserId());
            } else {
                statement.setString(
                        2,
                        null);
            }

            statement.setString(
                    3,
                    auction.getStatus().name());

            statement.setString(
                    4,
                    auction.getStartTime() == null
                            ? null
                            : auction.getStartTime().toString());

            statement.setString(
                    5,
                    auction.getEndTime() == null
                            ? null
                            : auction.getEndTime().toString());

            statement.setString(
                    6,
                    auction.getAuctionId());

            statement.executeUpdate();

        } catch (SQLException exception) {

            exception.printStackTrace();
            throw new AuctionException(
                    "Không thể cập nhật thông tin auction.");
        }
    }

    /**
     * Xóa auction.
     *
     * @param auctionId mã auction
     */
    public void deleteAuction(
            String auctionId) {

        String sql = "DELETE FROM auctions "
                + "WHERE auction_id = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    auctionId);

            statement.executeUpdate();

        } catch (SQLException exception) {

            exception.printStackTrace();
            throw new AuctionException(
                    "Không thể xóa auction khỏi cơ sở dữ liệu.");
        }
    }

    /**
     * Fill statement khi insert auction.
     *
     * @param statement prepared statement
     * @param auction   auction
     * @throws SQLException nếu set dữ liệu lỗi
     */
    private void fillInsertStatement(
            PreparedStatement statement,
            Auction auction)
            throws SQLException {

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

        if (auction.getCurrentHighestBidder() != null) {
            statement.setString(
                    6,
                    auction.getCurrentHighestBidder()
                            .getUserId());
        } else {
            statement.setString(
                    6,
                    null);
        }

        statement.setString(
                7,
                auction.getStatus().name());

        statement.setString(
                8,
                auction.getCreatedAt().toString());

        statement.setString(
                9,
                auction.getStartTime() == null
                        ? null
                        : auction.getStartTime().toString());

        statement.setString(
                10,
                auction.getEndTime() == null
                        ? null
                        : auction.getEndTime().toString());
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