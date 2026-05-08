package com.auction.models.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility xử lý thời gian.
 */
public final class TimeUtils {

    private TimeUtils() {
        // Prevent instantiation
    }

    /**
     * Tính số giây giữa 2 thời điểm.
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).getSeconds();
    }

    /**
     * Kiểm tra đã hết hạn chưa.
     */
    public static boolean isExpired(LocalDateTime endTime) {
        return LocalDateTime.now().isAfter(endTime);
    }

    /**
     * Format thời gian (simple).
     */
    public static String format(LocalDateTime time) {
        return time.toString();
    }
}