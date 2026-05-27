package com.auction.models.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.BidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho Seller.
 *
 * <p>EP: dữ liệu hợp lệ / null / blank; quyền đúng vai trò
 * BVA: email với @ / không @; totalAuctionsCreated từ 0 → 1
 */
@DisplayName("Seller Tests")
class SellerTest {

  private Seller validSeller() {
    return new Seller("SELLER-01", "Trần Seller", "seller@example.com");
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor Validation (EP + BVA)")
  class ConstructorValidation {

    @Test
    @DisplayName("EP-Valid: tạo seller với dữ liệu hợp lệ")
    void constructor_validData_createsSeller() {
      Seller seller = validSeller();
      assertEquals("SELLER-01", seller.getUserId());
      assertEquals("Trần Seller", seller.getName());
      assertEquals("seller@example.com", seller.getEmail());
      assertEquals(UserRole.SELLER, seller.getRole());
    }

    @Test
    @DisplayName("BVA-Boundary: totalAuctionsCreated ban đầu là 0")
    void constructor_initialTotalAuctions_isZero() {
      assertEquals(0, validSeller().getTotalAuctionsCreated());
    }

    @Test
    @DisplayName("EP-Invalid: userId null ném BidException")
    void constructor_nullUserId_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Seller(null, "Seller", "seller@test.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: userId blank ném BidException")
    void constructor_blankUserId_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Seller("  ", "Seller", "seller@test.com"));
    }

    @Test
    @DisplayName("EP-Invalid: name null ném BidException")
    void constructor_nullName_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Seller("S-01", null, "seller@test.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: name rỗng ném BidException")
    void constructor_emptyName_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Seller("S-01", "", "seller@test.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: name 1 ký tự hợp lệ")
    void constructor_singleCharName_isValid() {
      Seller seller = new Seller("S-01", "S", "s@t.com");
      assertEquals("S", seller.getName());
    }

    @Test
    @DisplayName("EP-Invalid: email null ném BidException")
    void constructor_nullEmail_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Seller("S-01", "Seller", null));
    }

    @Test
    @DisplayName("EP-Invalid: email thiếu @ ném BidException")
    void constructor_emailWithoutAt_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Seller("S-01", "Seller", "invalid.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: email chỉ có @ hợp lệ")
    void constructor_emailWithOnlyAt_isValid() {
      Seller seller = new Seller("S-01", "Seller", "@");
      assertEquals("@", seller.getEmail());
    }
  }

  // ──────── Permissions ────────

  @Nested
  @DisplayName("Seller Permissions")
  class SellerPermissions {

    @Test
    @DisplayName("Seller có quyền canCreateAuction")
    void seller_canCreateAuction() {
      assertTrue(validSeller().canCreateAuction());
    }

    @Test
    @DisplayName("Seller không có quyền canPlaceBid")
    void seller_cannotPlaceBid() {
      assertFalse(validSeller().canPlaceBid());
    }

    @Test
    @DisplayName("Seller không có quyền canDeleteAuction")
    void seller_cannotDeleteAuction() {
      assertFalse(validSeller().canDeleteAuction());
    }

    @Test
    @DisplayName("Seller không có quyền canBanUser")
    void seller_cannotBanUser() {
      assertFalse(validSeller().canBanUser());
    }

    @Test
    @DisplayName("Seller không có quyền canManageSystem")
    void seller_cannotManageSystem() {
      assertFalse(validSeller().canManageSystem());
    }
  }

  // ──────── Auction Counter ────────

  @Nested
  @DisplayName("Auction Counter (BVA)")
  class AuctionCounter {

    @Test
    @DisplayName("BVA-Boundary: increment từ 0 -> 1")
    void incrementAuctionCreated_fromZeroToOne() {
      Seller seller = validSeller();
      seller.incrementAuctionCreated();
      assertEquals(1, seller.getTotalAuctionsCreated());
    }

    @Test
    @DisplayName("EP-Valid: increment nhiều lần tích lũy đúng")
    void incrementAuctionCreated_multipleTimes_accumulates() {
      Seller seller = validSeller();
      for (int i = 0; i < 5; i++) {
        seller.incrementAuctionCreated();
      }
      assertEquals(5, seller.getTotalAuctionsCreated());
    }
  }
}
