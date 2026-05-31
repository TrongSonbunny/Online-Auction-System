package com.auction.backend.util;

import com.auction.exceptions.AuctionException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Tiện ích hash và xác minh mật khẩu dùng PBKDF2WithHmacSHA256.
 *
 * <p>Format lưu trữ: {@code {iterations}:{hex_salt}:{hex_hash}} — tự mô tả,
 * cho phép tăng iterations mà không phá vỡ mật khẩu cũ.
 */
public final class PasswordHasher {

  private static final String ALGORITHM =
      "PBKDF2WithHmacSHA256";

  private static final int ITERATIONS = 310_000;

  private static final int SALT_BYTES = 16;

  private static final int HASH_BYTES = 32;

  /**
   * Private constructor — utility class.
   */
  private PasswordHasher() {
  }

  /**
   * Hash mật khẩu với salt ngẫu nhiên.
   *
   * @param rawPassword mật khẩu gốc chưa hash
   * @return chuỗi {@code iterations:hex_salt:hex_hash} để lưu DB
   */
  public static String hash(String rawPassword) {

    byte[] salt = generateSalt();

    byte[] hashBytes =
        pbkdf2(
            rawPassword.toCharArray(),
            salt,
            ITERATIONS,
            HASH_BYTES);

    return ITERATIONS
        + ":"
        + toHex(salt)
        + ":"
        + toHex(hashBytes);
  }

  /**
   * Xác minh mật khẩu nhập vào khớp với hash đã lưu.
   *
   * @param rawPassword mật khẩu người dùng nhập
   * @param storedHash chuỗi hash đã lưu trong DB
   * @return true nếu khớp
   */
  public static boolean verify(
      String rawPassword,
      String storedHash) {

    String[] parts = storedHash.split(":", 3);

    if (parts.length != 3) {
      return false;
    }

    int iterations =
        Integer.parseInt(parts[0]);

    byte[] salt =
        fromHex(parts[1]);

    byte[] expectedHash =
        fromHex(parts[2]);

    byte[] actualHash =
        pbkdf2(
            rawPassword.toCharArray(),
            salt,
            iterations,
            expectedHash.length);

    return constantTimeEquals(
        expectedHash,
        actualHash);
  }

  /**
   * Tính PBKDF2.
   */
  private static byte[] pbkdf2(
      char[] password,
      byte[] salt,
      int iterations,
      int keyBytes) {

    try {

      PBEKeySpec spec =
          new PBEKeySpec(
              password,
              salt,
              iterations,
              keyBytes * 8);

      SecretKeyFactory factory =
          SecretKeyFactory.getInstance(ALGORITHM);

      byte[] result =
          factory.generateSecret(spec).getEncoded();

      spec.clearPassword();

      return result;

    } catch (NoSuchAlgorithmException
        | InvalidKeySpecException exception) {

      throw new AuctionException(
          "Lỗi hash mật khẩu.",
          exception);
    }
  }

  /**
   * Sinh salt ngẫu nhiên an toàn.
   */
  private static byte[] generateSalt() {

    byte[] salt = new byte[SALT_BYTES];

    new SecureRandom().nextBytes(salt);

    return salt;
  }

  /**
   * So sánh hai mảng byte trong thời gian hằng số để chống timing attack.
   */
  private static boolean constantTimeEquals(
      byte[] a,
      byte[] b) {

    if (a.length != b.length) {
      return false;
    }

    int diff = 0;

    for (int i = 0; i < a.length; i++) {
      diff |= a[i] ^ b[i];
    }

    return diff == 0;
  }

  /**
   * Chuyển mảng byte sang chuỗi hex.
   */
  private static String toHex(byte[] bytes) {

    StringBuilder sb =
        new StringBuilder(bytes.length * 2);

    for (byte b : bytes) {
      sb.append(
          String.format("%02x", b & 0xff));
    }

    return sb.toString();
  }

  /**
   * Chuyển chuỗi hex sang mảng byte.
   */
  private static byte[] fromHex(String hex) {

    int len = hex.length();

    byte[] bytes = new byte[len / 2];

    for (int i = 0; i < len; i += 2) {
      bytes[i / 2] = (byte) Integer.parseInt(
          hex.substring(i, i + 2), 16);
    }

    return bytes;
  }
}
