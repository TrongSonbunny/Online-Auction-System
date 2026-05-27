package com.auction.models.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.AuctionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho ItemFactory.
 *
 * <p>EP: tham số hợp lệ / null / blank; giá âm
 * BVA: estimatedPrice = 0 (valid boundary), -0.01 (invalid boundary)
 */
@DisplayName("ItemFactory Tests")
class ItemFactoryTest {

  // ──────── createItem ────────

  @Nested
  @DisplayName("createItem (EP + BVA)")
  class CreateItem {

    @Test
    @DisplayName("EP-Valid: tạo item với dữ liệu hợp lệ")
    void createItem_validData_returnsItem() {
      AuctionItem item = ItemFactory.createItem(
          "iPhone 15",
          "Điện thoại mới",
          ItemCategory.ELECTRONICS,
          "Mới",
          20_000_000.0);

      assertNotNull(item);
      assertEquals("iPhone 15", item.getName());
      assertEquals("Điện thoại mới", item.getDescription());
      assertEquals(ItemCategory.ELECTRONICS, item.getCategory());
      assertEquals("Mới", item.getItemCondition());
      assertEquals(20_000_000.0, item.getEstimatedPrice());
    }

    @Test
    @DisplayName("EP-Valid: itemId được sinh tự động với prefix ITEM-")
    void createItem_autoGeneratesItemIdWithPrefix() {
      AuctionItem item = ItemFactory.createItem(
          "Sách", "Mô tả", ItemCategory.BOOK, "New", 50.0);
      assertNotNull(item.getItemId());
      assertTrue(item.getItemId().startsWith("ITEM-"));
    }

    @Test
    @DisplayName("EP-Valid: hai item có ID khác nhau")
    void createItem_twoCalls_differentIds() {
      AuctionItem item1 = ItemFactory.createItem("A", "D", ItemCategory.ART, "New", 1.0);
      AuctionItem item2 = ItemFactory.createItem("B", "D", ItemCategory.ART, "New", 1.0);
      assertNotEquals(item1.getItemId(), item2.getItemId());
    }

    @Test
    @DisplayName("EP-Invalid: name null ném AuctionException")
    void createItem_nullName_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> ItemFactory.createItem(null, "Desc", ItemCategory.ART, "New", 10.0));
    }

    @Test
    @DisplayName("BVA-Boundary: name rỗng ném AuctionException")
    void createItem_emptyName_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> ItemFactory.createItem("", "Desc", ItemCategory.ART, "New", 10.0));
    }

    @Test
    @DisplayName("EP-Invalid: description null ném AuctionException")
    void createItem_nullDescription_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> ItemFactory.createItem("Name", null, ItemCategory.ART, "New", 10.0));
    }

    @Test
    @DisplayName("BVA-Boundary: estimatedPrice = 0 hợp lệ")
    void createItem_zeroPriceIsValid() {
      AuctionItem item = ItemFactory.createItem("Name", "Desc", ItemCategory.ART, "New", 0.0);
      assertEquals(0.0, item.getEstimatedPrice());
    }

    @Test
    @DisplayName("BVA-Boundary: estimatedPrice = -0.01 ném AuctionException")
    void createItem_slightlyNegativePrice_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> ItemFactory.createItem("Name", "Desc", ItemCategory.ART, "New", -0.01));
    }

    @Test
    @DisplayName("EP-Invalid: category null ném NullPointerException")
    void createItem_nullCategory_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> ItemFactory.createItem("Name", "Desc", null, "New", 10.0));
    }
  }
}
