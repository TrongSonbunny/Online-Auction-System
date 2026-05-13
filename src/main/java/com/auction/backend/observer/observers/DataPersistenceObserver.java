package com.auction.backend.observer.observers;

import com.auction.backend.database.DataManager;
import com.auction.backend.observer.AuctionEvent;
import com.auction.backend.observer.AuctionObserver;
import com.auction.exceptions.AuctionException;

/**
 * Observer kích hoạt lưu dữ liệu vào database khi có auction event.
 *
 * <p>Luồng xử lý:
 * <ol>
 *   <li>Nhận {@link AuctionEvent} từ publisher.
 *   <li>Gọi {@link DataManager#persist(AuctionEvent)} — database team
 *       xử lý INSERT/UPDATE tương ứng với eventType.
 * </ol>
 *
 * <p>Database team cần implement {@link DataManager} và truyền vào đây.
 */
public class DataPersistenceObserver
    implements AuctionObserver {

  private final DataManager dataManager;

  /**
   * Constructor data persistence observer.
   *
   * @param dataManager implementation của team database
   */
  public DataPersistenceObserver(
      DataManager dataManager) {

    if (dataManager == null) {

      throw new AuctionException(
          "DataManager không được null.");
    }

    this.dataManager = dataManager;
  }

  /**
   * Nhận event và gọi {@link DataManager#persist} để lưu dữ liệu vào database.
   * Event null được bỏ qua.
   *
   * @param event event được publish
   */
  @Override
  public void update(AuctionEvent event) {

    if (event == null) {
      return;
    }

    dataManager.persist(event);
  }

  @Override
  public String toString() {

    return "DataPersistenceObserver{}";
  }
}
