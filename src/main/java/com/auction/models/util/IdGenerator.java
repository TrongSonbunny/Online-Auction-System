package com.auction.models.util;

import java.util.UUID;

/**
 * Utility tạo ID duy nhất.
 */
public final class IdGenerator {

    private IdGenerator() {
        // Prevent instantiation
    }

    /**
     * Tạo UUID dạng string.
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Tạo ID ngắn (8 ký tự).
     */
    public static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}