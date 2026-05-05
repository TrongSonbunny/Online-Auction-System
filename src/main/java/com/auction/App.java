package com.auction;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Lớp khởi chạy ứng dụng JavaFX cho hệ thống đấu giá.
 */
public class App extends Application {

  private static Scene scene;

  /**
   * Phương thức khởi chạy giao diện chính của ứng dụng.
   *
   * @param stage Cửa sổ chính của ứng dụng (Stage)
   * @throws IOException Nếu không thể tìm thấy hoặc tải được file FXML
   */
  @Override
  public void start(Stage stage) throws IOException {
    // Đã sửa loadFXML thành loadFxml
    scene = new Scene(loadFxml("login"), 640, 480);
    stage.setScene(scene);
    stage.show();
  }

  /**
   * Thay đổi giao diện gốc (Root) của Scene hiện tại.
   *
   * @param fxml Tên file FXML cần tải (không bao gồm phần mở rộng .fxml)
   * @throws IOException Nếu không thể tải được file FXML
   */
  static void setRoot(String fxml) throws IOException {
    // Đã sửa loadFXML thành loadFxml
    scene.setRoot(loadFxml(fxml));
  }

  /**
   * Tải nội dung từ một file FXML.
   *
   * @param fxml Tên file FXML (không bao gồm phần mở rộng .fxml)
   * @return Đối tượng Parent chứa cấu trúc giao diện đã tải
   * @throws IOException Nếu không thể đọc được file
   */
  // Đã sửa tên hàm loadFXML thành loadFxml để tuân thủ Google Checkstyle
  private static Parent loadFxml(String fxml) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    return fxmlLoader.load();
  }

  /**
   * Điểm bắt đầu của ứng dụng (Entry point).
   *
   * @param args Các tham số truyền vào từ dòng lệnh
   */
  public static void main(String[] args) {
    launch();
  }
}