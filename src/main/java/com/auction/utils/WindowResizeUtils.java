package com.auction.utils;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Tiện ích hỗ trợ thay đổi kích thước cửa sổ cho Undecorated Stage.
 */
public class WindowResizeUtils {

  private static final int MARGIN = 10;
  private static boolean isResizing = false;
  private static Cursor activeEdgeCursor = Cursor.DEFAULT;

  /**
   * Kích hoạt tính năng kéo để thay đổi kích thước cửa sổ ở mép phải và dưới.
   *
   * @param stage cửa sổ cần thêm tính năng
   */
  public static void addResizeListener(Stage stage) {
    Scene scene = stage.getScene();
    if (scene == null) {
      return;
    }

    scene.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
      if (isResizing) {
        return;
      }

      double x = event.getSceneX();
      double y = event.getSceneY();
      double width = stage.getWidth();
      double height = stage.getHeight();

      Cursor targetCursor = Cursor.DEFAULT;

      if (x >= width - MARGIN && y >= height - MARGIN) {
        targetCursor = Cursor.SE_RESIZE;
      } else if (x >= width - MARGIN) {
        targetCursor = Cursor.E_RESIZE;
      } else if (y >= height - MARGIN) {
        targetCursor = Cursor.S_RESIZE;
      }

      // Chỉ đổi khi thực sự chạm viền. Nếu không, gỡ bỏ ép buộc (null)
      // để các nút bấm bên trong tự động hiện con trỏ bàn tay (Hand Cursor).
      if (targetCursor != Cursor.DEFAULT) {
        scene.setCursor(targetCursor);
        activeEdgeCursor = targetCursor;
      } else {
        scene.setCursor(null);
        activeEdgeCursor = Cursor.DEFAULT;
      }
    });

    scene.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
      if (activeEdgeCursor != Cursor.DEFAULT) {
        isResizing = true;
        event.consume();
      }
    });

    scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
      if (isResizing) {
        if (activeEdgeCursor == Cursor.SE_RESIZE || activeEdgeCursor == Cursor.E_RESIZE) {
          stage.setWidth(Math.max(800, event.getScreenX() - stage.getX()));
        }
        if (activeEdgeCursor == Cursor.SE_RESIZE || activeEdgeCursor == Cursor.S_RESIZE) {
          stage.setHeight(Math.max(600, event.getScreenY() - stage.getY()));
        }
        event.consume();
      }
    });

    scene.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> isResizing = false);
  }
}