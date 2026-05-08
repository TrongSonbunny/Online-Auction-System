package com.auction.models.util;

import java.util.UUID;

/**
 * Lớp tiện ích cung cấp các phương thức tạo mã định danh duy nhất (Unique ID).
 * Đảm bảo các đối tượng trong hệ thống như User, Auction, Item luôn có ID không trùng lặp.
 */
public final class IdGenerator {

  /**
   * Constructor riêng tư để ngăn chặn việc khởi tạo lớp tiện ích này.
   */
  private IdGenerator() {
    // Prevent instantiation
  }

  /**
   * Tạo một mã định danh ngẫu nhiên chuẩn UUID (36 ký tự).
   * Thường dùng cho các khóa chính cần độ an toàn tuyệt đối.
   *
   * @return một chuỗi UUID duy nhất
   */
  public static String generateId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Tạo một mã định danh ngắn gọn gồm 8 ký tự đầu của UUID.
   * Thường dùng làm mã phiên đấu giá (Auction Code) để người dùng dễ nhập.
   *
   * @return chuỗi ID rút gọn 8 ký tự
   */
  public static String shortId() {
    return UUID.randomUUID().toString().substring(0, 8);
  }
}