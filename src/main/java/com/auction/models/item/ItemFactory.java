package com.auction.models.item;

import com.auction.models.user.Seller;
import java.time.LocalDateTime;

/**
 * Factory tạo các loại AuctionItem.
 * Áp dụng Factory Method Pattern.
 */
public final class ItemFactory {

    private ItemFactory() {
    }

    public static AuctionItem createItem(
            String type,
            String id,
            String name,
            String description,
            double startPrice,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Seller seller) {

        return switch (type.toLowerCase()) {
            case "art" -> new ArtItem(
                    id, name, description, startPrice, startTime, endTime, seller);
            case "book" -> new BookItem(
                    id, name, description, startPrice, startTime, endTime, seller);
            case "collectible" -> new CollectibleItem(
                    id, name, description, startPrice, startTime, endTime, seller);
            case "electronics" -> new ElectronicsItem(
                    id, name, description, startPrice, startTime, endTime, seller);
            case "fashion" -> new FashionItem(
                    id, name, description, startPrice, startTime, endTime, seller);
            case "food" -> new FoodItem(
                    id, name, description, startPrice, startTime, endTime, seller);
            default -> throw new IllegalArgumentException(
                    "Loại sản phẩm không hợp lệ: \"" + type + "\"");
        };
    }
}