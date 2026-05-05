package com.auction.manager;

import com.auction.models.Item;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp quản lý phiên đấu giá, áp dụng mẫu thiết kế Singleton.
 */
public class AuctionManager {

  private static AuctionManager instance;
  private List<Item> auctionItems;

  /**
   * Constructor private để đảm bảo chỉ có một thể hiện duy nhất của
   * AuctionManager.
   */
  private AuctionManager() {
    auctionItems = new ArrayList<>();
  }

  /**
   * Lấy thể hiện duy nhất của hệ thống quản lý đấu giá.
   *
   * @return Đối tượng AuctionManager duy nhất
   */
  public static synchronized AuctionManager getInstance() {
    if (instance == null) {
      instance = new AuctionManager();
    }
    return instance;
  }

  /**
   * Thêm một sản phẩm mới vào danh sách đấu giá.
   *
   * @param item Sản phẩm cần thêm vào hệ thống
   */
  public void addItem(Item item) {
    auctionItems.add(item);
    System.out.println("Da them san pham vao he thong: " + item.getName());
  }

  /**
   * Lấy danh sách tất cả các sản phẩm đang được đấu giá.
   *
   * @return Danh sách các sản phẩm (Item)
   */
  public List<Item> getAuctionItems() {
    return auctionItems;
  }
}