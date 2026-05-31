# Hệ Thống Đấu Giá Online

## Mô tả bài toán

Hệ thống đấu giá trực tuyến cho phép người dùng tạo phiên đấu giá, đặt giá thủ công, và đăng ký tự động đấu giá (auto-bid). Hệ thống gồm hai thành phần chạy độc lập: **Server** (backend TCP) và **Client** (giao diện JavaFX). Ba vai trò người dùng: **Admin**, **Seller** (người bán), **Bidder** (người mua).

**Phạm vi hệ thống:**
- Quản lý phiên đấu giá theo trạng thái: `PENDING → ACTIVE → FINISHED / CANCELLED`
- Đặt giá thủ công và tự động (auto-bid cascade)
- Xác thực và phân quyền theo vai trò
- Tự động kết thúc auction theo thời gian (Java Virtual Threads)
- Cơ chế anti-snipe (gia hạn khi có bid sát giờ kết thúc)
- Thông báo real-time qua Observer Pattern + TCP push

---

## Công nghệ sử dụng

| Thành phần | Chi tiết |
|---|---|
| Ngôn ngữ | Java 21 |
| Build tool | Apache Maven 3.x |
| Giao diện | JavaFX 21 + FXML |
| Giao tiếp | TCP Socket (JSON qua Gson) |
| Cơ sở dữ liệu | SQLite (`sqlite-jdbc 3.46.1.0`) |
| Serialization | Gson 2.10.1 |
| Logging | SLF4J + Logback |
| Test | JUnit Jupiter 5.10.2 + JaCoCo |
| Lint | Google Checkstyle |
| Đóng gói | maven-shade-plugin 3.5.2 (fat JAR) |
| Đa luồng | Java Virtual Threads (JDK 21) |

### Yêu cầu cài đặt

- **JDK 21** trở lên (`java -version`)
- **Maven 3.6+** — chỉ cần khi build từ source (`mvn -version`)
- Không cần cài SQLite hay JavaFX riêng — đã đóng gói trong JAR

---

## Cấu trúc thư mục

```
Online-Auction-System/
├── src/main/java/com/auction/
│   ├── App.java                          # JavaFX Application (Client)
│   ├── Launcher.java                     # Entry point fat JAR Client
│   ├── Main.java                         # CLI demo (tùy chọn)
│   ├── controllers/                      # JavaFX Controllers (FXML)
│   │   ├── LoginController.java
│   │   ├── PrimaryController.java
│   │   ├── AdminController.java
│   │   ├── SellerController.java
│   │   └── LiveBiddingController.java
│   ├── models/                           # Domain models
│   │   ├── auction/  (Auction, AuctionRules, AuctionStatus)
│   │   ├── bid/      (BidTransaction, AutoBid)
│   │   ├── item/     (AuctionItem, ItemCategory, ItemFactory)
│   │   ├── payment/  (PaymentStrategy, BankPayment, MomoPayment, VnPayPayment)
│   │   └── user/     (User, Admin, Seller, Bidder, UserRole, UserFactory, permission/)
│   ├── backend/                          # Business logic (chạy trên Server)
│   │   ├── auction/  (AuctionService, AuctionManager, AuctionScheduler, AuctionValidator)
│   │   ├── auth/     (AuthService)
│   │   ├── bid/      (BidService, BidValidator, AutoBidService, AutoBidManager, BidHistoryManager)
│   │   ├── database/ (DatabaseManager, DatabaseConnection, DataManager, dao/)
│   │   ├── observer/ (AuctionEventPublisher, observers/)
│   │   ├── payment/  (PaymentProcessor, PaymentValidator, PaymentLogger)
│   │   └── util/     (IdGenerator, PasswordHasher)
│   ├── network/                          # Lớp mạng
│   │   ├── ServerMain.java               # TCP Server entry point
│   │   ├── ClientHandler.java            # Xử lý mỗi kết nối client
│   │   ├── NetworkClient.java            # TCP Client (dùng bởi JavaFX)
│   │   ├── ClientActionHandler.java      # Command dispatcher
│   │   ├── ClientMessage.java / ServerMessage.java
│   │   └── command/  (LoginCommand, BidCommand, CreateAuctionCommand, ...)
│   ├── exceptions/                       # Custom exceptions
│   └── utils/                            # Tiện ích UI
├── src/main/resources/com/auction/views/ # FXML + CSS + assets
├── src/test/                             # Unit tests & concurrency tests
├── target/
│   ├── server.jar                        # ← Server fat JAR (20 MB)
│   └── client.jar                        # ← Client fat JAR (28 MB, bao gồm JavaFX)
└── pom.xml
```

### Các module chính

| Module | Chạy ở | Trách nhiệm |
|---|---|---|
| `network/ServerMain` | Server | TCP server port 8080, nhận kết nối |
| `network/ClientHandler` | Server | Xử lý từng client, dispatch command |
| `backend/auction/` | Server | Tạo, kết thúc, hủy, lên lịch auction |
| `backend/bid/` | Server | Bid thủ công, auto-bid cascade |
| `backend/auth/` | Server | Đăng ký, đăng nhập |
| `backend/database/` | Server | DAO layer, SQLite |
| `App.java` + `controllers/` | Client | Giao diện JavaFX |
| `network/NetworkClient` | Client | Kết nối TCP tới server |

---

## Vị trí file JAR

