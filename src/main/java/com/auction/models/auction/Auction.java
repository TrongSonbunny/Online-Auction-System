package com.auction.models.auction;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.bid.Bid;
import com.auction.models.item.AuctionItem;
import com.auction.models.state.AuctionState;
import com.auction.models.state.FinishedState;
import com.auction.models.state.OpenState;
import com.auction.models.state.RunningState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Auction {

  private static final String STATE_OPEN = "OPEN";
  private static final String STATE_RUNNING = "RUNNING";
  private static final String STATE_FINISHED = "FINISHED";

  private final String id;
  private AuctionItem item;
  private AuctionState state;
  private String stateName;

  private final List<Bid> bids;

  //xử lý tuần tự
  private final ExecutorService executor;

  public Auction(String id, AuctionItem item) {
    this.id = id;
    this.item = item;
    this.state = new OpenState();
    this.stateName = state.getStateName();
    this.bids = Collections.synchronizedList(new ArrayList<>());

    this.executor = Executors.newSingleThreadExecutor();
  }

  public String getId() {
    return id;
  }

  public AuctionItem getItem() {
    return item;
  }

  public List<Bid> getBids() {
    return bids;
  }

  public void setState(AuctionState state) {
    this.state = state;
    this.stateName = state.getStateName();
  }

  public void start() {
    executor.submit(() -> {
      try {
        state.start(this);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    });
  }

  public void placeBid(Bid bid) {
    executor.submit(() -> {
      try {
        state.placeBid(this, bid);
      } catch (InvalidBidException | AuctionClosedException e) {
        System.out.println(e.getMessage());
      }
    });
  }

  public void end() {
    executor.submit(() -> {
      try {
        state.end(this);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    });
  }

  public void restoreState() {
    if (stateName == null) {
      return;
    }

    switch (stateName) {
      case STATE_OPEN:
        state = new OpenState();
        break;
      case STATE_RUNNING:
        state = new RunningState();
        break;
      case STATE_FINISHED:
        state = new FinishedState();
        break;
      default:
        state = new OpenState();
    }
  }

  public void shutdown() {
    executor.shutdown();
  }

  public void displayHistory() {
  synchronized (bids) {
    System.out.println("\n--- LỊCH SỬ ĐẤU GIÁ ---");

    if (bids.isEmpty()) {
      System.out.println("Chưa có bid nào.");
      return;
    }

    int i = 1;
    for (Bid b : bids) {
      System.out.println(
          i++ + ". " +
          b.getBidder().getUsername() +
          " → " +
          b.getAmount());
    }
  }
}
}