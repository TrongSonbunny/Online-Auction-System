package com.auction.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.Item;
import com.auction.models.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Lớp kiểm thử (Unit Test) cho logic nghiệp vụ trong AuctionService.
 */
public class AuctionServiceTest {

  private AuctionService auctionService;
  private Item testItem;

  /**
   * Khởi tạo dữ liệu mẫu trước mỗi test case.
   */
  @BeforeEach
  public void setUp() {
    auctionService = new AuctionService();
    // Sử dụng lớp con Vehicle để tạo đối tượng Item thử nghiệm
    testItem = new Vehicle("Test Car", 1000.0, "Toyota", 0);
  }

  /**
   * Kiểm thử: Đặt giá hợp lệ. Không được ném ra ngoại lệ và giá phải được cập
   * nhật.
   */
  @Test
  public void testPlaceBidValid() {
    assertDoesNotThrow(() -> auctionService.placeBid(testItem, 1500.0, false));
    assertEquals(1500.0, testItem.getStartingPrice(), "Giá sản phẩm phải được cập nhật thành 1500");
  }

  /**
   * Kiểm thử: Đặt giá thấp hơn giá hiện tại. Phải ném ra InvalidBidException.
   */
  @Test
  public void testPlaceBidInvalidLowerPrice() {
    Exception exception = assertThrows(InvalidBidException.class, () -> {
      auctionService.placeBid(testItem, 500.0, false);
    });

    String expectedMessage = "Giá đặt ($500.0) phải lớn hơn giá hiện tại: $1000.0";
    assertEquals(expectedMessage, exception.getMessage());
  }

  /**
   * Kiểm thử: Đặt giá khi phiên đã đóng. Phải ném ra AuctionClosedException.
   */
  @Test
  public void testPlaceBidWhenClosed() {
    assertThrows(AuctionClosedException.class, () -> {
      auctionService.placeBid(testItem, 2000.0, true);
    });
  }
}