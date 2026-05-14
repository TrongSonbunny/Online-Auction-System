package com.auction.backend.auction;

import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton quản lý toàn bộ auction đang hoạt động trong RAM.
 *
 * <p>Dùng {@link java.util.concurrent.ConcurrentHashMap} để đảm bảo thread-safety
 * khi nhiều thread cùng đọc/ghi. Singleton được tạo lần đầu theo lazy initialization
 * với {@code synchronized} để tránh race condition.
 */
public class AuctionManager {

  private static AuctionManager instance;

  private final Map<String, Auction>
      auctionMap;

  /**
   * Private constructor singleton.
   */
  private AuctionManager() {
    this.auctionMap = new ConcurrentHashMap<>();
  }

  /**
   * Lấy instance singleton của AuctionManager.
   *
   * @return AuctionManager instance
   */
  public static synchronized AuctionManager getInstance() {

    if (instance == null) {
      instance = new AuctionManager();
    }

    return instance;
  }

  /**
   * Thêm auction vào RAM.
   *
   * @param auction auction cần thêm
   */
  public void addAuction(
      Auction auction) {

    if (auction == null) {
      throw new AuctionException(
          "Auction không được null.");
    }

    auctionMap.put(
        auction.getAuctionId(),
        auction);
  }

  /**
   * Xóa auction khỏi RAM theo id.
   *
   * @param auctionId mã auction
   */
  public void removeAuction(
      String auctionId) {

    auctionMap.remove(auctionId);
  }

  /**
   * Tìm auction theo id.
   *
   * @param auctionId mã auction
   * @return auction tìm được hoặc null nếu không tồn tại
   */
  public Auction findAuction(
      String auctionId) {

    return auctionMap.get(auctionId);
  }

  /**
   * Lấy toàn bộ auction trong RAM.
   *
   * @return danh sách auction immutable
   */
  public Collection<Auction> getAllAuctions() {

    return Collections.unmodifiableCollection(
        auctionMap.values());
  }
}