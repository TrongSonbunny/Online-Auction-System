package com.auction.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auction.backend.database.dao.UserDao;
import com.auction.backend.util.PasswordHasher;
import com.auction.exceptions.AuctionException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.user.User;
import com.auction.models.user.UserFactory;
import com.auction.models.user.UserRole;
import com.auction.network.ClientMessage;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test cho AuthService — register và login.
 *
 * <p>Dùng FakeUserDao (in-memory) thay cho SQLite thật.
 * EP: role null/ADMIN; email null/invalid/duplicate; password blank.
 * BVA: username "ADMIN" → ánh xạ admin@auction.local.
 */
@DisplayName("AuthService Tests")
class AuthServiceTest {

  static class FakeUserDao extends UserDao {

    private final Map<String, User> byEmail = new HashMap<>();
    private final Map<String, String> passwords = new HashMap<>();

    @Override
    public void saveUser(User user, String password) {
      byEmail.put(user.getEmail(), user);
      passwords.put(user.getEmail(), PasswordHasher.hash(password));
    }

    @Override
    public boolean existsByEmail(String email) {
      return byEmail.containsKey(email);
    }

    @Override
    public User findByEmail(String email) {
      return byEmail.get(email);
    }

    @Override
    public boolean checkPassword(String email, String password) {
      final String stored = passwords.get(email);
      return stored != null && PasswordHasher.verify(password, stored);
    }
  }

  private AuthService authService;
  private FakeUserDao fakeDao;

  @BeforeEach
  void setUp() {
    fakeDao = new FakeUserDao();
    authService = new AuthService(fakeDao);
  }

  @Test
  @DisplayName("Constructor: null UserDao ném AuctionException")
  void constructor_nullDao_throws() {
    assertThrows(AuctionException.class, () -> new AuthService(null));
  }

  @Nested
  @DisplayName("register - Thành công")
  class RegisterSuccess {

    @Test
    @DisplayName("SELLER hợp lệ → trả User đúng email")
    void register_validSeller_returnsUser() {
      final ClientMessage msg = ClientMessage.builder()
          .role(UserRole.SELLER).name("Nguyen A")
          .email("a@test.com").password("pass123").build();
      final User user = authService.register(msg);
      assertNotNull(user);
      assertEquals("a@test.com", user.getEmail());
    }

    @Test
    @DisplayName("BIDDER hợp lệ → trả User không null")
    void register_validBidder_returnsUser() {
      final ClientMessage msg = ClientMessage.builder()
          .role(UserRole.BIDDER).name("Tran B")
          .email("b@test.com").password("pass456").build();
      assertNotNull(authService.register(msg));
    }
  }

  @Nested
  @DisplayName("register - Dữ liệu không hợp lệ")
  class RegisterValidation {

    @Test
    @DisplayName("message null → AuctionException")
    void register_nullMessage_throws() {
      assertThrows(AuctionException.class, () -> authService.register(null));
    }

    @Test
    @DisplayName("role null → AuctionException")
    void register_nullRole_throws() {
      final ClientMessage msg = ClientMessage.builder()
          .name("A").email("a@b.com").password("pass").build();
      assertThrows(AuctionException.class, () -> authService.register(msg));
    }

    @Test
    @DisplayName("name blank → AuctionException")
    void register_blankName_throws() {
      final ClientMessage msg = ClientMessage.builder()
          .role(UserRole.SELLER).name("  ")
          .email("a@b.com").password("pass").build();
      assertThrows(AuctionException.class, () -> authService.register(msg));
    }

    @Test
    @DisplayName("email null → AuctionException")
    void register_nullEmail_throws() {
      final ClientMessage msg = ClientMessage.builder()
          .role(UserRole.SELLER).name("A").password("pass").build();
      assertThrows(AuctionException.class, () -> authService.register(msg));
    }

    @Test
    @DisplayName("email không có @ → AuctionException")
    void register_invalidEmail_throws() {
      final ClientMessage msg = ClientMessage.builder()
          .role(UserRole.SELLER).name("A")
          .email("notvalid").password("pass").build();
      assertThrows(AuctionException.class, () -> authService.register(msg));
    }

