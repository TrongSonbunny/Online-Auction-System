package com.auction.models.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Lớp tiện ích cung cấp các phương thức xử lý và tính toán thời gian.
 * Giúp chuẩn hóa việc kiểm tra thời hạn và định dạng thời gian trong hệ thống đấu giá.
 */
public final class TimeUtils {

  /**
   * Constructor riêng tư để ngăn chặn việc khởi tạo lớp tiện ích.
   */
  private TimeUtils() {
    // Prevent instantiation
  }

  /**
   * Tính toán số giây chênh lệch giữa hai thời điểm.
   *
   * @param start thời điểm bắt đầu
   * @param end thời điểm kết thúc
   * @return số giây giữa hai thời điểm (có thể âm nếu start sau end)
   */
  public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
    return Duration.between(start, end).getSeconds();
  }

  /**
   * Kiểm tra xem thời điểm hiện tại đã vượt quá thời điểm kết thúc hay chưa.
   *
   * @param endTime thời điểm cần kiểm tra
   * @return true nếu đã quá hạn, false nếu vẫn còn trong thời gian
   */
  public static boolean isExpired(LocalDateTime endTime) {
    return LocalDateTime.now().isAfter(endTime);
  }

  /**
   * Chuyển đổi đối tượng LocalDateTime sang dạng chuỗi mặc định.
   *
   * @param time đối tượng thời gian cần định dạng
   * @return chuỗi đại diện cho thời gian
   */
  public static String format(LocalDateTime time) {
    return time.toString();
  }
}