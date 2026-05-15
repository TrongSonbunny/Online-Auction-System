package com.auction.backend.bid;

import com.auction.backend.util.IdGenerator;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.BidException;
import com.auction.models.auction.Auction;
import com.auction.models.bid.AutoBid;
import com.auction.models.bid.BidTransaction;
import com.auction.models.user.Bidder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý đăng ký và thực thi auto-bid.
 *
 * <p>Logic ưu tiên:
 * <ol>
 *   <li>Bidder có maxBid cao hơn thắng.
 *   <li>Nếu maxBid bằng nhau, bidder đăng ký trước thắng.
 *   <li>Auto-bid cascade tiếp tục cho đến khi không còn ai có thể outbid.
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

    synchronized (auction) {

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
    }

    return autoBid;
  }

  /**
   * Xử lý toàn bộ auto-bid cascade sau khi có bid mới.
   *
   * @param auction auction cần xử lý auto-bid
   * @return danh sách transaction auto-bid đã tạo
   */
  public List<BidTransaction> processAutoBids(
      Auction auction) {

    List<BidTransaction> createdTransactions =
        new ArrayList<>();

    while (true) {

      synchronized (auction) {

        if (!auction.isActive()) {
          break;
        }

        double currentBid =
            auction.getCurrentHighestBid();

        Bidder currentBidder =
            auction.getCurrentHighestBidder();

        Optional<AutoBid> bestOpt =
            findBestEligible(
                auction.getAuctionId(),
                currentBidder,
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
   * Tìm auto-bid tốt nhất đủ điều kiện outbid.
   *
   * @param auctionId mã auction
   * @param currentBidder bidder đang dẫn đầu
   * @param currentBid giá hiện tại
   * @return optional auto-bid tốt nhất
   */
  private Optional<AutoBid> findBestEligible(
      String auctionId,
      Bidder currentBidder,
      double currentBid) {

    List<AutoBid> autoBids =
        autoBidManager.getAutoBidsForAuction(
            auctionId);

    return autoBids.stream()
        .filter(ab ->
            ab.getBidder() != currentBidder)
        .filter(ab ->
            ab.getMaxBid() > currentBid)
        .sorted(
            Comparator
                .comparingDouble(AutoBid::getMaxBid)
                .reversed()
                .thenComparing(
                    AutoBid::getRegisteredAt))
        .findFirst();
  }
}