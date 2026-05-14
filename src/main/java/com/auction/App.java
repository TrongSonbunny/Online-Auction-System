package com.auction;

import com.auction.utils.WindowResizeUtils;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    // 1. Xóa khung viền mặc định của hệ điều hành
    stage.initStyle(StageStyle.UNDECORATED);

    // 2. Thiết lập logo mới cho ứng dụng
    try {
      Image appIcon = new Image(App.class.getResourceAsStream("/com/auction/assets/logo4.png"));
      stage.getIcons().add(appIcon);
    } catch (Exception e) {
      System.out.println("Chưa tìm thấy logo4.png trong thư mục assets, sử dụng mặc định.");
    }
    scene = new Scene(loadFxml("login"), 640, 480);
    stage.setScene(scene);

    // 3. Khôi phục tính năng kéo giãn cửa sổ (Resize)
    WindowResizeUtils.addResizeListener(stage);

    stage.show();
  }

  /**
   * Thay đổi giao diện gốc (Root) của Scene hiện tại.
   * LƯU Ý: Phải có từ khóa 'public' để các Controller ở package khác có thể gọi
   * được.
   *
   * @param fxml Tên file FXML cần tải (không bao gồm phần mở rộng .fxml)
   * @throws IOException Nếu không thể tải được file FXML
   */
  public static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFxml(fxml));
  }

  /**
   * Tải nội dung từ một file FXML.
   *
   * @param fxml Tên file FXML (không bao gồm phần mở rộng .fxml)
   * @return Đối tượng Parent chứa cấu trúc giao diện đã tải
   * @throws IOException Nếu không thể đọc được file
   */
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