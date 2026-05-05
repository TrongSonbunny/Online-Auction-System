package com.auction.services;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.Item;

/**
 * Lớp dịch vụ (Service) xử lý các nghiệp vụ cốt lõi của hệ thống đấu giá.
 * Tuân thủ nguyên tắc SRP: Tách biệt logic nghiệp vụ khỏi giao diện và dữ liệu.
 */
public class AuctionService {

  /**
   * Xử lý hành động đặt giá cho một sản phẩm.
   *
   * @param item      Sản phẩm đang được đấu giá
   * @param bidAmount Số tiền người dùng muốn đặt
   * @param isClosed  Trạng thái của phiên đấu giá (true nếu đã đóng)
   * @throws InvalidBidException    Nếu giá đặt thấp hơn hoặc bằng giá hiện tại
   * @throws AuctionClosedException Nếu phiên đấu giá đã đóng
   */
  public void placeBid(Item item, double bidAmount, boolean isClosed)
      throws InvalidBidException, AuctionClosedException {

    if (isClosed) {
      throw new AuctionClosedException("Thất bại: Phiên đấu giá cho sản phẩm này đã kết thúc!");
    }

    // Giả sử startingPrice đóng vai trò lưu giá hiện tại cao nhất
    if (bidAmount <= item.getStartingPrice()) {
      throw new InvalidBidException(
          "Giá đặt ($" + bidAmount + ") phải lớn hơn giá hiện tại: $" + item.getStartingPrice());
    }

    // Nếu hợp lệ, cập nhật mức giá mới
    item.setStartingPrice(bidAmount);
  }
}