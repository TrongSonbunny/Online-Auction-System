package com.auction.models.user;

import com.auction.models.core.Auction;
import com.auction.models.core.AuctionManager;
import com.auction.models.observer.AuctionEvent;

import java.util.List;

/**
 * Lớp đại diện cho Admin.
 */
public class Admin extends User {

    public Admin(String userId, String name, String email, String passwordHash) {
        super(userId, name, email, passwordHash);
    }

    public List<Auction> getAllAuctions() {
        return AuctionManager.getInstance().getAuctions();
    }

    public void cancelAuction(String auctionId) {
        Auction auction = AuctionManager.getInstance().findById(auctionId);

        if (auction == null) {
            throw new IllegalArgumentException("Auction not found");
        }

        auction.cancelAuction();
    }

    public void removeAuction(String auctionId) {
        AuctionManager.getInstance().removeAuction(auctionId);
    }

    @Override
    public void update(AuctionEvent event) {
        System.out.println("[ADMIN LOG] " + event);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}