package com.auction;

import com.auction.network.NetworkClient;
import com.auction.utils.WindowResizeUtils;
import java.io.IOException;
import java.net.URL;
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

  // LƯU TRỮ ĐỊNH DANH NGƯỜI DÙNG DƯỚI DẠNG CHUỖI (STRING)
  public static String loggedInEmail;
  public static String loggedInPassword;
  public static String loggedInUserId;

  @Override
  public void init() throws Exception {
    NetworkClient.getInstance().connect();
  }

  @Override
  public void stop() throws Exception {
    NetworkClient.getInstance().close();
  }

  @Override
  public void start(Stage stage) throws IOException {
    stage.initStyle(StageStyle.UNDECORATED);
    try {
      String logoPath = "/com/auction/views/assets/logo4.png";
      java.io.InputStream stream = App.class.getResourceAsStream(logoPath);
      Image appIcon = new Image(stream);
      stage.getIcons().add(appIcon);
    } catch (Exception e) {
      System.out.println("Chưa tìm thấy logo4.png, sử dụng mặc định.");
    }

    scene = new Scene(loadFxml("login"), 640, 480);
    stage.setScene(scene);
    WindowResizeUtils.addResizeListener(stage);
    stage.show();
  }

  /**
   * Thay đổi màn hình (Root) của ứng dụng.
   *
   * @param fxml tên file fxml cần load
   * @throws IOException nếu không thể tìm hoặc đọc file fxml
   */
  public static void setRoot(String fxml) throws IOException {
    // Không cần gọi clearListeners() thủ công ở đây nữa
    // Việc dọn dẹp đã được tự động hóa bên trong NetworkClient.addListener()
    scene.setRoot(loadFxml(fxml));
  }

  /**
   * Tải giao diện từ file FXML.
   *
   * @param fxml tên file fxml cần load
   * @return Parent node chứa giao diện
   * @throws IOException nếu không tìm thấy file
   */
  private static Parent loadFxml(String fxml) throws IOException {
    String fxmlPath = "/com/auction/views/" + fxml + ".fxml";
    URL url = App.class.getResource(fxmlPath);
    FXMLLoader fxmlLoader = new FXMLLoader(url);
    return fxmlLoader.load();
  }

  /**
   * Hàm main để khởi chạy ứng dụng.
   *
   * @param args các tham số dòng lệnh
   */
  public static void main(String[] args) {
    launch();
  }
}