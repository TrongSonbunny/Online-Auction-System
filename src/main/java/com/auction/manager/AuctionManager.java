package com.auction.manager;

import com.auction.models.Item;
import java.util.List;
import java.util.ArrayList;

public class AuctionManager {
    private static AuctionManager instance;
    private List<Item> auctionItems;

    private AuctionManager() {
        auctionItems = new ArrayList<>();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addItem(Item item) {
        auctionItems.add(item);
        System.out.println("Da them san pham vào he thong: " + item.getName());
    }

    public List<Item> getAuctionItems() {
        return auctionItems;
    }
}
