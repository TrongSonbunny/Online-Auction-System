package com.auction.backend.observer.observers;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionObserver;
import com.auction.exceptions.AuctionException;
import java.util.logging.Logger;

/**
 * Observer dành cho admin.
 */
public class AdminObserver
    implements AuctionObserver {

  private static final Logger logger =
      Logger.getLogger(
          AdminObserver.class.getName());

  private final String adminName;

  /**
   * Constructor admin observer.
   *
   * @param adminName tên admin
   */
  public AdminObserver(
      String adminName) {

    if (adminName == null
        || adminName.isBlank()) {

      throw new AuctionException(
          "Admin name không hợp lệ.");
    }

    this.adminName = adminName;
  }

  /**
   * Nhận event và ghi log toàn bộ thông tin event ra hệ thống logging.
   *
   * @param event event được publish
   */
  @Override
  public void update(
      AuctionEvent event) {

    logger.info(
        "[ADMIN LOG] "
            + adminName
            + " ghi nhận event: "
            + event);
  }

  @Override
  public String toString() {

    return "AdminObserver{"
        + "adminName='"
        + adminName
        + '\''
        + '}';
  }
}