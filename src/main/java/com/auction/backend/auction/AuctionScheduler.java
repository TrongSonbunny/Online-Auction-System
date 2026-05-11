package com.auction.backend.auction;

import com.auction.models.auction.Auction;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler tự động kết thúc auction.
 */
public class AuctionScheduler {

  private final ScheduledExecutorService
      scheduler;

  /**
   * Constructor scheduler.
   */
  public AuctionScheduler() {

    this.scheduler =
        Executors.newScheduledThreadPool(2);
  }

  /**
   * Schedule auction finish.
   *
   * @param auction auction cần schedule
   * @param durationSeconds thời gian kết thúc
   */
  public void scheduleAuctionFinish(
      Auction auction,
      long durationSeconds) {

    if (auction == null) {

      throw new IllegalArgumentException(
          "Auction không được null.");
    }

    if (durationSeconds <= 0) {

      throw new IllegalArgumentException(
          "Duration phải lớn hơn 0.");
    }

    scheduler.schedule(
        auction::finish,
        durationSeconds,
        TimeUnit.SECONDS);
  }

  /**
   * Shutdown scheduler.
   */
  public void shutdown() {
    scheduler.shutdown();
  }
}