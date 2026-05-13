package com.auction.utils;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Tiện ích hỗ trợ thay đổi kích thước cửa sổ cho Undecorated Stage.
 */
public class WindowResizeUtils {

  private static final int MARGIN = 8;
  private static boolean isResizing = false;
  private static Cursor cursorEvent = Cursor.DEFAULT;

  /**
   * Kích hoạt tính năng kéo để thay đổi kích thước cửa sổ ở mép phải và dưới.
   *
   * @param stage Cửa sổ cần gắn listener.
   */
  public static void addResizeListener(Stage stage) {
    Scene scene = stage.getScene();
    if (scene == null) {
      return;
    }

    // Đổi con trỏ chuột khi di chuyển sát các mép
    scene.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
      double x = event.getSceneX();
      double y = event.getSceneY();
      double width = stage.getWidth();
      double height = stage.getHeight();

      if (x >= width - MARGIN && y >= height - MARGIN) {
        cursorEvent = Cursor.SE_RESIZE;
      } else if (x >= width - MARGIN) {
        cursorEvent = Cursor.E_RESIZE;
      } else if (y >= height - MARGIN) {
        cursorEvent = Cursor.S_RESIZE;
      } else {
        cursorEvent = Cursor.DEFAULT;
      }
      scene.setCursor(cursorEvent);
    });

    // Bắt đầu kéo
    scene.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
      if (!cursorEvent.equals(Cursor.DEFAULT)) {
        isResizing = true;
        event.consume();
      }
    });

    // Đang kéo để thay đổi kích thước
    scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
      if (isResizing) {
        if (cursorEvent.equals(Cursor.SE_RESIZE) || cursorEvent.equals(Cursor.E_RESIZE)) {
          // Giới hạn chiều rộng tối thiểu là 400
          stage.setWidth(Math.max(400, event.getScreenX() - stage.getX()));
        }
        if (cursorEvent.equals(Cursor.SE_RESIZE) || cursorEvent.equals(Cursor.S_RESIZE)) {
          // Giới hạn chiều cao tối thiểu là 400
          stage.setHeight(Math.max(400, event.getScreenY() - stage.getY()));
        }
        event.consume();
      }
    });

    // Kết thúc kéo
    scene.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
      isResizing = false;
    });
  }
}