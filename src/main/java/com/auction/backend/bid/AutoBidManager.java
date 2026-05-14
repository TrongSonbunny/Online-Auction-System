package com.auction.backend.bid;

import com.auction.models.bid.AutoBid;
import com.auction.models.user.Bidder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Quản lý danh sách auto-bid trong RAM, nhóm theo auctionId.
 *
 * <p>Dùng {@link java.util.concurrent.ConcurrentHashMap} ánh xạ
 * {@code auctionId → CopyOnWriteArrayList<AutoBid>} để đảm bảo thread-safety
 * khi nhiều thread đọc/ghi đồng thời. {@link #getAutoBidsForAuction} trả về
 * bản copy có thể sort mà không ảnh hưởng danh sách gốc.
 */
public class AutoBidManager {

  private final ConcurrentHashMap<String,
      CopyOnWriteArrayList<AutoBid>>
      autoBidsByAuction;

  /**
   * Constructor auto-bid manager.
   */
  public AutoBidManager() {

    this.autoBidsByAuction =
        new ConcurrentHashMap<>();
  }

  /**
   * Thêm auto-bid mới.
   *
   * @param autoBid auto-bid cần thêm
   */
  public void addAutoBid(AutoBid autoBid) {

    if (autoBid == null) {

      throw new IllegalArgumentException(
          "AutoBid không được null.");
    }

    autoBidsByAuction
        .computeIfAbsent(
            autoBid.getAuctionId(),
            k -> new CopyOnWriteArrayList<>())
        .add(autoBid);
  }

  /**
   * Lấy tất cả auto-bid của một auction.
   *
   * @param auctionId mã auction
   * @return danh sách auto-bid (copy, không immutable để sort được)
   */
  public List<AutoBid> getAutoBidsForAuction(
      String auctionId) {

    CopyOnWriteArrayList<AutoBid> list =
        autoBidsByAuction.get(auctionId);

    if (list == null) {
      return Collections.emptyList();
    }

    return new ArrayList<>(list);
  }

  /**
   * Xóa auto-bid theo ID.
   *
   * @param autoBidId mã auto-bid
   * @return true nếu xóa thành công
   */
  public boolean removeAutoBidById(
      String autoBidId) {

    for (CopyOnWriteArrayList<AutoBid> list
        : autoBidsByAuction.values()) {

      for (AutoBid ab : list) {

        if (ab.getAutoBidId()
            .equals(autoBidId)) {

          return list.remove(ab);
        }
      }
    }

    return false;
  }

  /**
   * Xóa toàn bộ auto-bid của một auction (khi auction kết thúc).
   *
   * @param auctionId mã auction
   */
  public void removeAutoBidsForAuction(
      String auctionId) {

    autoBidsByAuction.remove(auctionId);
  }

  /**
   * Kiểm tra bidder đã có auto-bid cho auction chưa.
   *
   * @param bidder bidder cần kiểm tra
   * @param auctionId mã auction
   * @return true nếu đã có auto-bid
   */
  public boolean hasAutoBidForBidder(
      Bidder bidder,
      String auctionId) {

    List<AutoBid> list =
        getAutoBidsForAuction(auctionId);

    return list.stream()
        .anyMatch(ab ->
            ab.getBidder() == bidder);
  }
}