    @Test
    @DisplayName("password blank → AuctionException")
    void register_blankPassword_throws() {
      final ClientMessage msg = ClientMessage.builder()
          .role(UserRole.SELLER).name("A")
          .email("a@b.com").password("").build();
      assertThrows(AuctionException.class, () -> authService.register(msg));
    }

    @Test
    @DisplayName("email đã tồn tại → AuctionException")
    void register_duplicateEmail_throws() {
      final ClientMessage msg = ClientMessage.builder()
          .role(UserRole.SELLER).name("A")
          .email("dup@test.com").password("pass").build();
      authService.register(msg);
      assertThrows(AuctionException.class, () -> authService.register(msg));
    }

    @Test
    @DisplayName("role ADMIN → UnauthorizedException")
    void register_adminRole_throwsUnauthorized() {
      final ClientMessage msg = ClientMessage.builder()
          .role(UserRole.ADMIN).name("Admin")
          .email("a@b.com").password("pass").build();
      assertThrows(UnauthorizedException.class, () -> authService.register(msg));
    }
  }

  @Nested
  @DisplayName("login - Thành công")
  class LoginSuccess {

    @Test
    @DisplayName("email + password đúng → trả User")
    void login_validCredentials_returnsUser() {
      final ClientMessage reg = ClientMessage.builder()
          .role(UserRole.SELLER).name("Seller")
          .email("s@test.com").password("mypass").build();
      authService.register(reg);
      final ClientMessage login = ClientMessage.builder()
          .email("s@test.com").password("mypass").build();
      assertNotNull(authService.login(login));
    }

    @Test
    @DisplayName("username ADMIN → ánh xạ admin@auction.local")
    void login_adminUsername_mapsToAdminEmail() {
      final User admin = UserFactory.createUser(
          UserRole.SELLER, "Admin", "admin@auction.local");
      fakeDao.saveUser(admin, "adminpass");
      final ClientMessage login = ClientMessage.builder()
          .email("ADMIN").password("adminpass").build();
      assertNotNull(authService.login(login));
    }
  }

  @Nested
  @DisplayName("login - Thất bại")
  class LoginFailures {

    @Test
    @DisplayName("tài khoản không tồn tại → UnauthorizedException")
    void login_unknownEmail_throwsUnauthorized() {
      final ClientMessage msg = ClientMessage.builder()
          .email("nobody@test.com").password("pass").build();
      assertThrows(UnauthorizedException.class, () -> authService.login(msg));
    }

    @Test
    @DisplayName("sai mật khẩu → UnauthorizedException")
    void login_wrongPassword_throwsUnauthorized() {
      final ClientMessage reg = ClientMessage.builder()
          .role(UserRole.SELLER).name("A")
          .email("a@test.com").password("correct").build();
      authService.register(reg);
      final ClientMessage login = ClientMessage.builder()
          .email("a@test.com").password("wrong").build();
      assertThrows(UnauthorizedException.class, () -> authService.login(login));
    }

    @Test
    @DisplayName("message null → AuctionException")
    void login_nullMessage_throws() {
      assertThrows(AuctionException.class, () -> authService.login(null));
    }

    @Test
    @DisplayName("email blank → AuctionException")
    void login_blankEmail_throws() {
      final ClientMessage msg = ClientMessage.builder()
          .email("").password("pass").build();
      assertThrows(AuctionException.class, () -> authService.login(msg));
    }

    @Test
    @DisplayName("email không có @ (không phải ADMIN) → AuctionException")
    void login_invalidEmailFormat_throws() {
      final ClientMessage msg = ClientMessage.builder()
          .email("notanemail").password("pass").build();
      assertThrows(AuctionException.class, () -> authService.login(msg));
    }

    @Test
    @DisplayName("password blank → AuctionException")
    void login_blankPassword_throws() {
      final ClientMessage msg = ClientMessage.builder()
          .email("a@b.com").password("").build();
      assertThrows(AuctionException.class, () -> authService.login(msg));
    }
  }
}
