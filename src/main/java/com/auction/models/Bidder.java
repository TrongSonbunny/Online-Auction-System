package com.auction.models;

public class Bidder extends User {
    private double balance;

    public Bidder(String username, String password, double balance) {
        super(username, password);
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public void printInfo() {
        System.out.println("[bidder] " + getUsername() + " - Số dư: $" + balance);
    }
}