package com.auction;

/**
 * Entry point cho client fat JAR.
 *
 * <p>JavaFX kiểm tra module system khi main class kế thừa {@link javafx.application.Application}.
 * Class này không kế thừa Application nên tránh được lỗi
 * "JavaFX runtime components are missing" khi chạy từ fat JAR.
 */
public class Launcher {

  /**
   * Khởi động ứng dụng JavaFX client.
   *
   * @param args tham số dòng lệnh
   */
  public static void main(String[] args) {
    App.main(args);
  }
}
