package com.auction.models.auction;

import com.auction.models.bid.Bid;
import com.auction.models.item.AuctionItem;
import com.auction.models.state.AuctionState;
import com.auction.models.state.OpenState;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho một phiên đấu giá.
 * Quản lý item, trạng thái và danh sách bid.
 */
public class Auction {
  private String id;
  private AuctionItem item;
  private AuctionState state;
  private List<Bid> bids;

  /**
  * Khởi tạo auction, mặc định trạng thái OPEN.
  */
  public Auction(String id, AuctionItem item) {
    this.id = id;
    this.item = item;
    this.state = new OpenState();
    this.bids = new ArrayList<>();
  }

  public void start() { 
    state.start(this); 
  } 

  public void placeBid(Bid bid) { 
    state.placeBid(this, bid); 
  }

  public void end() { 
    state.end(this); 
  }

  public String getId() { 
    return id; 
  }

  public void setId(String id) { 
    this.id = id; 
  }
  
  public AuctionItem getItem() { 
    return item; 
  }
    
  public void setItem(AuctionItem item) { 
    this.item = item; 
  }

  public AuctionState getState() { 
    return state; 
  }
  
  public void setState(AuctionState state) { 
    this.state = state; 
  }

  public List<Bid> getBids() { 
    return bids; 
  }

  public void setBids(List<Bid> bids) { 
    this.bids = bids; 
  }
  
  /**
   * Hiển thị lịch sử đấu giá.
   */
  public void displayHistory() {
    System.out.println("\n--- LỊCH SỬ ĐẤU GIÁ CHO: " + item.getName() + " ---");
    if (bids.isEmpty()) {
      System.out.println("Chưa có lượt trả giá nào.");
    } else {
      for (int i = 0; i < bids.size(); i++) {
        Bid b = bids.get(i);
        System.out.println("Lượt " + (i + 1) + ": " + b.getBidder().getUsername() 
                                   + " trả " + b.getAmount() + " VNĐ");
      }
    }
    System.out.println("\n");
  }
}