package com.auction;

import com.auction.network.NetworkClient;
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
 * Quản lý vòng đời ứng dụng và thông tin xác thực toàn cục.
 */
public class App extends Application {

  private static Scene scene;

  // Cặp bài trùng "Thẻ căn cước" để vượt qua bảo mật Stateless của Server
  public static String loggedInEmail;
  public static String loggedInPassword;

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
      Image appIcon = new Image(
          App.class.getResourceAsStream("/com/auction/views/assets/logo4.png"));
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
   * Thay đổi giao diện gốc (Root) của Scene hiện tại.
   *
   * @param fxml Tên file FXML cần tải
   * @throws IOException Nếu không thể tải được file FXML
   */
  public static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFxml(fxml));
  }

  private static Parent loadFxml(String fxml) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(
        App.class.getResource("/com/auction/views/" + fxml + ".fxml"));
    return fxmlLoader.load();
  }

  public static void main(String[] args) {
    launch();
  }
}