package com.auction.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho IdGenerator - format và tính unique của ID.
 *
 * <p>EP: generateAuctionId / generateTransactionId trả chuỗi đúng định dạng
 * BVA: 2 lần gọi → 2 ID khác nhau; ID đúng độ dài; prefix đúng
 */
@DisplayName("IdGenerator Tests")
class IdGeneratorTest {

  // ──────── generateAuctionId ────────

  @Nested
  @DisplayName("generateAuctionId (EP + BVA)")
  class GenerateAuctionId {

    @Test
    @DisplayName("EP-Valid: kết quả không null")
    void generateAuctionId_notNull() {
      assertNotNull(IdGenerator.generateAuctionId());
    }

    @Test
    @DisplayName("EP-Valid: bắt đầu bằng 'AUC-'")
    void generateAuctionId_startsWithAucPrefix() {
      assertTrue(IdGenerator.generateAuctionId().startsWith("AUC-"));
    }

    @Test
    @DisplayName("BVA-Boundary: độ dài = 12 (AUC- + 8 ký tự)")
    void generateAuctionId_lengthIsTwelve() {
      assertEquals(12, IdGenerator.generateAuctionId().length());
    }

    @Test
    @DisplayName("EP-Valid: phần sau prefix là chữ hoa")
    void generateAuctionId_suffixIsUpperCase() {
      String id = IdGenerator.generateAuctionId();
      String suffix = id.substring(4);
      assertEquals(suffix.toUpperCase(), suffix);
    }

    @Test
    @DisplayName("BVA-Boundary: 2 lần gọi trả 2 ID khác nhau")
    void generateAuctionId_twoCalls_differentIds() {
      String id1 = IdGenerator.generateAuctionId();
      String id2 = IdGenerator.generateAuctionId();
      assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("EP-Valid: 100 ID đều unique")
    void generateAuctionId_hundredCalls_allUnique() {
      Set<String> ids = new HashSet<>();
      for (int i = 0; i < 100; i++) {
        ids.add(IdGenerator.generateAuctionId());
      }
      assertEquals(100, ids.size());
    }
  }

  // ──────── generateTransactionId ────────

  @Nested
  @DisplayName("generateTransactionId (EP + BVA)")
  class GenerateTransactionId {

    @Test
    @DisplayName("EP-Valid: kết quả không null")
    void generateTransactionId_notNull() {
      assertNotNull(IdGenerator.generateTransactionId());
    }

    @Test
    @DisplayName("EP-Valid: bắt đầu bằng 'TRANS-'")
    void generateTransactionId_startsWithTransPrefix() {
      assertTrue(IdGenerator.generateTransactionId().startsWith("TRANS-"));
    }

    @Test
    @DisplayName("BVA-Boundary: độ dài = 14 (TRANS- + 8 ký tự)")
    void generateTransactionId_lengthIsFourteen() {
      assertEquals(14, IdGenerator.generateTransactionId().length());
    }

    @Test
    @DisplayName("EP-Valid: phần sau prefix là chữ hoa")
    void generateTransactionId_suffixIsUpperCase() {
      String id = IdGenerator.generateTransactionId();
      String suffix = id.substring(6);
      assertEquals(suffix.toUpperCase(), suffix);
    }

    @Test
    @DisplayName("BVA-Boundary: 2 lần gọi trả 2 ID khác nhau")
    void generateTransactionId_twoCalls_differentIds() {
      String id1 = IdGenerator.generateTransactionId();
      String id2 = IdGenerator.generateTransactionId();
      assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("EP-Valid: 100 ID đều unique")
    void generateTransactionId_hundredCalls_allUnique() {
      Set<String> ids = new HashSet<>();
      for (int i = 0; i < 100; i++) {
        ids.add(IdGenerator.generateTransactionId());
      }
      assertEquals(100, ids.size());
    }
  }
}
