package com.auction.models.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.exceptions.AuctionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho AuctionItem.
 *
 * <p>EP: giá trị hợp lệ / null / blank / âm
 * BVA: estimatedPrice = -0.01 (invalid), 0 (valid boundary), 0.01 (valid)
 *      name/description null, blank, 1 ký tự
 */
@DisplayName("AuctionItem Tests")
class AuctionItemTest {

  private AuctionItem validItem() {
    return new AuctionItem(
        "ITEM-001",
        "Sách Java",
        "Sách lập trình Java",
        ItemCategory.BOOK,
        "Mới",
        100.0);
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor - ItemId (EP + BVA)")
  class ItemIdValidation {

    @Test
    @DisplayName("EP-Valid: itemId hợp lệ")
    void constructor_validItemId_stores() {
      AuctionItem item = validItem();
      assertEquals("ITEM-001", item.getItemId());
    }

    @Test
    @DisplayName("EP-Invalid: itemId null ném AuctionException")
    void constructor_nullItemId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionItem(null, "Name", "Desc", ItemCategory.ART, "New", 10.0));
    }

    @Test
    @DisplayName("BVA-Boundary: itemId rỗng ném AuctionException")
    void constructor_emptyItemId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionItem("", "Name", "Desc", ItemCategory.ART, "New", 10.0));
    }

    @Test
    @DisplayName("BVA-Boundary: itemId blank ném AuctionException")
    void constructor_blankItemId_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionItem("   ", "Name", "Desc", ItemCategory.ART, "New", 10.0));
    }
  }

  @Nested
  @DisplayName("Constructor - Name (EP + BVA)")
  class NameValidation {

    @Test
    @DisplayName("EP-Valid: name hợp lệ")
    void constructor_validName_stores() {
      assertEquals("Sách Java", validItem().getName());
    }

    @Test
    @DisplayName("EP-Invalid: name null ném AuctionException")
    void constructor_nullName_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionItem("I-001", null, "Desc", ItemCategory.ART, "New", 10.0));
    }

    @Test
    @DisplayName("BVA-Boundary: name rỗng ném AuctionException")
    void constructor_emptyName_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionItem("I-001", "", "Desc", ItemCategory.ART, "New", 10.0));
    }

    @Test
    @DisplayName("BVA-Boundary: name 1 ký tự hợp lệ")
    void constructor_singleCharName_isValid() {
      AuctionItem item = new AuctionItem("I-001", "A", "Desc", ItemCategory.ART, "New", 10.0);
      assertEquals("A", item.getName());
    }
  }

  @Nested
  @DisplayName("Constructor - Description (EP + BVA)")
  class DescriptionValidation {

    @Test
    @DisplayName("EP-Valid: description hợp lệ")
    void constructor_validDescription_stores() {
      assertEquals("Sách lập trình Java", validItem().getDescription());
    }

    @Test
    @DisplayName("EP-Invalid: description null ném AuctionException")
    void constructor_nullDescription_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionItem("I-001", "Name", null, ItemCategory.ART, "New", 10.0));
    }

    @Test
    @DisplayName("BVA-Boundary: description rỗng ném AuctionException")
    void constructor_emptyDescription_throwsAuctionException() {
      assertThrows(AuctionException.class,
          () -> new AuctionItem("I-001", "Name", "", ItemCategory.ART, "New", 10.0));
    }

    @Test
    @DisplayName("BVA-Boundary: description 1 ký tự hợp lệ")
    void constructor_singleCharDescription_isValid() {
      AuctionItem item = new AuctionItem("I-001", "Name", "D", ItemCategory.ART, "New", 10.0);
      assertEquals("D", item.getDescription());
    }
  }

  @Nested
  @DisplayName("Constructor - EstimatedPrice (EP + BVA)")
  class EstimatedPriceValidation {

    @Test
    @DisplayName("BVA-Boundary: estimatedPrice = 0 hợp lệ")
    void constructor_zeroPriceIsValid() {
      AuctionItem item = new AuctionItem("I-001", "Name", "Desc", ItemCategory.ART, "New", 0.0);
      assertEquals(0.0, item.getEstimatedPrice());
    }

    @Test
    @DisplayName("BVA-Boundary: estimatedPrice = 0.01 hợp lệ")
    void constructor_smallPositivePrice_isValid() {
      AuctionItem item = new AuctionItem("I-001", "Name", "Desc", ItemCategory.ART, "New", 0.01);
      assertEquals(0.01, item.getEstimatedPrice(), 0.001);
    }

    @Test
    @DisplayName("BVA-Boundary: estimatedPrice = -0.01 ném AuctionException")
    void constructor_slightlyNegativePrice_throws() {
      assertThrows(AuctionException.class,
          () -> new AuctionItem("I-001", "Name", "Desc", ItemCategory.ART, "New", -0.01));
    }

    @Test
    @DisplayName("EP-Invalid: estimatedPrice âm lớn ném AuctionException")
    void constructor_largeNegativePrice_throws() {
      assertThrows(AuctionException.class,
          () -> new AuctionItem("I-001", "Name", "Desc", ItemCategory.ART, "New", -1000.0));
    }

    @Test
    @DisplayName("EP-Valid: estimatedPrice dương lớn hợp lệ")
    void constructor_largePriceIsValid() {
      AuctionItem item = new AuctionItem("I-001", "Name", 
          "Desc", ItemCategory.ART, "New", 1_000_000.0);
      assertEquals(1_000_000.0, item.getEstimatedPrice());
    }
  }

  @Nested
  @DisplayName("Constructor - Category và Condition (EP)")
  class CategoryAndCondition {

    @Test
    @DisplayName("EP-Valid: category ART lưu đúng")
    void constructor_categoryArt_stores() {
      AuctionItem item = new AuctionItem("I-001", "Name", "Desc", ItemCategory.ART, "Used", 10.0);
      assertEquals(ItemCategory.ART, item.getCategory());
    }

    @Test
    @DisplayName("EP-Invalid: category null ném NullPointerException")
    void constructor_nullCategory_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> new AuctionItem("I-001", "Name", "Desc", null, "New", 10.0));
    }

    @Test
    @DisplayName("EP-Invalid: itemCondition null ném NullPointerException")
    void constructor_nullCondition_throwsNpe() {
      assertThrows(NullPointerException.class,
          () -> new AuctionItem("I-001", "Name", "Desc", ItemCategory.ART, null, 10.0));
    }
  }

  // ──────── Setters ────────

  @Nested
  @DisplayName("Setters (EP + BVA)")
  class Setters {

    @Test
    @DisplayName("EP-Valid: setName hợp lệ cập nhật")
    void setName_valid_updates() {
      AuctionItem item = validItem();
      item.setName("Sách Python");
      assertEquals("Sách Python", item.getName());
    }

    @Test
    @DisplayName("EP-Invalid: setName null ném AuctionException")
    void setName_null_throws() {
      assertThrows(AuctionException.class, () -> validItem().setName(null));
    }

    @Test
    @DisplayName("EP-Invalid: setDescription rỗng ném AuctionException")
    void setDescription_empty_throws() {
      assertThrows(AuctionException.class, () -> validItem().setDescription(""));
    }

    @Test
    @DisplayName("BVA-Boundary: setEstimatedPrice = 0 hợp lệ")
    void setEstimatedPrice_zero_isValid() {
      AuctionItem item = validItem();
      item.setEstimatedPrice(0.0);
      assertEquals(0.0, item.getEstimatedPrice());
    }

    @Test
    @DisplayName("BVA-Boundary: setEstimatedPrice âm ném AuctionException")
    void setEstimatedPrice_negative_throws() {
      assertThrows(AuctionException.class, () -> validItem().setEstimatedPrice(-1.0));
    }

    @Test
    @DisplayName("EP-Invalid: setCategory null ném NullPointerException")
    void setCategory_null_throwsNpe() {
      assertThrows(NullPointerException.class, () -> validItem().setCategory(null));
    }

    @Test
    @DisplayName("EP-Valid: setCategory cập nhật đúng")
    void setCategory_valid_updates() {
      AuctionItem item = validItem();
      item.setCategory(ItemCategory.ELECTRONICS);
      assertEquals(ItemCategory.ELECTRONICS, item.getCategory());
    }

    @Test
    @DisplayName("EP-Invalid: setItemCondition blank ném AuctionException")
    void setItemCondition_blank_throws() {
      assertThrows(AuctionException.class, () -> validItem().setItemCondition("  "));
    }

    @Test
    @DisplayName("EP-Valid: setItemCondition hợp lệ cập nhật")
    void setItemCondition_valid_updates() {
      AuctionItem item = validItem();
      item.setItemCondition("Cũ");
      assertEquals("Cũ", item.getItemCondition());
      assertNotNull(item.toString());
    }
  }
}