Sau khi build (`mvn package -DskipTests`), hai file JAR nằm tại:

| File | Vị trí | Kích thước |
|---|---|---|
| **Server** | `target/server.jar` | ~20 MB |
| **Client** | `target/client.jar` | ~28 MB (có JavaFX) |

---

## Hướng dẫn chạy

> **Thứ tự bắt buộc: chạy Server trước, rồi mới chạy Client.**

### Bước 1 — vào github

vào github, vào mục release,tải 2 file server.jar và client.jar xuống

### Bước 2 — Chạy Server

Mở **terminal 1**, chạy:

```bash
java -jar server.jar
```

Kết quả khi server khởi động thành công:

```
[main] INFO  ServerMain - Đang khởi động Máy chủ Đấu giá trên cổng 8080...
[main] INFO  ServerMain - Khởi tạo cơ sở dữ liệu thành công.
[main] INFO  ServerMain - Máy chủ đã hoạt động và đang chờ kết nối từ máy khách...
```

### Bước 3 — Chạy Client (JavaFX)

Mở **terminal 2**, chạy:

```bash
java -jar client.jar
```

Client sẽ kết nối tới server tại `127.0.0.1:8080` và hiển thị màn hình đăng nhập.

> **Kết nối tới server khác IP:**
> ```bash
> java --enable-native-access=ALL-UNNAMED -Dserver.ip=192.168.1.x -jar target/client.jar
> ```

### Tài khoản mặc định

| Tài khoản | Mật khẩu |
|---|---|
| `ADMIN` (hoặc `admin@auction.local`) | `123456789` |

---

### Các lệnh hữu ích khác

```bash
# Chạy toàn bộ unit tests
mvn test

# Build đầy đủ kèm tests
mvn package

# Kiểm tra Google Java Style
mvn checkstyle:check
```

---

## Danh sách chức năng đã hoàn thành

### Xác thực & Phân quyền
- [x] Đăng ký tài khoản: `SELLER`, `BIDDER` (ADMIN chỉ có 1 mặc định)
- [x] Đăng nhập bằng email + mật khẩu (hash trước khi lưu DB)
- [x] Đăng nhập Admin bằng username `ADMIN` (ánh xạ nội bộ)
- [x] Phân quyền theo vai trò qua Strategy Pattern (`PermissionStrategy`)

### Quản lý Phiên đấu giá
- [x] Tạo auction (PENDING) với thông tin sản phẩm, giá khởi điểm, thời gian
- [x] Cập nhật thông tin auction khi đang PENDING
- [x] Mở auction thủ công (PENDING → ACTIVE)
- [x] Tự động kết thúc auction theo thời gian (Virtual Threads)
- [x] Kết thúc thủ công (Seller: auction của mình; Admin: tất cả)
- [x] Hủy auction (Seller: auction của mình; Admin: tất cả)
- [x] Xem danh sách toàn bộ auction
- [x] Cơ chế **anti-snipe**: tự động gia hạn khi có bid sát giờ kết thúc

### Đặt Giá
- [x] Đặt giá thủ công (Manual Bid)
- [x] Đăng ký **auto-bid** với giá tối đa và bước giá tùy chỉnh
- [x] Hủy auto-bid
- [x] **Auto-bid cascade**: phản ứng dây chuyền giữa nhiều người đăng ký auto-bid
- [x] Xem lịch sử bid (`GET_BID_HISTORY`)

### Real-time & Mạng
- [x] TCP Server port 8080, xử lý nhiều client qua Virtual Threads
- [x] Đồng bộ giá real-time: khi có bid mới, server push `UPDATE_PRICE` tới tất cả client khác
- [x] JavaFX Client kết nối server qua `NetworkClient` (Singleton)
- [x] JSON serialization/deserialization qua Gson

### Lưu trữ dữ liệu
- [x] Lưu/đọc `User`, `AuctionItem`, `Auction`, `BidTransaction` từ SQLite
- [x] Khởi tạo schema và tài khoản Admin mặc định khi start server

### Hệ thống sự kiện
- [x] Observer Pattern: phát event khi bid, tạo, kết thúc, hủy, gia hạn auction
- [x] Các observer: `AdminObserver`, `SellerObserver`, `BidderObserver`, `DataPersistenceObserver`, `FrontendNotificationObserver`

### Thanh toán (mô phỏng)
- [x] Strategy Pattern: **BankPayment**, **MomoPayment**, **VnPayPayment**
- [x] Xử lý thanh toán và hoàn tiền qua `PaymentProcessor`

### Giao diện (JavaFX)
- [x] Màn hình đăng nhập (`login.fxml`)
- [x] Dashboard chính (`primary.fxml`)
- [x] Dashboard Admin (`admin_dashboard.fxml`)
- [x] Giao diện Seller (`seller.fxml`)
- [x] Màn hình đấu giá live (`live_bidding.fxml`)

### Kiểm thử & Chất lượng
- [x] Unit tests cho tất cả service, model, validator, observer, payment
- [x] Concurrency test (`ConcurrentBiddingTest`) — đặt giá đồng thời
- [x] Code coverage đo bằng JaCoCo
- [x] Tuân thủ Google Java Style Guide qua Checkstyle

---

## Báo cáo & Demo

| Tài liệu | Link |
|---|---|
| Báo cáo PDF | *(sẽ cập nhật)* |
| Video demo | *(sẽ cập nhật)* |
