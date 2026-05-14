package com.auction.backend.database.dao;

import com.auction.backend.database.MySqlConnection;
import com.auction.exceptions.AuctionException;
import com.auction.models.item.AuctionItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO thực hiện INSERT/DELETE item trong bảng {@code items}.
 *
 * <p>Cùng cơ chế kết nối với {@link AuctionDao}: mỗi thao tác dùng
 * try-with-resources, lỗi SQL in ra stderr.
 */
public class ItemDao {

  /**
   * Lưu item vào database.
   *
   * @param item item cần lưu
   */
  public void saveItem(AuctionItem item) {

    validateItem(item);

    String sql =
        "INSERT INTO items "
            + "(item_id, name, description,"
            + " category, item_condition,"
            + " estimated_price)"
            + " VALUES (?, ?, ?, ?, ?, ?)";

    try (
        Connection connection =
            MySqlConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          item.getItemId());

      statement.setString(
          2,
          item.getName());

      statement.setString(
          3,
          item.getDescription());

      statement.setString(
          4,
          item.getCategory().name());

      statement.setString(
          5,
          item.getItemCondition());

      statement.setDouble(
          6,
          item.getEstimatedPrice());

      statement.executeUpdate();

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }

  /**
   * Xóa item.
   *
   * @param itemId mã item
   */
  public void deleteItem(String itemId) {

    String sql =
        "DELETE FROM items WHERE item_id = ?";

    try (
        Connection connection =
            MySqlConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(1, itemId);

      statement.executeUpdate();

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }

  /**
   * Validate item.
   *
   * @param item item
   */
  private void validateItem(
      AuctionItem item) {

    if (item == null) {

      throw new AuctionException(
          "Item không được null.");
    }
  }
}