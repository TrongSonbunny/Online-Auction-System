package com.auction.backend.bid;

import com.auction.backend.util.IdGenerator;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.BidException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.AutoBid;
import com.auction.models.bid.BidTransaction;
import com.auction.models.user.Bidder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý đăng ký và thực thi auto-bid.
 *
 * <p>Logic ưu tiên:
 * <ol>
 *   <li>Auto-bid chỉ kích hoạt khi chính bidder đó bị vượt giá (outbid).
 *   <li>Cascade tiếp tục cho đến khi không còn ai phản ứng được.
 * </ol>
 */
public class AutoBidService {

  private final AutoBidManager autoBidManager;

  private final BidHistoryManager bidHistoryManager;

  /**
   * Constructor auto-bid service.
   *
   * @param autoBidManager manager lưu auto-bid
   * @param bidHistoryManager manager lưu lịch sử bid
   */
  public AutoBidService(
      AutoBidManager autoBidManager,
      BidHistoryManager bidHistoryManager) {

    this.autoBidManager = autoBidManager;
    this.bidHistoryManager = bidHistoryManager;
  }

  /**
   * Đăng ký auto-bid cho auction.
   *
   * @param auction auction mục tiêu
   * @param bidder bidder đăng ký
   * @param maxBid giá tối đa bidder chấp nhận
   * @param increment bước giá mỗi lần tự động bid
   * @return AutoBid đã đăng ký
   */
  public AutoBid registerAutoBid(
      Auction auction,
      Bidder bidder,
      double maxBid,
      double increment) {

    if (auction == null) {
      throw new AuctionException(
          "Auction không được null.");
    }

    if (bidder == null) {
      throw new BidException(
          "Bidder không được null.");
    }

    if (!bidder.canPlaceBid()) {
      throw new BidException(
          "Bidder không có quyền đấu giá.");
    }

    if (increment <= 0) {
      throw new BidException(
          "Increment phải lớn hơn 0.");
    }

    AutoBid autoBid;

    auction.getLock().lock();
    try {

      if (!auction.isActive()) {
        throw new AuctionException(
            "Chỉ đăng ký auto-bid khi auction đang active.");
      }

      if (maxBid <= auction.getCurrentHighestBid()) {
        throw new BidException(
            "MaxBid phải lớn hơn giá hiện tại ("
                + auction.getCurrentHighestBid()
                + ").");
      }

      autoBid =
          new AutoBid(
              IdGenerator.generateAutoBidId(),
              bidder,
              auction.getAuctionId(),
              maxBid,
              increment);

      autoBidManager.addAutoBid(autoBid);

    } finally {
      auction.getLock().unlock();
    }

    return autoBid;
  }

  /**
   * Xử lý auto-bid cascade sau khi có bid mới.
   *
   * <p>Chỉ kích hoạt auto-bid cho {@code outbidBidder} — người vừa bị vượt giá.
   * Nếu auto-bid đó fires và vượt lại người bid vừa rồi, vòng lặp tiếp tục
   * tìm auto-bid của người bị vượt kế tiếp (cascade).
   *
   * @param auction auction cần xử lý auto-bid
   * @param outbidBidder bidder vừa bị vượt giá
   * @return danh sách transaction auto-bid đã tạo
   */
  public List<BidTransaction> processAutoBids(
      Auction auction,
      Bidder outbidBidder) {

    List<BidTransaction> createdTransactions =
        new ArrayList<>();

    Bidder currentOutbid = outbidBidder;

    while (true) {

      auction.getLock().lock();
      try {

        if (!auction.isActive()) {
          break;
        }

        double currentBid =
            auction.getCurrentHighestBid();

        Optional<AutoBid> bestOpt =
            findAutoForOutbidBidder(
                auction.getAuctionId(),
                currentOutbid,
                currentBid);

        if (bestOpt.isEmpty()) {
          break;
        }

        AutoBid best =
            bestOpt.get();

        double nextBid =
            currentBid + best.getIncrement();

        if (nextBid > best.getMaxBid()) {
          nextBid = best.getMaxBid();
        }

        if (nextBid <= currentBid) {
          break;
        }

        final Bidder currentBidder =
            auction.getCurrentHighestBidder();

        auction.updateHighestBid(
            best.getBidder(),
            nextBid);

        best.getBidder()
            .incrementTotalBidsPlaced();

        BidTransaction transaction =
            new BidTransaction(
                IdGenerator.generateTransactionId(),
                best.getBidder(),
                auction.getAuctionId(),
                nextBid);

        bidHistoryManager.addTransaction(
            transaction);

        createdTransactions.add(
            transaction);

        currentOutbid = currentBidder;

      } finally {
        auction.getLock().unlock();
      }
    }

    return createdTransactions;
  }

  /**
   * Hủy auto-bid theo ID.
   *
   * @param autoBidId mã auto-bid cần hủy
   * @return true nếu hủy thành công
   */
  public boolean cancelAutoBid(
      String autoBidId) {

    return autoBidManager
        .removeAutoBidById(autoBidId);
  }

  /**
   * Lấy auto-bid manager.
   *
   * @return AutoBidManager
   */
  public AutoBidManager getAutoBidManager() {
    return autoBidManager;
  }

  /**
   * Tìm auto-bid của {@code outbidBidder} còn đủ điều kiện phản ứng.
   *
   * <p>So sánh bằng userId (không phải tham chiếu object) vì mỗi request
   * load user mới từ DB, tạo ra các instance khác nhau cho cùng một người dùng.
   *
   * @param auctionId mã auction
   * @param outbidBidder bidder vừa bị vượt giá
   * @param currentBid giá hiện tại
   * @return optional auto-bid nếu còn đủ điều kiện
   */
  private Optional<AutoBid> findAutoForOutbidBidder(
      String auctionId,
      Bidder outbidBidder,
      double currentBid) {

    if (outbidBidder == null) {
      return Optional.empty();
    }

    List<AutoBid> autoBids =
        autoBidManager.getAutoBidsForAuction(
            auctionId);

    return autoBids.stream()
        .filter(ab ->
            ab.getBidder().getUserId()
                .equals(outbidBidder.getUserId()))
        .filter(ab ->
            ab.getMaxBid() > currentBid)
        .findFirst();
  }
}
