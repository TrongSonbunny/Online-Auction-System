package com.auction.factory;

import com.auction.models.*;

public class ItemFactory {
    public static Item createItem(String type, String name, double price, int extraInfo) {
        switch (type.toLowerCase()) {
            case "electronics":
                return new Electronics(name, price, extraInfo);
            default:
                throw new IllegalArgumentException("Không hỗ trợ loại sản phẩm này: " + type);
        }
    }
}
