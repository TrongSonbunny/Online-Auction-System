package com.auction.backend.observer;

import com.auction.exceptions.AuctionException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Publisher trong Observer pattern — quản lý danh sách observer và phân phối event.
 *
 * <p>Dùng {@link java.util.concurrent.CopyOnWriteArrayList} để đảm bảo thread-safety:
 * có thể publish event khi một thread khác đang thêm/xóa observer mà không cần lock.
 * {@link #publishEvent} duyệt toàn bộ observer và gọi {@code update()} lần lượt.
 */
public class AuctionEventPublisher {

  private static final Logger logger =
      Logger.getLogger(AuctionEventPublisher.class.getName());

  private final List<AuctionObserver>
      observers;

  /**
   * Constructor publisher.
   */
  public AuctionEventPublisher() {

    this.observers =
        new CopyOnWriteArrayList<>();
  }

  /**
   * Đăng ký observer.
   *
   * @param observer observer cần thêm
   */
  public void addObserver(
      AuctionObserver observer) {

    if (observer == null) {

      throw new AuctionException(
          "Observer không được null.");
    }

    observers.add(observer);
  }

  /**
   * Xóa observer.
   *
   * @param observer observer cần xóa
   */
  public void removeObserver(
      AuctionObserver observer) {

    observers.remove(observer);
  }

  /**
   * Publish event tới toàn bộ observer.
   *
   * @param event event cần publish
   */
  public void publishEvent(
      AuctionEvent event) {

    if (event == null) {

      throw new AuctionException(
          "Event không được null.");
    }

    for (AuctionObserver observer
        : observers) {

      // Cô lập lỗi từng observer: một client mất kết nối (socket lỗi) hoặc một
      // observer ném exception KHÔNG được phép chặn các observer còn lại
      // (đặc biệt là persistence và các client khác đang xem real-time).
      try {
        observer.update(event);
      } catch (Exception ex) {
        logger.log(
            Level.WARNING,
            "Observer "
                + observer
                + " xử lý event thất bại: "
                + ex.getMessage(),
            ex);
      }
    }
  }

  /**
   * Lấy số lượng observer.
   *
   * @return tổng observer
   */
  public int getObserverCount() {
    return observers.size();
  }
}