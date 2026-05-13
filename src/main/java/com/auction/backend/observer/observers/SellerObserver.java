package com.auction.backend.observer.observers;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionObserver;
import com.auction.exceptions.AuctionException;

/**
 * Observer dành cho seller.
 */
public class SellerObserver
    implements AuctionObserver {

  private final String sellerName;

  /**
   * Constructor seller observer.
   *
   * @param sellerName tên seller
   */
  public SellerObserver(
      String sellerName) {

    if (sellerName == null
        || sellerName.isBlank()) {

      throw new AuctionException(
          "Seller name không hợp lệ.");
    }

    this.sellerName = sellerName;
  }

  /**
   * Nhận event và hiển thị thông báo tóm tắt cho seller.
   *
   * @param event event được publish
   */
  @Override
  public void update(
      AuctionEvent event) {

    System.out.println(
        "[SELLER NOTIFICATION] "
            + sellerName
            + " nhận event: "
            + event.getMessage());
  }

  @Override
  public String toString() {

    return "SellerObserver{"
        + "sellerName='"
        + sellerName
        + '\''
        + '}';
  }
}