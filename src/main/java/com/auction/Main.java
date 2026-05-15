package com.auction;

import com.auction.backend.bid.BidResult;
import com.auction.backend.database.DatabaseManager;
import com.auction.models.auction.Auction;
import com.auction.models.bid.AutoBid;
import com.auction.models.bid.BidTransaction;
import com.auction.models.item.ItemCategory;
import com.auction.models.user.User;
import com.auction.models.user.UserRole;
import com.auction.network.ActionType;
import com.auction.network.ClientActionHandler;
import com.auction.network.ClientMessage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * CLI tương tác cho hệ thống đấu giá online.
 *
 * <p>Mọi tương tác đều đi qua {@link ClientActionHandler#doAction} — đúng với
 * luồng mà frontend thật sẽ gọi. Hỗ trợ:
 * <ul>
 *   <li>Đăng ký / Đăng nhập với 3 role: ADMIN, SELLER, BIDDER.
 *   <li>Tạo, xem, kết thúc, hủy auction (SELLER / ADMIN).
 *   <li>Đặt giá thủ công, đăng ký / hủy auto-bid (BIDDER).
 * </ul>
 *
 * <p>Chạy: {@code mvn compile exec:java}
 */
public class Main {

  private static final Scanner SCANNER =
      new Scanner(System.in, java.nio.charset.StandardCharsets.UTF_8);

  private static User currentUser = null;

  private static final List<AutoBid> SESSION_AUTO_BIDS =
      new ArrayList<>();

  // ── Entry point ────────────────────────────────────────────────────────────

  /**
   * Khởi động ứng dụng CLI đấu giá.
   *
   * <p>Xóa DB cũ rồi khởi tạo lại để đảm bảo mỗi lần chạy bắt đầu sạch.
   *
   * @param args không dùng
   * @throws Exception nếu thao tác file thất bại
   */
  public static void main(String[] args) throws Exception {

    Files.deleteIfExists(Path.of("auction_system.db"));
    new DatabaseManager().initializeDatabase();

    printBanner();

    while (true) {
      try {
        if (currentUser == null) {
          showGuestMenu();
        } else {
          switch (currentUser.getRole()) {
            case SELLER -> showSellerMenu();
            case BIDDER -> showBidderMenu();
            case ADMIN  -> showAdminMenu();
            default     -> logout();
          }
        }
      } catch (NoSuchElementException e) {
        // stdin đóng (EOF) — thoát sạch
        exitApp();
      } catch (Exception e) {
        printError(e.getMessage());
      }
    }
  }

  // ── Menus ──────────────────────────────────────────────────────────────────

  /**
   * Menu chính khi chưa đăng nhập.
   */
  private static void showGuestMenu() {

    printHeader("HỆ THỐNG ĐẤU GIÁ ONLINE");
    System.out.println("  1. Đăng ký tài khoản");
    System.out.println("  2. Đăng nhập");
    System.out.println("  0. Thoát");
    printLine();

    switch (readInt("Lựa chọn")) {
      case 1  -> doRegister();
      case 2  -> doLogin();
      case 0  -> exitApp();
      default -> System.out.println("  Lựa chọn không hợp lệ.");
    }
  }

  /**
   * Menu dành cho SELLER.
   */
  private static void showSellerMenu() {

    printHeader("SELLER — " + currentUser.getName());
    System.out.println("  1. Xem danh sách auction");
    System.out.println("  2. Tạo auction mới");
    System.out.println("  3. Kết thúc auction của tôi");
    System.out.println("  4. Hủy auction của tôi");
    System.out.println("  0. Đăng xuất");
    printLine();

    switch (readInt("Lựa chọn")) {
      case 1  -> doViewAuctions();
      case 2  -> doCreateAuction();
      case 3  -> doFinishAuction();
      case 4  -> doCancelAuction();
      case 0  -> logout();
      default -> System.out.println("  Lựa chọn không hợp lệ.");
    }
  }

  /**
   * Menu dành cho BIDDER.
   */
  private static void showBidderMenu() {

    printHeader("BIDDER — " + currentUser.getName());
    System.out.println("  1. Xem danh sách auction");
    System.out.println("  2. Đặt giá thủ công (Manual Bid)");
    System.out.println("  3. Đăng ký auto-bid");
    System.out.println("  4. Hủy auto-bid của tôi");
    System.out.println("  0. Đăng xuất");
    printLine();

    switch (readInt("Lựa chọn")) {
      case 1  -> doViewAuctions();
      case 2  -> doBid();
      case 3  -> doRegisterAutoBid();
      case 4  -> doCancelAutoBid();
      case 0  -> logout();
      default -> System.out.println("  Lựa chọn không hợp lệ.");
    }
  }

  /**
   * Menu dành cho ADMIN.
   */
  private static void showAdminMenu() {

    printHeader("ADMIN — " + currentUser.getName());
    System.out.println("  1. Xem danh sách auction");
    System.out.println("  2. Kết thúc auction bất kỳ");
    System.out.println("  3. Hủy auction bất kỳ");
    System.out.println("  0. Đăng xuất");
    printLine();

    switch (readInt("Lựa chọn")) {
      case 1  -> doViewAuctions();
      case 2  -> doFinishAuction();
      case 3  -> doCancelAuction();
      case 0  -> logout();
      default -> System.out.println("  Lựa chọn không hợp lệ.");
    }
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  /**
   * Đăng ký tài khoản mới.
   */
  private static void doRegister() {

    System.out.println("\n  === ĐĂNG KÝ TÀI KHOẢN ===");
    System.out.println("  Chọn role:");
    System.out.println("    1. SELLER (người bán)");
    System.out.println("    2. BIDDER (người mua)");
    System.out.println("    3. ADMIN  (quản trị viên)");

    UserRole role;
    switch (readInt("Role")) {
      case 1  -> role = UserRole.SELLER;
      case 2  -> role = UserRole.BIDDER;
      case 3  -> role = UserRole.ADMIN;
      default -> {
        System.out.println("  Role không hợp lệ.");
        return;
      }
    }

    String name     = readLine("Họ tên");
    String email    = readLine("Email");
    String password = readLine("Mật khẩu");

    User user = (User) call(ClientMessage.builder()
        .action(ActionType.REGISTER)
        .role(role)
        .name(name)
        .email(email)
        .password(password)
        .build());

    System.out.println();
    System.out.println("  Đăng ký thành công!");
    System.out.printf("  Tên    : %s%n", user.getName());
    System.out.printf("  Role   : %s%n", user.getRole());
    System.out.printf("  User ID: %s%n", user.getUserId());
  }

  /**
   * Đăng nhập bằng email và mật khẩu.
   */
  private static void doLogin() {

    System.out.println("\n  === ĐĂNG NHẬP ===");
    String email    = readLine("Email");
    String password = readLine("Mật khẩu");

    User user = (User) call(ClientMessage.builder()
        .action(ActionType.LOGIN)
        .email(email)
        .password(password)
        .build());

    currentUser = user;
    SESSION_AUTO_BIDS.clear();

    System.out.println();
    System.out.printf("  Chào mừng, %s! (Role: %s)%n",
        user.getName(), user.getRole());
  }

  /**
   * Tạo auction mới (SELLER).
   */
  private static void doCreateAuction() {

    System.out.println("\n  === TẠO AUCTION MỚI ===");

    String itemName = readLine("Tên sản phẩm");
    String desc     = readLine("Mô tả sản phẩm");

    String category = pickCategory();
    if (category == null) {
      return;
    }

    String condition      = readLine("Tình trạng (vd: Mới 100%, Đã qua sử dụng)");
    double estimatedPrice = readDouble("Giá ước tính (VND)");
    double startingPrice  = readDouble("Giá khởi điểm (VND)");
    long   duration       = readLong("Thời gian đấu giá (giây, vd: 3600 = 1 giờ)");

    Auction auction = (Auction) call(ClientMessage.builder()
        .action(ActionType.CREATE_AUCTION)
        .userId(currentUser.getUserId())
        .itemName(itemName)
        .itemDescription(desc)
        .itemCategory(category)
        .itemCondition(condition)
        .estimatedPrice(estimatedPrice)
        .startingPrice(startingPrice)
        .durationSeconds(duration)
        .build());

    System.out.println("\n  Tạo auction thành công!");
    printAuction(auction);
  }

  /**
   * Hiển thị toàn bộ auction đang có trong hệ thống.
   */
  private static void doViewAuctions() {

    System.out.println("\n  === DANH SÁCH AUCTION ===");

    @SuppressWarnings("unchecked")
    Collection<Auction> auctions =
        (Collection<Auction>) call(ClientMessage.builder()
            .action(ActionType.GET_ALL_AUCTIONS)
            .build());

    if (auctions.isEmpty()) {
      System.out.println("  Chưa có auction nào trong hệ thống.");
      return;
    }

    int index = 1;
    for (Auction a : auctions) {
      System.out.printf("%n  [%d] %s%n", index++, a.getItem().getName());
      System.out.printf("      ID         : %s%n", a.getAuctionId());
      System.out.printf("      Trạng thái : %s%n", a.getStatus());
      System.out.printf("      Giá KĐ     : %s%n", formatPrice(a.getStartingPrice()));
      System.out.printf("      Giá cao nhất: %s%n", formatPrice(a.getCurrentHighestBid()));
      if (a.getCurrentHighestBidder() != null) {
        System.out.printf("      Dẫn đầu    : %s%n",
            a.getCurrentHighestBidder().getName());
      }
      if (a.getScheduledEndTime() != null) {
        System.out.printf("      Kết thúc lúc: %s%n", a.getScheduledEndTime());
      }
    }
  }

  /**
   * Đặt giá thủ công cho một auction đang ACTIVE (BIDDER).
   */
  private static void doBid() {

    System.out.println("\n  === ĐẶT GIÁ THỦ CÔNG ===");

    Auction auction = selectAuction();
    if (auction == null) {
      return;
    }

    System.out.printf("%n  Auction    : %s%n", auction.getItem().getName());
    System.out.printf("  Giá hiện tại: %s%n",
        formatPrice(auction.getCurrentHighestBid()));
    if (auction.getCurrentHighestBidder() != null) {
      System.out.printf("  Dẫn đầu    : %s%n",
          auction.getCurrentHighestBidder().getName());
    }

    double amount = readDouble("\nSố tiền bid (VND)");

    BidResult result = (BidResult) call(ClientMessage.builder()
        .action(ActionType.BID)
        .userId(currentUser.getUserId())
        .auctionId(auction.getAuctionId())
        .bidAmount(amount)
        .build());

    System.out.printf("%n  Đặt giá thành công: %s%n",
        formatPrice(result.getManualTransaction().getBidAmount()));

    List<BidTransaction> cascades = result.getAutoBidTransactions();
    if (!cascades.isEmpty()) {
      System.out.println("  Auto-bid cascade phát sinh:");
      for (BidTransaction tx : cascades) {
        System.out.printf("    → %s tự bid: %s%n",
            tx.getBidder().getName(), formatPrice(tx.getBidAmount()));
      }
    }

    System.out.printf("  Giá cao nhất hiện tại: %s%n",
        formatPrice(auction.getCurrentHighestBid()));
    if (auction.getCurrentHighestBidder() != null) {
      System.out.printf("  Người dẫn đầu        : %s%n",
          auction.getCurrentHighestBidder().getName());
    }
  }

  /**
   * Đăng ký auto-bid cho một auction đang ACTIVE (BIDDER).
   */
  private static void doRegisterAutoBid() {

    System.out.println("\n  === ĐĂNG KÝ AUTO-BID ===");

    Auction auction = selectAuction();
    if (auction == null) {
      return;
    }

    System.out.printf("%n  Auction    : %s%n", auction.getItem().getName());
    System.out.printf("  Giá hiện tại: %s%n",
        formatPrice(auction.getCurrentHighestBid()));

    double maxBid     = readDouble("Giá tối đa bạn chấp nhận (VND)");
    double increment  = readDouble("Bước giá mỗi lần tự động bid (VND)");

    AutoBid autoBid = (AutoBid) call(ClientMessage.builder()
        .action(ActionType.REGISTER_AUTO_BID)
        .userId(currentUser.getUserId())
        .auctionId(auction.getAuctionId())
        .maxBid(maxBid)
        .increment(increment)
        .build());

    SESSION_AUTO_BIDS.add(autoBid);

    System.out.println("\n  Đăng ký auto-bid thành công!");
    System.out.printf("  Max Bid  : %s%n", formatPrice(autoBid.getMaxBid()));
    System.out.printf("  Increment: %s%n", formatPrice(autoBid.getIncrement()));
    System.out.printf("  ID       : %s%n", autoBid.getAutoBidId());
    System.out.println("  (Hệ thống sẽ tự động bid khi có người vượt giá bạn)");
  }

  /**
   * Hủy auto-bid đã đăng ký trong phiên hiện tại (BIDDER).
   */
  private static void doCancelAutoBid() {

    System.out.println("\n  === HỦY AUTO-BID ===");

    if (SESSION_AUTO_BIDS.isEmpty()) {
      System.out.println("  Bạn chưa đăng ký auto-bid nào trong phiên này.");
      return;
    }

    System.out.println("  Auto-bid của bạn:");
    for (int i = 0; i < SESSION_AUTO_BIDS.size(); i++) {
      AutoBid ab = SESSION_AUTO_BIDS.get(i);
      System.out.printf("  [%d] Auction %-15s | Max: %-20s | Bước: %s%n",
          i + 1,
          ab.getAuctionId(),
          formatPrice(ab.getMaxBid()),
          formatPrice(ab.getIncrement()));
    }

    int idx = readInt("Chọn số thứ tự để hủy (0 = quay lại)");
    if (idx == 0) {
      return;
    }
    if (idx < 1 || idx > SESSION_AUTO_BIDS.size()) {
      System.out.println("  Số thứ tự không hợp lệ.");
      return;
    }

    AutoBid selected = SESSION_AUTO_BIDS.get(idx - 1);

    call(ClientMessage.builder()
        .action(ActionType.CANCEL_AUTO_BID)
        .userId(currentUser.getUserId())
        .autoBidId(selected.getAutoBidId())
        .build());

    SESSION_AUTO_BIDS.remove(idx - 1);
    System.out.printf("  Đã hủy auto-bid %s thành công.%n",
        selected.getAutoBidId());
  }

  /**
   * Kết thúc auction thủ công (SELLER hoặc ADMIN).
   */
  private static void doFinishAuction() {

    System.out.println("\n  === KẾT THÚC AUCTION ===");

    Auction auction = selectAuction();
    if (auction == null) {
      return;
    }

    System.out.printf("%n  Xác nhận kết thúc auction: %s?%n",
        auction.getItem().getName());
    String confirm = readLine("Nhập 'yes' để xác nhận");
    if (!"yes".equalsIgnoreCase(confirm)) {
      System.out.println("  Đã hủy thao tác.");
      return;
    }

    Auction finished = (Auction) call(ClientMessage.builder()
        .action(ActionType.FINISH_AUCTION)
        .userId(currentUser.getUserId())
        .auctionId(auction.getAuctionId())
        .build());

    System.out.println("\n  Auction đã kết thúc!");
    printAuction(finished);
  }

  /**
   * Hủy auction (SELLER hoặc ADMIN).
   */
  private static void doCancelAuction() {

    System.out.println("\n  === HỦY AUCTION ===");

    Auction auction = selectAuction();
    if (auction == null) {
      return;
    }

    System.out.printf("%n  Xác nhận hủy auction: %s?%n",
        auction.getItem().getName());
    String confirm = readLine("Nhập 'yes' để xác nhận");
    if (!"yes".equalsIgnoreCase(confirm)) {
      System.out.println("  Đã hủy thao tác.");
      return;
    }

    Auction cancelled = (Auction) call(ClientMessage.builder()
        .action(ActionType.CANCEL_AUCTION)
        .userId(currentUser.getUserId())
        .auctionId(auction.getAuctionId())
        .build());

    System.out.println("\n  Auction đã bị hủy!");
    printAuction(cancelled);
  }

  /**
   * Đăng xuất người dùng hiện tại.
   */
  private static void logout() {

    System.out.printf("%n  Đã đăng xuất: %s%n", currentUser.getName());
    currentUser = null;
    SESSION_AUTO_BIDS.clear();
  }

  /**
   * Thoát ứng dụng.
   */
  private static void exitApp() {

    System.out.println("\n  Tạm biệt!");
    System.exit(0);
  }

  // ── Shared helpers ─────────────────────────────────────────────────────────

  /**
   * Hiển thị danh sách auction và yêu cầu người dùng chọn một.
   *
   * @return auction được chọn, hoặc null nếu người dùng quay lại
   */
  private static Auction selectAuction() {

    @SuppressWarnings("unchecked")
    Collection<Auction> col =
        (Collection<Auction>) call(ClientMessage.builder()
            .action(ActionType.GET_ALL_AUCTIONS)
            .build());

    List<Auction> list = new ArrayList<>(col);

    if (list.isEmpty()) {
      System.out.println("  Chưa có auction nào trong hệ thống.");
      return null;
    }

    System.out.println();
    for (int i = 0; i < list.size(); i++) {
      Auction a = list.get(i);
      System.out.printf("  [%d] [%-10s] %-38s Giá: %s%n",
          i + 1,
          a.getStatus(),
          a.getItem().getName(),
          formatPrice(a.getCurrentHighestBid()));
    }

    int idx = readInt(
        "\nChọn auction (1-" + list.size() + ", 0 = quay lại)");

    if (idx == 0) {
      return null;
    }
    if (idx < 1 || idx > list.size()) {
      System.out.println("  Số thứ tự không hợp lệ.");
      return null;
    }
    return list.get(idx - 1);
  }

  /**
   * Hiển thị danh sách ItemCategory và yêu cầu người dùng chọn.
   *
   * @return tên category (String), hoặc null nếu chọn không hợp lệ
   */
  private static String pickCategory() {

    ItemCategory[] cats = ItemCategory.values();
    System.out.println("  Chọn danh mục sản phẩm:");
    for (int i = 0; i < cats.length; i++) {
      System.out.printf("    %d. %s%n", i + 1, cats[i].name());
    }

    int idx = readInt("Danh mục");
    if (idx < 1 || idx > cats.length) {
      System.out.println("  Danh mục không hợp lệ.");
      return null;
    }
    return cats[idx - 1].name();
  }

  /**
   * Gọi ClientActionHandler và trả về kết quả.
   *
   * @param msg message cần gửi
   * @return kết quả từ command
   */
  private static Object call(ClientMessage msg) {
    return ClientActionHandler.doAction(msg);
  }

  /**
   * In thông tin chi tiết của một auction.
   *
   * @param a auction cần in
   */
  private static void printAuction(Auction a) {

    System.out.println();
    System.out.printf("  ┌─ %s%n", a.getAuctionId());
    System.out.printf("  │  Sản phẩm    : %s%n", a.getItem().getName());
    System.out.printf("  │  Mô tả       : %s%n", a.getItem().getDescription());
    System.out.printf("  │  Danh mục    : %s%n", a.getItem().getCategory());
    System.out.printf("  │  Tình trạng  : %s%n", a.getItem().getItemCondition());
    System.out.printf("  │  Giá KĐ      : %s%n", formatPrice(a.getStartingPrice()));
    System.out.printf("  │  Giá cao nhất: %s%n", formatPrice(a.getCurrentHighestBid()));
    System.out.printf("  │  Trạng thái  : %s%n", a.getStatus());

    if (a.getCurrentHighestBidder() != null) {
      System.out.printf("  │  Người dẫn đầu: %s%n",
          a.getCurrentHighestBidder().getName());
    }
    if (a.getScheduledEndTime() != null) {
      System.out.printf("  │  Kết thúc dự kiến: %s%n",
          a.getScheduledEndTime());
    }
    if (a.getEndTime() != null) {
      System.out.printf("  │  Kết thúc lúc    : %s%n",
          a.getEndTime());
    }
    System.out.println(
        "  └──────────────────────────────────────────────────");
  }

  /**
   * Định dạng giá tiền: {@code 30000000 → "30,000,000 VND"}.
   *
   * @param price giá cần định dạng
   * @return chuỗi đã định dạng
   */
  private static String formatPrice(double price) {
    return String.format("%,.0f VND", price);
  }

  /**
   * In banner khởi động.
   */
  private static void printBanner() {

    System.out.println();
    System.out.println("  ╔══════════════════════════════════════════════╗");
    System.out.println("  ║    HỆ THỐNG ĐẤU GIÁ ONLINE — CLI Demo       ║");
    System.out.println("  ╚══════════════════════════════════════════════╝");
    System.out.println("  Database: auction_system.db (SQLite, fresh start)");
    System.out.println();
  }

  /**
   * In tiêu đề section với tên người dùng / role.
   *
   * @param title nội dung tiêu đề
   */
  private static void printHeader(String title) {

    System.out.println();
    System.out.println(
        "  ┌──────────────────────────────────────────────────┐");
    System.out.printf(
        "  │  %-48s│%n", title);
    System.out.println(
        "  └──────────────────────────────────────────────────┘");
  }

  /**
   * In đường kẻ ngang ngăn cách.
   */
  private static void printLine() {
    System.out.println(
        "  ──────────────────────────────────────────────────────");
  }

  /**
   * In thông báo lỗi ra console.
   *
   * @param msg nội dung lỗi
   */
  private static void printError(String msg) {
    System.out.println(
        "\n  [LỖI] " + (msg != null ? msg : "Lỗi không xác định."));
  }

  /**
   * Đọc một dòng text từ stdin.
   *
   * @param prompt nhãn hiển thị trước dấu ":"
   * @return chuỗi người dùng nhập (đã trim)
   */
  private static String readLine(String prompt) {
    System.out.print("  " + prompt + ": ");
    return SCANNER.nextLine().trim();
  }

  /**
   * Đọc một số nguyên từ stdin; trả về -1 nếu không parse được.
   *
   * @param prompt nhãn hiển thị
   * @return số nguyên, hoặc -1 nếu không hợp lệ
   */
  private static int readInt(String prompt) {
    System.out.print("  " + prompt + ": ");
    String line = SCANNER.nextLine().trim();
    try {
      return Integer.parseInt(line);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /**
   * Đọc một số thực từ stdin; lặp lại nếu không parse được.
   *
   * @param prompt nhãn hiển thị
   * @return số thực hợp lệ
   */
  private static double readDouble(String prompt) {
    while (true) {
      System.out.print("  " + prompt + ": ");
      String line = SCANNER.nextLine().trim().replace(",", "");
      try {
        return Double.parseDouble(line);
      } catch (NumberFormatException e) {
        System.out.println("  Vui lòng nhập số hợp lệ (vd: 5000000).");
      }
    }
  }

  /**
   * Đọc một số nguyên dài từ stdin; lặp lại nếu không parse được.
   *
   * @param prompt nhãn hiển thị
   * @return số nguyên dài hợp lệ
   */
  private static long readLong(String prompt) {
    while (true) {
      System.out.print("  " + prompt + ": ");
      String line = SCANNER.nextLine().trim().replace(",", "");
      try {
        return Long.parseLong(line);
      } catch (NumberFormatException e) {
        System.out.println("  Vui lòng nhập số nguyên hợp lệ (vd: 3600).");
      }
    }
  }
}
