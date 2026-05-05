package com.auction.controllers;

import com.auction.App;
import java.io.IOException;
import javafx.fxml.FXML;

/**
 * Controller xử lý các sự kiện giao diện cho màn hình phụ (Secondary).
 */
public class SecondaryController {

  /**
   * Chuyển đổi giao diện hiện tại về màn hình chính (primary).
   *
   * @throws IOException Nếu có lỗi trong quá trình tải file FXML của màn hình
   *                     chính
   */
  @FXML
  private void switchToPrimary() throws IOException {
    App.setRoot("primary");
  }
}