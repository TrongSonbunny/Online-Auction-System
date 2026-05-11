package com.auction.backend.observer.observers;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionObserver;

/**
 * Observer dành cho admin.
 */
public class AdminObserver
    implements AuctionObserver {

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

      throw new IllegalArgumentException(
          "Admin name không hợp lệ.");
    }

    this.adminName = adminName;
  }

  @Override
  public void update(
      AuctionEvent event) {

    System.out.println(
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