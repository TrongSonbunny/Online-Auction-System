# Hệ Thống Đấu Giá Online

## Mô tả bài toán

Hệ thống đấu giá trực tuyến cho phép người dùng tạo phiên đấu giá, đặt giá thủ công, và đăng ký tự động đấu giá (auto-bid). Hệ thống hỗ trợ ba vai trò người dùng: **Admin**, **Seller** (người bán), và **Bidder** (người mua). Giao tiếp giữa frontend (CLI) và backend đi qua một lớp `ClientActionHandler` mô phỏng luồng client–server thực tế, sử dụng **Command Pattern**. Dữ liệu được lưu trữ bền vững trong cơ sở dữ liệu SQLite.

**Phạm vi hệ thống:**
- Quản lý phiên đấu giá (tạo, theo dõi, kết thúc, hủy)
- Đặt giá thủ công và tự động (auto-bid cascade)
- Xác thực phân quyền theo vai trò
- Tự động kết thúc phiên đấu giá theo thời gian (Virtual Threads)
- Cơ chế anti-snipe (gia hạn thêm thời gian khi có bid sát giờ kết thúc)
- Thông báo sự kiện qua Observer Pattern

---

## Công nghệ sử dụng

| Thành phần | Chi tiết |
|---|---|
| Ngôn ngữ | Java 21 |
| Build tool | Apache Maven 3.x |
| Giao diện | CLI (Console) |
| Cơ sở dữ liệu | SQLite (`sqlite-jdbc 3.45.1.0`) |
| Serialization | Gson 2.10.1 |
| Logging | SLF4J + Logback |
| Test | JUnit Jupiter 5.10.2 |
| Coverage | JaCoCo 0.8.12 |
| Lint | Google Checkstyle |
| Đóng gói | maven-shade-plugin 3.5.2 (fat JAR) |
| Đa luồng | Java Virtual Threads (JDK 21) |

### Yêu cầu cài đặt

- **JDK 21** trở lên (`java -version` để kiểm tra)
- **Maven 3.6+** (`mvn -version` để kiểm tra)
- Không cần cài SQLite riêng — driver đã được đóng gói trong JAR

---

## Cấu trúc thư mục

```
Online-Auction-System/
├── src/
│   ├── main/java/com/auction/
│   │   ├── Main.java                        # Entry point — CLI tương tác
│   │   ├── models/                          # Domain models
│   │   │   ├── auction/   (Auction, AuctionRules, AuctionStatus)
│   │   │   ├── bid/       (BidTransaction, AutoBid, Transaction)
│   │   │   ├── item/      (AuctionItem, ItemCategory, ItemFactory)
│   │   │   ├── payment/   (PaymentStrategy, BankPayment, MomoPayment, VnPayPayment)
│   │   │   └── user/      (User, Admin, Seller, Bidder, UserRole, UserFactory, permission/)
│   │   ├── backend/                         # Business logic
│   │   │   ├── auction/   (AuctionService, AuctionManager, AuctionScheduler, AuctionValidator)
│   │   │   ├── auth/      (AuthService)
│   │   │   ├── bid/       (BidService, BidValidator, AutoBidService, AutoBidManager, BidHistoryManager)
│   │   │   ├── database/  (DatabaseManager, DatabaseConnection, DataManager, dao/)
│   │   │   ├── observer/  (AuctionEventPublisher, observers/)
│   │   │   ├── payment/   (PaymentProcessor, PaymentValidator, PaymentLogger)
│   │   │   └── util/      (IdGenerator, PasswordHasher)
│   │   ├── network/                         # Lớp giao tiếp client–server
│   │   │   ├── ClientActionHandler.java
│   │   │   ├── ClientMessage.java
│   │   │   ├── ActionType.java
│   │   │   └── command/   (LoginCommand, BidCommand, CreateAuctionCommand, ...)
│   │   └── exceptions/                      # Custom exceptions
│   └── test/                                # Unit tests & concurrency tests
├── target/
│   └── online-auction-system-1.0-SNAPSHOT.jar   # ← Executable fat JAR
├── docs/
├── pom.xml
└── README.md
```

### Các module chính

| Module | Trách nhiệm |
|---|---|
| `models/` | Định nghĩa entity thuần — không có logic nghiệp vụ |
| `backend/auction/` | Tạo, kết thúc, hủy phiên đấu giá; lên lịch tự động |
| `backend/bid/` | Xử lý bid thủ công, auto-bid cascade |
| `backend/auth/` | Đăng ký, đăng nhập, xác thực mật khẩu |
| `backend/database/` | DAO layer, kết nối SQLite, khởi tạo schema |
| `backend/observer/` | Phát và nhận sự kiện đấu giá (Observer Pattern) |
| `backend/payment/` | Xử lý và hoàn tiền qua Strategy Pattern |
| `network/command/` | Command Pattern cho từng action của client |

---

## Vị trí file JAR

Sau khi build, file fat JAR nằm tại:

```
target/online-auction-system-1.0-SNAPSHOT.jar
```

