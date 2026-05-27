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
 * khi nhiều thread cùng đọc/ghi. Singleton khởi tạo theo initialization-on-demand
 * holder pattern — thread-safe mà không cần {@code synchronized}.
 */
public class AuctionManager {

  private final Map<String, Auction>
      auctionMap;

  /**
   * Private constructor singleton.
   */
  private AuctionManager() {

    this.auctionMap =
        new ConcurrentHashMap<>();
  }

  /**
   * Holder đảm bảo lazy initialization thread-safe nhờ class-loading semantics.
   */
  private static final class Holder {
    static final AuctionManager INSTANCE =
        new AuctionManager();
  }

  /**
   * Lấy instance singleton.
   *
   * @return AuctionManager
   */
  public static AuctionManager getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Thêm auction.
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
   * Xóa auction.
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
   * @return Auction tìm được
   */
  public Auction findAuction(
      String auctionId) {

    return auctionMap.get(auctionId);
  }

  /**
   * Lấy toàn bộ auction.
   *
   * @return collection immutable
   */
  public Collection<Auction>
      getAllAuctions() {

    return Collections.unmodifiableCollection(
        auctionMap.values());
  }
}