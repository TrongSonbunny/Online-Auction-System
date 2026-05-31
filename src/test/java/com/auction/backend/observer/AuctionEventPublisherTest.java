package com.auction.backend.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.exceptions.AuctionException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho AuctionEventPublisher - addObserver, removeObserver, publishEvent.
 *
 * <p>EP: observer null / valid; event null / valid; 0 / 1 / nhiều observer
 * BVA: 0 observer (không publish), 1 observer, thêm rồi xóa
 */
@DisplayName("AuctionEventPublisher Tests")
class AuctionEventPublisherTest {

  private AuctionEventPublisher publisher;

  @BeforeEach
  void setUp() {
    publisher = new AuctionEventPublisher();
  }

  private AuctionEvent sampleEvent() {
    return new AuctionEvent(AuctionEventType.AUCTION_CREATED, "AUC-001", "Tạo auction");
  }

  /** Observer giả để kiểm tra thứ tự/số lần nhận event. */
  private static class CountingObserver implements AuctionObserver {
    int count = 0;
    AuctionEvent lastEvent = null;

    @Override
    public void update(AuctionEvent event) {
      count++;
      lastEvent = event;
    }
  }

  // ──────── addObserver ────────

  @Nested
  @DisplayName("addObserver (EP + BVA)")
  class AddObserver {

    @Test
    @DisplayName("EP-Valid: thêm 1 observer, observerCount = 1")
    void addObserver_one_countIsOne() {
      publisher.addObserver(new CountingObserver());
      assertEquals(1, publisher.getObserverCount());
    }

    @Test
    @DisplayName("EP-Valid: thêm 3 observer, observerCount = 3")
    void addObserver_three_countIsThree() {
      publisher.addObserver(new CountingObserver());
      publisher.addObserver(new CountingObserver());
      publisher.addObserver(new CountingObserver());
      assertEquals(3, publisher.getObserverCount());
    }

    @Test
    @DisplayName("EP-Invalid: thêm null ném AuctionException")
    void addObserver_null_throwsAuctionException() {
      assertThrows(AuctionException.class, () -> publisher.addObserver(null));
    }

    @Test
    @DisplayName("BVA-Boundary: ban đầu observerCount = 0")
    void initial_observerCount_isZero() {
      assertEquals(0, publisher.getObserverCount());
    }
  }

  // ──────── removeObserver ────────

  @Nested
  @DisplayName("removeObserver (EP + BVA)")
  class RemoveObserver {

    @Test
    @DisplayName("EP-Valid: xóa observer giảm count")
    void removeObserver_existing_decreasesCount() {
      CountingObserver obs = new CountingObserver();
      publisher.addObserver(obs);
      publisher.removeObserver(obs);
      assertEquals(0, publisher.getObserverCount());
    }

    @Test
    @DisplayName("EP-Invalid: xóa observer không tồn tại không ném exception")
    void removeObserver_nonExisting_noException() {
      publisher.removeObserver(new CountingObserver());
      assertEquals(0, publisher.getObserverCount());
    }

    @Test
    @DisplayName("BVA-Boundary: thêm 2, xóa 1, còn 1")
    void removeObserver_fromTwo_remainsOne() {
      CountingObserver obs1 = new CountingObserver();
      CountingObserver obs2 = new CountingObserver();
      publisher.addObserver(obs1);
      publisher.addObserver(obs2);
      publisher.removeObserver(obs1);
      assertEquals(1, publisher.getObserverCount());
    }
  }

  // ──────── publishEvent ────────

  @Nested
  @DisplayName("publishEvent (EP + BVA)")
  class PublishEvent {

    @Test
    @DisplayName("EP-Valid: 1 observer nhận event đúng 1 lần")
    void publishEvent_oneObserver_receivesOnce() {
      CountingObserver obs = new CountingObserver();
      publisher.addObserver(obs);
      publisher.publishEvent(sampleEvent());
      assertEquals(1, obs.count);
    }

    @Test
    @DisplayName("EP-Valid: 3 observer đều nhận event")
    void publishEvent_threeObservers_allReceive() {
      List<CountingObserver> observers = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        CountingObserver obs = new CountingObserver();
        observers.add(obs);
        publisher.addObserver(obs);
      }
      publisher.publishEvent(sampleEvent());
      for (CountingObserver obs : observers) {
        assertEquals(1, obs.count);
      }
    }

    @Test
    @DisplayName("EP-Valid: observer nhận đúng event")
    void publishEvent_observerReceivesCorrectEvent() {
      CountingObserver obs = new CountingObserver();
      publisher.addObserver(obs);
      AuctionEvent event = sampleEvent();
      publisher.publishEvent(event);
      assertEquals(event, obs.lastEvent);
    }

    @Test
    @DisplayName("BVA-Boundary: 0 observer, publishEvent không ném exception")
    void publishEvent_noObservers_doesNotThrow() {
      publisher.publishEvent(sampleEvent());
      // không ném exception
    }

    @Test
    @DisplayName("EP-Invalid: publishEvent null ném AuctionException")
    void publishEvent_null_throwsAuctionException() {
      assertThrows(AuctionException.class, () -> publisher.publishEvent(null));
    }

    @Test
    @DisplayName("EP-Valid: publish 3 events, observer nhận 3 lần")
    void publishEvent_threeEvents_receivedThreeTimes() {
      CountingObserver obs = new CountingObserver();
      publisher.addObserver(obs);
      publisher.publishEvent(sampleEvent());
      publisher.publishEvent(new AuctionEvent(AuctionEventType.NEW_BID, "AUC-001", "Bid mới"));
      publisher.publishEvent(
          new AuctionEvent(AuctionEventType.AUCTION_FINISHED, "AUC-001", "Kết thúc"));
      assertEquals(3, obs.count);
    }

    @Test
    @DisplayName("BVA-Boundary: observer được xóa không nhận event tiếp theo")
    void publishEvent_afterRemove_removedObserverNotNotified() {
      CountingObserver obs = new CountingObserver();
      publisher.addObserver(obs);
      publisher.publishEvent(sampleEvent());
      publisher.removeObserver(obs);
      publisher.publishEvent(sampleEvent());
      assertEquals(1, obs.count);
    }
  }
}