File này bao gồm toàn bộ dependencies (SQLite, Gson, Logback, ...) và có thể chạy độc lập mà không cần cài thêm thư viện.

---

## Hướng dẫn chạy

### Cách 1 — Chạy trực tiếp từ fat JAR (khuyến nghị)

**Bước 1:** Build fat JAR

```bash
mvn package -DskipTests
```

**Bước 2:** Chạy ứng dụng

```bash
java -jar target/online-auction-system-1.0-SNAPSHOT.jar
```

> Nếu gặp cảnh báo về native access của SQLite, thêm flag sau để tắt warning:
> ```bash
> java --enable-native-access=ALL-UNNAMED -jar target/online-auction-system-1.0-SNAPSHOT.jar
> ```

---

### Cách 2 — Chạy qua Maven (không cần build JAR trước)

```bash
mvn compile exec:java
```

---

### Thứ tự thao tác demo

Ứng dụng là CLI đơn tiến trình (không tách Server/Client riêng). Thứ tự thao tác gợi ý:

| Bước | Thao tác |
|---|---|
| 1 | Đăng ký tài khoản **SELLER** → Đăng nhập → Tạo auction mới |
| 2 | Đăng xuất → Đăng ký tài khoản **BIDDER** → Đăng nhập → Đặt giá thủ công |
| 3 | (Tùy chọn) Đăng ký thêm BIDDER thứ hai để thử **auto-bid cascade** |
| 4 | Đăng nhập lại SELLER hoặc ADMIN → Kết thúc auction thủ công |

> Mỗi lần khởi động, database `auction_system.db` sẽ được **reset hoàn toàn** — đây là thiết kế có chủ ý để mỗi lần chạy bắt đầu từ trạng thái sạch.

---

### Các lệnh hữu ích khác

```bash
# Chạy toàn bộ unit tests
mvn test

# Build JAR kèm chạy tests
mvn package

# Kiểm tra Google Java Style
mvn checkstyle:check
```

---

## Danh sách chức năng đã hoàn thành

### Xác thực & Phân quyền
- [x] Đăng ký tài khoản với 3 vai trò: `ADMIN`, `SELLER`, `BIDDER`
- [x] Đăng nhập bằng email + mật khẩu (mật khẩu được hash trước khi lưu)
- [x] Phân quyền theo vai trò qua Strategy Pattern (`PermissionStrategy`)

### Quản lý Phiên đấu giá
- [x] Tạo auction mới (Seller) với thông tin sản phẩm, giá khởi điểm, thời gian
- [x] Xem danh sách toàn bộ auction và trạng thái hiện tại
- [x] Kết thúc auction thủ công (Seller: chỉ auction của mình; Admin: tất cả)
- [x] Hủy auction (Seller: chỉ auction của mình; Admin: tất cả)
- [x] Tự động kết thúc auction theo thời gian (Java Virtual Threads + `ScheduledExecutorService`)
- [x] Cơ chế **anti-snipe**: tự động gia hạn auction khi có bid trong khoảng thời gian cuối

### Đặt Giá
- [x] Đặt giá thủ công (Manual Bid) — phải lớn hơn giá hiện tại
- [x] Đăng ký **auto-bid** với giá tối đa và bước giá tùy chỉnh
- [x] Hủy auto-bid đã đăng ký trong phiên
- [x] **Auto-bid cascade**: khi bị vượt giá, hệ thống tự động phản ứng dây chuyền giữa nhiều auto-bid

### Lưu trữ dữ liệu
- [x] Lưu/đọc `User`, `AuctionItem`, `Auction`, `BidTransaction` từ SQLite qua DAO layer
- [x] Khởi tạo schema tự động khi chạy lần đầu (`DatabaseManager`)

### Hệ thống sự kiện
- [x] Observer Pattern: phát sự kiện khi bid, kết thúc, hủy, gia hạn auction
- [x] Các observer: `AdminObserver`, `SellerObserver`, `BidderObserver`, `DataPersistenceObserver`, `FrontendNotificationObserver`

### Thanh toán (mô phỏng)
- [x] Strategy Pattern cho 3 phương thức: **BankPayment**, **MomoPayment**, **VnPayPayment**
- [x] Xử lý thanh toán và hoàn tiền qua `PaymentProcessor`
- [x] Ghi log giao dịch qua `PaymentLogger`

### Danh mục sản phẩm
- [x] Hỗ trợ 6 danh mục: `ART`, `BOOK`, `ELECTRONICS`, `FASHION`, `FOOD`, `COLLECTIBLE`

### Kiểm thử & Chất lượng
- [x] Unit tests cho tất cả service, model, validator, observer, payment
- [x] Concurrency test (`ConcurrentBiddingTest`) kiểm thử đặt giá đồng thời
- [x] Code coverage đo bằng JaCoCo
- [x] Tuân thủ Google Java Style Guide qua Checkstyle

---

## Báo cáo & Demo

| Tài liệu | Link |
|---|---|
| Báo cáo PDF | *(sẽ cập nhật)* |
| Video demo | *(sẽ cập nhật)* |
