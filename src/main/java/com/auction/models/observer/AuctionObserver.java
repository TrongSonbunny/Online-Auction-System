package com.auction.models.observer;

/**
 * Interface định nghĩa hành động "Nhận thông báo" của Observer Pattern.
 * Bất kỳ đối tượng nào muốn lắng nghe sự kiện từ phiên đấu giá
 * đều phải triển khai interface này.
 */
public interface AuctionObserver {

    /**
     * Được gọi khi có sự kiện mới trong phiên đấu giá.
     *
     * @param event Sự kiện đấu giá được gửi đến Observer
     */
    void update(AuctionEvent event);
}