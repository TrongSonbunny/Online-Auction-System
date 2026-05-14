package com.auction.backend.auction;

import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler tự động kết thúc auction, hỗ trợ reschedule khi anti-snipe kích hoạt.
 */
public class AuctionScheduler {

  private final ScheduledExecutorService
      scheduler;

  private final ConcurrentHashMap<String, ScheduledFuture<?>>
      scheduledTasks;

  /**
   * Constructor scheduler.
   */
  public AuctionScheduler() {

    this.scheduler =
        Executors.newScheduledThreadPool(2);

    this.scheduledTasks =
        new ConcurrentHashMap<>();
  }

  /**
   * Schedule auction finish và lưu future.
   *
   * @param auction auction cần schedule
   * @param durationSeconds thời gian kết thúc
   */
  public void scheduleAuctionFinish(
      Auction auction,
      long durationSeconds) {

    if (auction == null) {

      throw new AuctionException(
          "Auction không được null.");
    }

    if (durationSeconds <= 0) {

      throw new AuctionException(
          "Duration phải lớn hơn 0.");
    }

    auction.setScheduledEndTime(
        LocalDateTime.now()
            .plusSeconds(durationSeconds));

    ScheduledFuture<?> future =
        scheduler.schedule(
            () -> finishSafely(auction),
            durationSeconds,
            TimeUnit.SECONDS);

    scheduledTasks.put(
        auction.getAuctionId(), future);
  }

  /**
   * Reschedule auction finish khi anti-snipe kích hoạt.
   * Hủy task cũ, tính thời gian còn lại rồi gia hạn thêm.
   *
   * @param auction auction cần gia hạn
   * @param extensionSeconds số giây gia hạn thêm
   */
  public synchronized void rescheduleAuctionFinish(
      Auction auction,
      long extensionSeconds) {

    if (auction == null || !auction.isActive()) {
      return;
    }

    String auctionId = auction.getAuctionId();

    ScheduledFuture<?> existing =
        scheduledTasks.get(auctionId);

    if (existing != null && !existing.isDone()) {
      existing.cancel(false);
    }

    LocalDateTime scheduledEnd =
        auction.getScheduledEndTime();

    long remaining = scheduledEnd == null
        ? 0
        : Duration
            .between(LocalDateTime.now(), scheduledEnd)
            .getSeconds();

    if (remaining < 0) {
      remaining = 0;
    }

    long newDuration = remaining + extensionSeconds;

    auction.extendScheduledEndTime(extensionSeconds);

    ScheduledFuture<?> newFuture =
        scheduler.schedule(
            () -> finishSafely(auction),
            newDuration,
            TimeUnit.SECONDS);

    scheduledTasks.put(auctionId, newFuture);
  }

  /**
   * Kết thúc auction an toàn, bỏ qua nếu đã kết thúc hoặc bị hủy.
   *
   * @param auction auction cần kết thúc
   */
  private void finishSafely(Auction auction) {

    try {
      auction.finish();
    } catch (IllegalStateException ignored) {
      // Auction đã kết thúc hoặc bị hủy trước đó
    }
  }

  /**
   * Shutdown scheduler.
   */
  public void shutdown() {
    scheduler.shutdown();
  }
}