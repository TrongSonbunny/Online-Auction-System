package com.auction.models.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.auction.models.core.Auction;

/**
 * Data Access Object (DAO) cho Auction.
 * Quản lý các thao tác CRUD (Create, Read, Update, Delete) vào cơ sở dữ liệu.
 */
public class AuctionDao {

  /**
   * Lưu một phiên đấu giá mới vào cơ sở dữ liệu.
   * Sử dụng Transaction để đảm bảo tính nguyên tử.
   *
   * @param auction đối tượng phiên đấu giá cần lưu
   * @throws SQLException nếu có lỗi xảy ra trong quá trình truy vấn hoặc rollback
   */
  public void save(Auction auction) throws SQLException {
    if (auction == null) {
      throw new IllegalArgumentException("Auction không được null");
    }

    if (auction.getItem() == null) {
      throw new SQLException("Auction item không được null");
    }

    String sql = """
        INSERT INTO auctions (
            auction_id, item_id, seller_id,
            start_price, current_price,
            status, start_time, end_time
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    Connection conn = DatabaseManager.getInstance().getConnection();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      conn.setAutoCommit(false);

      stmt.setString(1, auction.getAuctionId());
      stmt.setString(2, auction.getItem().getId());
      stmt.setString(3, auction.getSellerId());
      stmt.setDouble(4, auction.getStartingPrice());
      stmt.setDouble(5, auction.getCurrentHighestBid());
      stmt.setString(6, auction.getStatus().name());
      stmt.setTimestamp(7, Timestamp.valueOf(auction.getStartTime()));
      stmt.setTimestamp(8, Timestamp.valueOf(auction.getEndTime()));

      stmt.executeUpdate();
      conn.commit();

    } catch (Exception e) {
      conn.rollback();
      throw new SQLException("Lỗi khi lưu Auction: " + auction.getAuctionId(), e);
    } finally {
      conn.setAutoCommit(true);
      conn.close();
    }
  }

  /**
   * Cập nhật thông tin phiên đấu giá (giá hiện tại, trạng thái, thời gian kết thúc).
   *
   * @param auction đối tượng phiên đấu giá chứa thông tin mới
   * @throws SQLException nếu không tìm thấy ID hoặc có lỗi database
   */
  public void update(Auction auction) throws SQLException {
    if (auction == null) {
      throw new IllegalArgumentException("Auction không được null");
    }

    String sql = """
        UPDATE auctions
        SET current_price = ?, status = ?, end_time = ?
        WHERE auction_id = ?
        """;

    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setDouble(1, auction.getCurrentHighestBid());
      stmt.setString(2, auction.getStatus().name());
      stmt.setTimestamp(3, Timestamp.valueOf(auction.getEndTime()));
      stmt.setString(4, auction.getAuctionId());

      int rows = stmt.executeUpdate();
      if (rows == 0) {
        throw new SQLException(
            "Không tìm thấy auction để update: " + auction.getAuctionId());
      }
    }
  }

  /**
   * Lấy danh sách ID của tất cả các phiên đấu giá hiện có.
   *
   * @return danh sách các chuỗi ID
   * @throws SQLException nếu truy vấn thất bại
   */
  public List<String> findAllIds() throws SQLException {
    List<String> ids = new ArrayList<>();
    String sql = "SELECT auction_id FROM auctions";

    try (Connection conn = DatabaseManager.getInstance().getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        ids.add(rs.getString("auction_id"));
      }
    }
    return ids;
  }

  /**
   * Tìm kiếm các phiên đấu giá dựa trên ID người bán.
   *
   * @param sellerId ID của người bán
   * @return danh sách ID các phiên đấu giá thuộc về người bán đó
   * @throws SQLException nếu truy vấn thất bại
   */
  public List<String> findBySeller(String sellerId) throws SQLException {
    if (sellerId == null || sellerId.isBlank()) {
      throw new IllegalArgumentException("sellerId không hợp lệ");
    }

    List<String> ids = new ArrayList<>();
    String sql = "SELECT auction_id FROM auctions WHERE seller_id = ?";

    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, sellerId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        ids.add(rs.getString("auction_id"));
      }
    }
    return ids;
  }

  /**
   * Xóa một phiên đấu giá khỏi cơ sở dữ liệu theo ID.
   *
   * @param auctionId ID của phiên cần xóa
   * @throws SQLException nếu không tìm thấy hoặc có lỗi xảy ra
   */
  public void delete(String auctionId) throws SQLException {
    if (auctionId == null || auctionId.isBlank()) {
      throw new IllegalArgumentException("auctionId không hợp lệ");
    }

    String sql = "DELETE FROM auctions WHERE auction_id = ?";

    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, auctionId);
      int rows = stmt.executeUpdate();

      if (rows == 0) {
        throw new SQLException(
            "Không tìm thấy auction để xóa: " + auctionId);
      }
    }
  }
}