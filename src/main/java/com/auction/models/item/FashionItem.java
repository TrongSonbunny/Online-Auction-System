package com.auction.models.item;

import com.auction.models.user.Seller;
import java.time.LocalDateTime;

/** Sản phẩm loại: Thời trang. */
public class FashionItem extends AuctionItem {

    public FashionItem(
            String id,
            String name,
            String description,
            double startPrice,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Seller seller) {

        super(id, name, description, startPrice, startTime, endTime, seller);
    }

    @Override
    public void printInfo() {
        System.out.println("[Thời trang]"
                + " | Tên: " + getName()
                + " | Mô tả: " + getDescription()
                + " | Giá khởi điểm: " + getStartPrice()
                + " | Giá hiện tại: " + getCurrentPrice()
                + " | Người bán: " + getSeller().getName()
                + " | Bắt đầu: " + getStartTime()
                + " | Kết thúc: " + getEndTime());
    }
}