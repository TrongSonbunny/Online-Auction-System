package com.auction.models.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exceptions.BidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho Admin.
 *
 * <p>EP: userId/name/email hợp lệ / null / blank
 * BVA: email thiếu @ / có @; name 1 ký tự / rỗng
 */
@DisplayName("Admin Tests")
class AdminTest {

  // ──────── Helpers ────────

  private Admin validAdmin() {
    return new Admin("ADMIN-01", "Nguyễn Admin", "admin@example.com");
  }

  // ──────── Constructor ────────

  @Nested
  @DisplayName("Constructor Validation (EP + BVA)")
  class ConstructorValidation {

    @Test
    @DisplayName("EP-Valid: tạo admin với dữ liệu hợp lệ")
    void constructor_validData_createsAdmin() {
      Admin admin = new Admin("ADMIN-01", "Admin Name", "admin@test.com");
      assertNotNull(admin);
      assertEquals("ADMIN-01", admin.getUserId());
      assertEquals("Admin Name", admin.getName());
      assertEquals("admin@test.com", admin.getEmail());
      assertEquals(UserRole.ADMIN, admin.getRole());
    }

    @Test
    @DisplayName("EP-Invalid: userId null ném BidException")
    void constructor_nullUserId_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Admin(null, "Admin", "admin@test.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: userId rỗng ném BidException")
    void constructor_emptyUserId_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Admin("", "Admin", "admin@test.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: userId toàn khoảng trắng ném BidException")
    void constructor_blankUserId_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Admin("   ", "Admin", "admin@test.com"));
    }

    @Test
    @DisplayName("EP-Invalid: name null ném BidException")
    void constructor_nullName_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Admin("ADMIN-01", null, "admin@test.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: name rỗng ném BidException")
    void constructor_emptyName_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Admin("ADMIN-01", "", "admin@test.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: name 1 ký tự hợp lệ")
    void constructor_singleCharName_isValid() {
      Admin admin = new Admin("ADMIN-01", "A", "admin@test.com");
      assertEquals("A", admin.getName());
    }

    @Test
    @DisplayName("EP-Invalid: email null ném BidException")
    void constructor_nullEmail_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Admin("ADMIN-01", "Admin", null));
    }

    @Test
    @DisplayName("EP-Invalid: email không có @ ném BidException")
    void constructor_emailWithoutAt_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Admin("ADMIN-01", "Admin", "invalidemail.com"));
    }

    @Test
    @DisplayName("BVA-Boundary: email chỉ có @ là hợp lệ")
    void constructor_emailWithOnlyAt_isValid() {
      Admin admin = new Admin("ADMIN-01", "Admin", "@");
      assertEquals("@", admin.getEmail());
    }

    @Test
    @DisplayName("BVA-Boundary: email rỗng ném BidException")
    void constructor_emptyEmail_throwsBidException() {
      assertThrows(BidException.class,
          () -> new Admin("ADMIN-01", "Admin", ""));
    }
  }

  // ──────── Permissions ────────

  @Nested
  @DisplayName("Admin Permissions")
  class AdminPermissions {

    @Test
    @DisplayName("Admin có quyền canCreateAuction")
    void admin_canCreateAuction() {
      assertTrue(validAdmin().canCreateAuction());
    }

    @Test
    @DisplayName("Admin có quyền canPlaceBid")
    void admin_canPlaceBid() {
      assertTrue(validAdmin().canPlaceBid());
    }

    @Test
    @DisplayName("Admin có quyền canDeleteAuction")
    void admin_canDeleteAuction() {
      assertTrue(validAdmin().canDeleteAuction());
    }

    @Test
    @DisplayName("Admin có quyền canBanUser")
    void admin_canBanUser() {
      assertTrue(validAdmin().canBanUser());
    }

    @Test
    @DisplayName("Admin có quyền canManageSystem")
    void admin_canManageSystem() {
      assertTrue(validAdmin().canManageSystem());
    }
  }

  // ──────── Setters ────────

  @Nested
  @DisplayName("Setters (EP + BVA)")
  class Setters {

    @Test
    @DisplayName("EP-Valid: setName hợp lệ cập nhật tên")
    void setName_validName_updatesName() {
      Admin admin = validAdmin();
      admin.setName("New Admin Name");
      assertEquals("New Admin Name", admin.getName());
    }

    @Test
    @DisplayName("EP-Invalid: setName null ném BidException")
    void setName_null_throwsBidException() {
      assertThrows(BidException.class, () -> validAdmin().setName(null));
    }

    @Test
    @DisplayName("BVA-Boundary: setName rỗng ném BidException")
    void setName_empty_throwsBidException() {
      assertThrows(BidException.class, () -> validAdmin().setName(""));
    }

    @Test
    @DisplayName("EP-Valid: setEmail hợp lệ cập nhật email")
    void setEmail_validEmail_updatesEmail() {
      Admin admin = validAdmin();
      admin.setEmail("newemail@test.com");
      assertEquals("newemail@test.com", admin.getEmail());
    }

    @Test
    @DisplayName("EP-Invalid: setEmail không có @ ném BidException")
    void setEmail_withoutAt_throwsBidException() {
      assertThrows(BidException.class,
          () -> validAdmin().setEmail("invalidemail"));
    }
  }
}
