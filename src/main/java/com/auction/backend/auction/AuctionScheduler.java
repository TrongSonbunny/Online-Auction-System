package com.auction.backend.auction;

import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionEventPublisher;
import com.auction.backend.observer.AuctionEventType;
import com.auction.exceptions.AuctionException;
import com.auction.models.auction.Auction;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Scheduler tự động kết thúc auction, hỗ trợ reschedule khi anti-snipe kích hoạt.
 *
 * <p>Dùng một platform-thread scheduler nhỏ chỉ để tính giờ; công việc thực sự
 * ({@code finishSafely}) chạy trên virtual thread qua {@code virtualExecutor}.
 */
public class AuctionScheduler {

  private final ScheduledExecutorService scheduler;

  private final ExecutorService virtualExecutor;

  private final ConcurrentHashMap<String, ScheduledFuture<?>>
      scheduledTasks;

  private final AuctionEventPublisher eventPublisher;

  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Constructor scheduler.
   */
  public AuctionScheduler() {

    this(new AuctionEventPublisher());
  }

  /**
   * Constructor scheduler dùng chung event publisher.
   *
   * @param eventPublisher publisher phát event
   */
  public AuctionScheduler(
      AuctionEventPublisher eventPublisher) {

    this.scheduler =
        Executors.newSingleThreadScheduledExecutor();

    this.virtualExecutor =
        Executors.newVirtualThreadPerTaskExecutor();

    this.scheduledTasks =
        new ConcurrentHashMap<>();

    this.eventPublisher =
        eventPublisher == null
            ? new AuctionEventPublisher()
            : eventPublisher;
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
            () -> virtualExecutor.submit(
                () -> finishSafely(auction)),
            durationSeconds,
            TimeUnit.SECONDS);

    scheduledTasks.put(
        auction.getAuctionId(),
        future);
  }

  /**
   * Reschedule auction finish khi anti-snipe kích hoạt.
   *
   * @param auction auction cần gia hạn
   * @param extensionSeconds số giây gia hạn thêm
   */
  public void rescheduleAuctionFinish(
      Auction auction,
      long extensionSeconds) {

    lock.lock();
    try {

      if (auction == null || !auction.isActive()) {
        return;
      }

      final String auctionId =
          auction.getAuctionId();

      ScheduledFuture<?> existing =
          scheduledTasks.get(auctionId);

      if (existing != null && !existing.isDone()) {
        existing.cancel(false);
      }

      LocalDateTime scheduledEnd =
          auction.getScheduledEndTime();

      long remaining =
          scheduledEnd == null
              ? 0
              : Duration
                  .between(
                      LocalDateTime.now(),
                      scheduledEnd)
                  .getSeconds();

      if (remaining < 0) {
        remaining = 0;
      }

      final long newDuration =
          remaining + extensionSeconds;

      auction.extendScheduledEndTime(
          extensionSeconds);

      publishAuctionEvent(
          AuctionEventType.AUCTION_EXTENDED,
          auction,
          "Auction được gia hạn.");

      ScheduledFuture<?> newFuture =
          scheduler.schedule(
              () -> virtualExecutor.submit(
                  () -> finishSafely(auction)),
              newDuration,
              TimeUnit.SECONDS);

      scheduledTasks.put(
          auctionId,
          newFuture);

    } finally {
      lock.unlock();
    }
  }

  /**
   * Kết thúc auction an toàn, bỏ qua nếu đã kết thúc hoặc bị hủy.
   *
   * @param auction auction cần kết thúc
   */
  private void finishSafely(
      Auction auction) {

    try {
      auction.finish();

      publishAuctionEvent(
          AuctionEventType.AUCTION_FINISHED,
          auction,
          "Auction đã tự động kết thúc.");
    } catch (IllegalStateException ignored) {
      // Auction đã kết thúc hoặc bị hủy trước đó.
    }
  }

  /**
   * Publish auction event.
   *
   * @param eventType loại event
   * @param auction auction liên quan
   * @param message nội dung event
   */
  private void publishAuctionEvent(
      AuctionEventType eventType,
      Auction auction,
      String message) {

    eventPublisher.publishEvent(
        new AuctionEvent(
            eventType,
            auction.getAuctionId(),
            message,
            auction));
  }

  /**
   * Shutdown scheduler và virtual executor.
   */
  public void shutdown() {
    scheduler.shutdown();
    virtualExecutor.shutdown();
  }
}
