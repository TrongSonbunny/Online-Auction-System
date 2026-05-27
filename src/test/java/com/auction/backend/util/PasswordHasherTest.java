package com.auction.backend.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho PasswordHasher.
 *
 * <p>EP: hash hợp lệ, verify đúng/sai, hash cùng password ra kết quả khác nhau.
 */
@DisplayName("PasswordHasher Tests")
class PasswordHasherTest {

  @Nested
  @DisplayName("hash (EP)")
  class Hash {

    @Test
    @DisplayName("EP-Valid: hash trả về chuỗi có 3 phần phân cách bởi ':'")
    void hash_validPassword_returnsThreeParts() {
      String hashed = PasswordHasher.hash("matkhau123");
      assertTrue(hashed.split(":", 3).length == 3);
    }

    @Test
    @DisplayName("EP-Valid: hash cùng password hai lần cho ra chuỗi khác nhau (salt ngẫu nhiên)")
    void hash_samePasswordTwice_producesDifferentHashes() {
      String hash1 = PasswordHasher.hash("matkhau123");
      String hash2 = PasswordHasher.hash("matkhau123");
      assertNotEquals(hash1, hash2);
    }
  }

  @Nested
  @DisplayName("verify (EP)")
  class Verify {

    @Test
    @DisplayName("EP-Valid: verify đúng mật khẩu trả về true")
    void verify_correctPassword_returnsTrue() {
      String raw = "matkhau@123";
      String hashed = PasswordHasher.hash(raw);
      assertTrue(PasswordHasher.verify(raw, hashed));
    }

    @Test
    @DisplayName("EP-Invalid: verify sai mật khẩu trả về false")
    void verify_wrongPassword_returnsFalse() {
      String hashed = PasswordHasher.hash("matkhau@123");
      assertFalse(PasswordHasher.verify("saiMatKhau", hashed));
    }

    @Test
    @DisplayName("EP-Invalid: verify chuỗi hash không đúng format trả về false")
    void verify_malformedHash_returnsFalse() {
      assertFalse(PasswordHasher.verify("matkhau", "invalid-hash"));
    }

    @Test
    @DisplayName("EP-Valid: verify phân biệt hoa/thường")
    void verify_caseSensitive_returnsFalse() {
      String hashed = PasswordHasher.hash("MatKhau");
      assertFalse(PasswordHasher.verify("matkhau", hashed));
    }
  }
}
