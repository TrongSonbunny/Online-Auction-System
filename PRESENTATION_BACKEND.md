# THUYẾT TRÌNH BACKEND - HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN

---

## MỤC LỤC

1. [Tổng Quan Kiến Trúc](#1-tổng-quan-kiến-trúc)
2. [Cấu Trúc Package & Chức Năng Từng Lớp](#2-cấu-trúc-package--chức-năng-từng-lớp)
3. [Các Phương Thức Quan Trọng — Phân Tích Chi Tiết](#3-các-phương-thức-quan-trọng--phân-tích-chi-tiết)
4. [Cú Pháp Đặc Biệt: Concurrency & Thread-Safety](#4-cú-pháp-đặc-biệt-concurrency--thread-safety)
5. [Liên Kết Backend với Các Tầng Khác](#5-liên-kết-backend-với-các-tầng-khác)
6. [Luồng Hoạt Động Của Hệ Thống](#6-luồng-hoạt-động-của-hệ-thống)
7. [Các Mẫu Thiết Kế — Tại Sao & Dùng Ở Đâu](#7-các-mẫu-thiết-kế--tại-sao--dùng-ở-đâu)

---

## 1. TỔNG QUAN KIẾN TRÚC

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND (JavaFX)                     │
│   LoginController  AuctionController  BidController ...      │
└──────────────────────┬──────────────────────────────────────┘
                       │ TCP Socket / JSON
┌──────────────────────▼──────────────────────────────────────┐
│                  NETWORK LAYER (Port 8080)                   │
│  ServerMain → ClientHandler (Virtual Thread per Client)      │
│  ClientMessage ←→ ServerMessage (JSON via Gson)              │
│  ClientActionHandler → Command Pattern Dispatch              │
└──────────────────────┬──────────────────────────────────────┘
                       │ CommandContext (DI Container)
┌──────────────────────▼──────────────────────────────────────┐
│                   SERVICE LAYER (Business Logic)             │
│  AuthService  AuctionService  BidService  AutoBidService     │
│  AuctionScheduler  PaymentProcessor  ...                     │
└──────┬──────────────┬──────────────┬───────────────────────┘
       │              │              │
┌──────▼──────┐ ┌─────▼──────┐ ┌────▼────────────────────────┐
│  MODEL LAYER │ │ OBSERVER   │ │  DATABASE LAYER (SQLite)    │
│  Auction     │ │ SYSTEM     │ │  UserDao  AuctionDao        │
│  User        │ │ Publisher  │ │  ItemDao  BidDao            │
│  AutoBid ... │ │ Observers  │ │  DatabaseManager            │
└─────────────┘ └────────────┘ └─────────────────────────────┘
```

**Hệ thống gồm 5 tầng rõ ràng:**

| Tầng | Trách nhiệm |
|------|------------|
| Frontend | JavaFX UI — hiển thị và nhận input người dùng |
| Network | TCP socket server — nhận/gửi JSON, định tuyến request |
| Service | Xử lý logic nghiệp vụ — bid, auction, auth, payment |
| Model | Thực thể dữ liệu — Auction, User, Bid... |
| Database | Lưu trữ bền vững — SQLite thông qua DAO |

---

## 2. CẤU TRÚC PACKAGE & CHỨC NĂNG TỪNG LỚP

### 2.1 Package `backend/auction/`

#### `AuctionManager`
**Vai trò:** Kho lưu trữ auction trên RAM — Singleton toàn hệ thống.

```
Trạng thái: Map<String, Auction> (ConcurrentHashMap)
Phương thức:
  + getInstance()         → Lấy instance duy nhất (Initialization-on-demand Holder)
  + addAuction(auction)   → Thêm auction vào RAM
  + removeAuction(id)     → Xóa auction khỏi RAM
  + findAuction(id)       → Tìm auction theo ID
  + getAllAuctions()       → Lấy toàn bộ (unmodifiable view)
```

**Tại sao cần?** — Server cần truy xuất auction cực nhanh (O(1) qua HashMap). Lưu trên RAM thay vì query DB liên tục khi đấu giá đang diễn ra.

---

#### `AuctionService`
**Vai trò:** Xử lý vòng đời auction — tạo, bắt đầu, cập nhật, hủy, kết thúc.

```
Phương thức:
  + createAuction(seller, item, startingPrice, durationSeconds)
      → Tạo Auction mới, thêm vào AuctionManager, publish AUCTION_CREATED
  + startAuction(auctionId)
      → Chuyển PENDING → ACTIVE, schedule timer, publish AUCTION_STARTED
  + updateAuction(auctionId, ...)
      → Cập nhật thông tin khi còn PENDING, publish AUCTION_UPDATED
  + cancelAuction(auctionId)
      → Hủy bỏ, cancel timer, publish AUCTION_CANCELLED
  + finishAuction(auctionId)
      → Kết thúc thủ công, publish AUCTION_FINISHED
```

---

#### `AuctionScheduler`
**Vai trò:** Tự động kết thúc auction đúng giờ; hỗ trợ **Anti-Snipe** — gia hạn thêm thời gian nếu có bid vào phút cuối.

```
Fields:
  - ScheduledExecutorService scheduler    → 1 platform thread để tính giờ
  - ExecutorService virtualExecutor       → Virtual threads để thực thi
  - ConcurrentHashMap scheduledTasks      → Map<auctionId, ScheduledFuture>
  - ReentrantLock lock                    → Bảo vệ reschedule

Phương thức:
  + scheduleAuctionFinish(auction, durationSeconds)
      → Đặt lịch gọi finishSafely() sau N giây
  + rescheduleAuctionFinish(auction, extensionSeconds)
      → Hủy lịch cũ, tính lại thời gian, đặt lịch mới
  + finishSafely(auction)               [private]
      → Gọi auction.finish(), bắt IllegalStateException nếu đã kết thúc
  + shutdown()
      → Tắt gracefully cả 2 executor
```

---

#### `AuctionValidator`
**Vai trò:** Kiểm tra tính hợp lệ của auction trước khi tạo.

```
Phương thức:
  + validateAuction(seller, item, startingPrice, durationSeconds)
      → Kiểm tra: seller/item ≠ null, startingPrice > 0,
        0 < duration ≤ 72h, seller có quyền tạo auction
```

---

### 2.2 Package `backend/auth/`

#### `AuthService`
**Vai trò:** Xử lý đăng ký và đăng nhập.

```
Phương thức:
  + register(ClientMessage)
      → Validate → kiểm tra email tồn tại → UserFactory.createUser() → saveUser()
  + login(ClientMessage)
      → Validate → normalizeLoginIdentifier() → findByEmail() → checkPassword()
  + normalizeLoginIdentifier(str)  [private]
      → Nếu nhập "ADMIN" → đổi thành "admin@auction.local"
  + validateRegisterMessage(msg)   [private]
  + validateLoginMessage(msg)      [private]
  + validateNotAdminRegister(msg)  [private]
      → Chặn không cho đăng ký role ADMIN từ client
```

---

### 2.3 Package `backend/bid/`

#### `BidService`
**Vai trò:** Điều phối toàn bộ quy trình đặt giá — validation, ghi lịch sử, anti-snipe, auto-bid.

```
Phương thức:
  + placeBid(auction, bidder, amount)
      → [Lock] validate → cập nhật giá → tạo transaction → [Unlock]
      → publishBidEvent → checkAndApplyAntiSnipe → processAutoBids
  + checkAndApplyAntiSnipe(auction)     [private]
      → Nếu còn ≤ 30 giây → reschedule thêm 60 giây
  + processAutoBids(auction, prevBidder) [private]
      → Gọi autoBidService → publish từng auto-bid event
  + publishBidEvent(...)               [private]
      → Đóng gói AuctionEvent + BidEventPayload → gửi lên publisher
  + createTransaction(...)             [private]
      → Tạo BidTransaction với ID mới từ IdGenerator
```

---

#### `AutoBidService`
**Vai trò:** Đăng ký và thực thi auto-bid theo cơ chế cascade.

```
Phương thức:
  + registerAutoBid(auction, bidder, maxBid, increment)
      → [Lock] validate → new AutoBid → autoBidManager.addAutoBid() → [Unlock]
  + processAutoBids(auction, outbidBidder)
      → Vòng lặp cascade: tìm auto-bid của người vừa bị vượt giá
        → tính nextBid → [Lock] updateHighestBid → tạo transaction → [Unlock]
        → currentOutbid = bidder vừa bị vượt → lặp tiếp
  + cancelAutoBid(autoBidId)
      → autoBidManager.removeAutoBidById()
  + findAutoForOutbidBidder(...)        [private]
      → So sánh bằng userId (không dùng reference equality!)
```

---

#### `AutoBidManager`
**Vai trò:** Lưu trữ các AutoBid đang hoạt động theo từng auction.

```
Fields:
  - ConcurrentHashMap<String, CopyOnWriteArrayList<AutoBid>> autoBidsMap
Phương thức:
  + addAutoBid(autoBid)
  + removeAutoBidById(autoBidId)
  + getAutoBidsForAuction(auctionId)
  + clearForAuction(auctionId)
```

---

#### `BidHistoryManager`
**Vai trò:** Lưu toàn bộ lịch sử bid trên RAM trong phiên chạy.

```
Fields:
  - List<BidTransaction> bidHistory   (CopyOnWriteArrayList)
  - ReentrantLock lock
Phương thức:
  + addTransaction(transaction)     → [Lock] thêm vào list → [Unlock]
  + getTransactionsForAuction(id)   → lọc theo auctionId
  + getAllTransactions()             → unmodifiable view
```

---

#### `BidValidator`
**Vai trò:** Kiểm tra tính hợp lệ của một bid trước khi thực hiện.

```
Phương thức:
  + validateBid(auction, bidder, amount)
      → auction ≠ null
      → bidder ≠ null
      → amount > 0
      → auction đang ACTIVE
      → amount > currentHighestBid
      → bidder.canPlaceBid() == true
```

---

#### `BidResult`
**Vai trò:** Value object chứa kết quả của placeBid.

```
Fields:
  - BidTransaction manualTransaction      → bid thủ công từ người dùng
  - List<BidTransaction> autoBidTransactions → auto-bid phát sinh
Phương thức:
  + getManualTransaction()
  + getAutoBidTransactions()
  + hasAutoBids()
```

---

### 2.4 Package `backend/database/`

#### `DatabaseConnection`
**Vai trò:** Quản lý kết nối JDBC tới SQLite.

```
Phương thức:
  + getConnection()   → Trả về Connection (tạo mới nếu chưa có)
  + closeConnection() → Đóng kết nối
```

#### `DatabaseManager`
**Vai trò:** Khởi tạo schema database (tạo bảng, default admin) khi khởi động server.

```
Phương thức:
  + initialize()
      → Tạo bảng users, items, auctions, bid_transactions
      → Bật PRAGMA foreign_keys = ON
      → Insert admin mặc định nếu chưa có
```

#### `DataManager` (interface)
**Vai trò:** Contract cho các DAO — đảm bảo tính nhất quán.

#### DAO Classes (`UserDao`, `AuctionDao`, `ItemDao`, `BidDao`)
**Vai trò:** Trừu tượng hóa thao tác database — mỗi DAO phụ trách 1 bảng.

| DAO | Các phương thức chính |
|-----|----------------------|
| `UserDao` | `saveUser()`, `findByEmail()`, `existsByEmail()`, `checkPassword()` |
| `AuctionDao` | `saveAuction()`, `updateAuction()`, `findById()`, `findAll()` |
| `ItemDao` | `saveItem()`, `findById()` |
| `BidDao` | `saveBid()`, `findByAuctionId()` |

---

### 2.5 Package `backend/observer/`

#### `AuctionObserver` (interface)
```java
public interface AuctionObserver {
    void update(AuctionEvent event);
}
```

#### `AuctionEventPublisher`
**Vai trò:** Quản lý danh sách observer; phân phối event.

```
Fields:
  - CopyOnWriteArrayList<AuctionObserver> observers
Phương thức:
  + addObserver(observer)
  + removeObserver(observer)
  + publishEvent(event)       → for each observer: observer.update(event)
  + getObserverCount()
```

#### `AuctionEvent`
**Vai trò:** Đóng gói thông tin một sự kiện.

```
Fields:
  - AuctionEventType type    → loại sự kiện (enum)
  - String auctionId
  - String message
  - Object payload           → Auction hoặc BidEventPayload
```

#### `AuctionEventType` (enum)
```
AUCTION_CREATED, AUCTION_STARTED, AUCTION_FINISHED,
AUCTION_CANCELLED, AUCTION_EXTENDED, AUCTION_UPDATED,
NEW_BID, AUTO_BID_PLACED, AUTO_BID_REGISTERED, AUTO_BID_CANCELLED
```

#### `AuctionEventSerializer`
**Vai trò:** Chuyển AuctionEvent → JSON string để gửi cho frontend.

#### Observers (trong `observers/` sub-package)

| Observer | Trách nhiệm |
|----------|------------|
| `DataPersistenceObserver` | Lưu vào SQLite khi nhận event |
| `FrontendNotificationObserver` | Serialize JSON → gửi tới client qua FrontendNotifier |
| `AdminObserver` | Log mọi event ra console/file |
| `SellerObserver` | Thông báo cho Seller khi auction của họ có hoạt động |
| `BidderObserver` | Thông báo cho Bidder khi bị vượt giá |

---

### 2.6 Package `backend/payment/`

#### `PaymentProcessor`
```
Phương thức:
  + processPayment(user, amount, method)  → strategy.pay()
  + processRefund(user, amount, method)   → strategy.refund()
```

#### `PaymentValidator`
```
Phương thức:
  + validate(amount, strategy)  → amount > 0, strategy ≠ null
```

#### `PaymentLogger`
```
Phương thức:
  + logPayment(...)   → ghi log giao dịch
```

---

### 2.7 Package `backend/util/`

#### `IdGenerator`
**Vai trò:** Tạo ID unique cho mọi thực thể.

```
Phương thức:
  + generateUserId()        → "U-" + UUID
  + generateAuctionId()     → "A-" + UUID
  + generateItemId()        → "I-" + UUID
  + generateTransactionId() → "T-" + UUID
  + generateAutoBidId()     → "AB-" + UUID
```

#### `PasswordHasher`
**Vai trò:** Hash và verify mật khẩu dùng thuật toán PBKDF2WithHmacSHA256.

```
Hằng số:
  - ITERATIONS = 310_000   (chuẩn NIST 2023)
  - SALT_LENGTH = 16 bytes
  - KEY_LENGTH = 32 bytes

Phương thức:
  + hash(password)          → "310000:hex_salt:hex_hash"
  + verify(password, hash)  → tách salt, tính lại hash, so sánh
```

---

### 2.8 Package `network/`

#### `ServerMain`
**Vai trò:** Điểm khởi động server TCP — lắng nghe cổng 8080.

```
Fields:
  - volatile boolean isRunning   → cờ tắt graceful
  - ScheduledExecutorService     → virtual thread executor

Phương thức:
  + start()   → ServerSocket(8080) → accept loop → spawn ClientHandler
  + stop()    → isRunning = false → shutdown executor
  + main()    → init DB → start server
```

#### `ClientHandler`
**Vai trò:** Xử lý 1 kết nối TCP — cầu nối giữa Network, Service và Frontend.

```
Fields:
  - static CopyOnWriteArrayList<ClientHandler> activeClients  → tất cả client online
  - Socket clientSocket
  - Gson gson                            → parser với ExclusionStrategy
  - ConcurrentHashMap<String, AuctionObserver> activeObservers
  - PrintWriter writer
  - String authenticatedUserId

Phương thức:
  + run()                        → vòng lặp đọc JSON từ socket
  + handleIncomingRequest(json)  → parse → dispatch → gửi response + broadcast
  + sendNotification(id, json)   [FrontendNotifier] → gửi event real-time
  + sendToClient(message)        [synchronized] → ghi JSON ra socket
  + cleanup()                    → xóa khỏi activeClients, đóng socket
```

#### `ClientActionHandler`
**Vai trò:** Dispatch request tới đúng Command.

```
+ doAction(ClientMessage) → ClientCommandFactory → map.get(action) → command.execute()
```

#### `ClientCommandFactory`
**Vai trò:** Factory tạo map Action → Command.

```
+ createDefaultCommands() → HashMap<ActionType, ClientCommand>
```

#### `CommandContext`
**Vai trò:** DI container thủ công — khởi tạo và kết nối tất cả dependency.

---

#### Command Classes (`network/command/`)

| Command | Action | Chức năng |
|---------|--------|-----------|
| `LoginCommand` | LOGIN | Gọi AuthService.login() |
| `RegisterCommand` | REGISTER | Gọi AuthService.register() |
| `CreateAuctionCommand` | CREATE_AUCTION | Tạo auction mới |
| `StartAuctionCommand` | START_AUCTION | Bắt đầu đấu giá |
| `BidCommand` | BID | Đặt giá |
| `RegisterAutoBidCommand` | REGISTER_AUTO_BID | Đăng ký auto-bid |
| `CancelAutoBidCommand` | CANCEL_AUTO_BID | Hủy auto-bid |
| `FinishAuctionCommand` | FINISH_AUCTION | Kết thúc auction |
| `CancelAuctionCommand` | CANCEL_AUCTION | Hủy auction |
| `UpdateAuctionCommand` | UPDATE_AUCTION | Cập nhật thông tin |
| `GetAllAuctionsCommand` | GET_ALL_AUCTIONS | Lấy danh sách |
| `GetBidHistoryCommand` | GET_BID_HISTORY | Lấy lịch sử bid |

---

### 2.9 Package `models/`

#### Hierarchy `User`

```
User (abstract)
├── userId, name, email, role
├── PermissionStrategy permissionStrategy
├── canCreateAuction() → permissionStrategy.canCreateAuction()
├── canPlaceBid()      → permissionStrategy.canPlaceBid()
├── canDeleteAuction() → permissionStrategy.canDeleteAuction()
├── canBanUser()       → permissionStrategy.canBanUser()
│
├── Admin
│   └── AdminPermission (tất cả = true)
│
├── Seller
│   ├── SellerPermission (canCreateAuction = true)
│   └── AtomicInteger totalAuctionsCreated
│
└── Bidder
    ├── BidderPermission (canPlaceBid = true)
    ├── AtomicInteger totalBidsPlaced
    └── PaymentStrategy paymentStrategy
```

#### `Auction`

```
Fields:
  - auctionId, seller, item
  - double startingPrice, currentHighestBid
  - Bidder currentHighestBidder
  - AuctionStatus status           → PENDING / ACTIVE / FINISHED / CANCELLED
  - LocalDateTime createdAt, startTime, endTime, scheduledEndTime
  - long durationSeconds
  - transient ReentrantLock lock   → không serialize, khởi tạo khi cần

Phương thức:
  + start()                        → PENDING → ACTIVE
  + finish()                       → ACTIVE → FINISHED, setEndTime
  + cancel()                       → * → CANCELLED
  + updateHighestBid(bidder, amt)  → [Lock-safe] cập nhật giá
  + isActive()                     → status == ACTIVE
  + getLock()                      → trả về lock (tạo mới nếu null)
  + extendScheduledEndTime(secs)   → scheduledEndTime += secs
```

---

## 3. CÁC PHƯƠNG THỨC QUAN TRỌNG — PHÂN TÍCH CHI TIẾT

### 3.1 `BidService.placeBid()` — Tâm Điểm Của Hệ Thống

```java
public BidResult placeBid(Auction auction, Bidder bidder, double amount) {

    BidTransaction manualTransaction;
    final Bidder previousHighestBidder;

    // BƯỚC 1: CRITICAL SECTION — Khóa auction lại
    auction.getLock().lock();
    try {
        // B1: Validate toàn bộ điều kiện
        bidValidator.validateBid(auction, bidder, amount);

        // B2: Lưu người đang giữ giá cao nhất (để cascade auto-bid sau)
        previousHighestBidder = auction.getCurrentHighestBidder();

        // B3: Cập nhật giá cao nhất trong auction
        auction.updateHighestBid(bidder, amount);

        // B4: Tăng counter stats
        bidder.incrementTotalBidsPlaced();

        // B5: Tạo bản ghi giao dịch với ID unique
        manualTransaction = createTransaction(bidder, auction.getAuctionId(), amount);

        // B6: Lưu vào lịch sử bid (thread-safe)
        bidHistoryManager.addTransaction(manualTransaction);

    } finally {
        // LUÔN LUÔN mở khóa dù có exception
        auction.getLock().unlock();
    }

    // BƯỚC 2: OUTSIDE LOCK — Publish event (I/O nặng, không nên giữ lock)
    publishBidEvent(AuctionEventType.NEW_BID, auction, manualTransaction, "Có bid mới.");

    // BƯỚC 3: Kiểm tra Anti-Snipe (ngoài lock vì chỉ đọc scheduledEndTime)
    checkAndApplyAntiSnipe(auction);

    // BƯỚC 4: Kích hoạt Auto-Bid cascade
    List<BidTransaction> autoBidTransactions = processAutoBids(auction, previousHighestBidder);

    return new BidResult(manualTransaction, autoBidTransactions);
}
```

**Tại sao thiết kế như vậy?**

| Quyết định | Lý do |
|------------|-------|
| Dùng ReentrantLock thay synchronized | Có thể thử lock (tryLock), tránh deadlock có kiểm soát hơn |
| Lưu `previousHighestBidder` trước khi update | Auto-bid cần biết ai vừa bị vượt để cascade đúng |
| Publish event NGOÀI lock | Event gọi DAO (I/O) + network write — giữ lock trong I/O = deadlock nguy hiểm |
| Anti-snipe NGOÀI lock | Chỉ đọc `scheduledEndTime` — không cần khóa toàn auction |
| Auto-bid NGOÀI lock | Auto-bid tự lấy lock bên trong vòng lặp của nó |

---

### 3.2 `AutoBidService.processAutoBids()` — Cascade Logic

```java
public List<BidTransaction> processAutoBids(Auction auction, Bidder outbidBidder) {
    List<BidTransaction> createdTransactions = new ArrayList<>();
    Bidder currentOutbid = outbidBidder;

    while (true) {  // Vòng lặp cascade — tiếp tục cho đến khi không ai phản ứng được

        auction.getLock().lock();
        try {
            if (!auction.isActive()) break;  // Auction có thể kết thúc giữa chừng

            double currentBid = auction.getCurrentHighestBid();

            // Tìm auto-bid của người VỪA BỊ VƯỢT GIÁ
            Optional<AutoBid> bestOpt = findAutoForOutbidBidder(
                auction.getAuctionId(), currentOutbid, currentBid);

            if (bestOpt.isEmpty()) break;  // Không có ai muốn phản ứng → dừng

            AutoBid best = bestOpt.get();
            double nextBid = currentBid + best.getIncrement();
            if (nextBid > best.getMaxBid()) nextBid = best.getMaxBid();
            if (nextBid <= currentBid) break;  // Không thể bid thêm

            // Nhớ người đang giữ giá (sẽ là "outbid" tiếp theo trong cascade)
            final Bidder currentBidder = auction.getCurrentHighestBidder();

            auction.updateHighestBid(best.getBidder(), nextBid);
            // ... tạo transaction, lưu lịch sử ...

            // Người vừa bị vượt giá → sẽ kiểm tra auto-bid ở vòng tiếp theo
            currentOutbid = currentBidder;

        } finally {
            auction.getLock().unlock();
        }
    }
    return createdTransactions;
}
```

**Ví dụ cascade:**
```
Giá hiện tại: 100
A bid thủ công: 150
→ B bị vượt, B có auto-bid maxBid=200, increment=20 → B auto-bid 170
→ A bị vượt, A có auto-bid maxBid=180, increment=10 → A auto-bid 180
→ B bị vượt, B muốn auto-bid 200 → 200 > 180 → B auto-bid 200
→ A bị vượt, A maxBid=180 < 200 → DỪNG
Kết quả: B thắng với 200
```

**Tại sao compare bằng `getUserId()` không phải `==`?**
```java
ab.getBidder().getUserId().equals(outbidBidder.getUserId())
// KHÔNG phải: ab.getBidder() == outbidBidder
```
Vì mỗi request load user mới từ DB tạo ra **object mới** cho cùng 1 người dùng.
So sánh reference `==` sẽ luôn trả về `false` → auto-bid không bao giờ hoạt động.

---

### 3.3 `AuctionScheduler.rescheduleAuctionFinish()` — Anti-Snipe

```java
public void rescheduleAuctionFinish(Auction auction, long extensionSeconds) {
    lock.lock();  // Lock scheduler để tránh race condition khi nhiều bid cùng lúc
    try {
        if (auction == null || !auction.isActive()) return;

        final String auctionId = auction.getAuctionId();

        // Hủy task cũ (cancel(false) = không interrupt nếu đang chạy)
        ScheduledFuture<?> existing = scheduledTasks.get(auctionId);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }

        // Tính thời gian còn lại
        LocalDateTime scheduledEnd = auction.getScheduledEndTime();
        long remaining = scheduledEnd == null ? 0
            : Duration.between(LocalDateTime.now(), scheduledEnd).getSeconds();
        if (remaining < 0) remaining = 0;

        final long newDuration = remaining + extensionSeconds;
        auction.extendScheduledEndTime(extensionSeconds);

        // Publish event AUCTION_EXTENDED để frontend cập nhật countdown
        publishAuctionEvent(AuctionEventType.AUCTION_EXTENDED, auction, "Auction được gia hạn.");

        // Đặt lịch mới
        ScheduledFuture<?> newFuture = scheduler.schedule(
            () -> virtualExecutor.submit(() -> finishSafely(auction)),  // Virtual thread
            newDuration, TimeUnit.SECONDS);

        scheduledTasks.put(auctionId, newFuture);

    } finally {
        lock.unlock();
    }
}
```

**Tại sao dùng 2 tầng executor?**
```
scheduler (platform thread) → chỉ để "đếm giờ", chi phí thấp
virtualExecutor             → thực thi công việc thực (finish auction, DAO, event)
                              → Virtual thread: non-blocking, tốt cho I/O
```

---

### 3.4 `ClientHandler.handleIncomingRequest()` — Trái Tim Network

```java
private void handleIncomingRequest(String rawJson) {
    try {
        // 1. Parse JSON → ClientMessage (Gson với ExclusionStrategy)
        ClientMessage request = gson.fromJson(rawJson, ClientMessage.class);

        // 2. Lần đầu nhận userId → lưu vào socket session
        if (authenticatedUserId == null && request.getUserId() != null) {
            this.authenticatedUserId = request.getUserId();
        }

        // 3. Command Pattern: dispatch đến đúng Command
        Object result = ClientActionHandler.doAction(request);

        // 4. Đóng gói kết quả → ServerMessage → gửi về client
        ServerMessage response = ServerMessage.builder()
            .action(request.getAction().name())
            .status(ServerMessage.STATUS_SUCCESS)
            .data(result)
            .build();
        sendToClient(response);

        // 5. Broadcast real-time: BID/CREATE_AUCTION → thông báo tất cả client khác
        ActionType action = request.getAction();
        if (action == ActionType.BID || action == ActionType.CREATE_AUCTION) {
            for (ClientHandler client : activeClients) {
                if (client != this) {
                    client.sendNotification("ALL", "UPDATE_PRICE");
                }
            }
        }
    } catch (Exception e) {
        sendToClient(ServerMessage.error("EXECUTION_ERROR", e.getMessage()));
    }
}
```

---

### 3.5 `PasswordHasher.hash()` & `verify()` — Bảo Mật Mật Khẩu

```java
// Hash: tạo salt ngẫu nhiên → PBKDF2 → lưu format "iterations:salt_hex:hash_hex"
public static String hash(String password) {
    byte[] salt = new byte[SALT_LENGTH];
    new SecureRandom().nextBytes(salt);  // SecureRandom, không phải Random

    PBEKeySpec spec = new PBEKeySpec(
        password.toCharArray(), salt,
        ITERATIONS,   // 310,000 iterations — chậm có chủ đích (chống brute force)
        KEY_LENGTH * 8
    );
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] hash = factory.generateSecret(spec).getEncoded();

    return ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
}

// Verify: tách salt từ stored hash → tính lại hash → compare
public static boolean verify(String password, String stored) {
    String[] parts = stored.split(":");
    int iterations = Integer.parseInt(parts[0]);
    byte[] salt = fromHex(parts[1]);
    byte[] expectedHash = fromHex(parts[2]);
    // Tính lại hash với cùng salt...
    return Arrays.equals(computedHash, expectedHash);
}
```

**Tại sao 310,000 iterations?** — NIST khuyến nghị tối thiểu 310,000 lần cho PBKDF2-SHA256 (2023). Mỗi lần verify mất ~100ms — đủ chậm để brute force không thực tế.

---

## 4. CÚ PHÁP ĐẶC BIỆT: CONCURRENCY & THREAD-SAFETY

### 4.1 `ReentrantLock` — Khóa Có Thể Tái Nhập

**Dùng ở đâu:** `Auction.lock`, `BidHistoryManager.lock`, `AuctionScheduler.lock`

```java
// Pattern chuẩn — LUÔN dùng try-finally để đảm bảo unlock
lock.lock();
try {
    // Critical section
} finally {
    lock.unlock();  // Luôn chạy dù có exception
}
```

**Pattern đặc biệt: Null-safe lock trong Auction**

```java
// lock là transient → Gson không khởi tạo → có thể null khi deserialize
private transient ReentrantLock lock;

public ReentrantLock getLock() {
    if (lock == null) {
        lock = new ReentrantLock();  // Lazy init
    }
    return lock;
}

// Sử dụng:
if (lock != null) { lock.lock(); }
try { /* ... */ }
finally { if (lock != null) { lock.unlock(); } }
```

**Tại sao `transient`?** — Gson dùng reflection để serialize. Nếu lock không phải `transient`, Gson sẽ cố serialize `ReentrantLock` → crash vì lock không có cấu trúc JSON hợp lệ.

**ReentrantLock vs `synchronized`:**

| Tiêu chí | `synchronized` | `ReentrantLock` |
|----------|---------------|-----------------|
| Syntax | Đơn giản | Verbose hơn |
| Có thể thử lock | Không | `tryLock()` |
| Interrupt khi chờ | Không | `lockInterruptibly()` |
| Fair lock | Không | `new ReentrantLock(true)` |
| Điều kiện phức tạp | 1 condition | Nhiều `Condition` |

---

### 4.2 `ConcurrentHashMap` — Map Thread-Safe

**Dùng ở đâu:** `AuctionManager.auctionMap`, `AutoBidManager.autoBidsMap`, `ClientHandler.activeObservers`, `AuctionScheduler.scheduledTasks`

```java
// Thread-safe mà không cần lock toàn bộ map
// Chỉ lock từng "segment" (bucket) khi write
private final Map<String, Auction> auctionMap = new ConcurrentHashMap<>();

// An toàn khi nhiều thread đọc/ghi đồng thời
auctionMap.put(id, auction);    // Thread A
auctionMap.get(id);             // Thread B đồng thời → OK
```

**Tại sao không dùng `HashMap`?** — `HashMap` KHÔNG thread-safe: hai thread put đồng thời có thể gây infinite loop trong Java 7-, hoặc mất data trong Java 8+.

---

### 4.3 `CopyOnWriteArrayList` — List Thread-Safe cho Observer

**Dùng ở đâu:** `AuctionEventPublisher.observers`, `ClientHandler.activeClients`, `NetworkClient.listeners`

```java
private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();

// Khi iterate để publish event:
for (AuctionObserver observer : observers) {
    observer.update(event);  // Thread A đang duyệt
}
// Thread B có thể addObserver() đồng thời → KHÔNG xảy ra ConcurrentModificationException
// Vì CopyOnWriteArrayList tạo COPY của mảng khi write
```

**Trade-off:** Write tốn kém (copy toàn bộ mảng) nhưng Read rất nhanh. Phù hợp khi read nhiều, write ít — đúng với observer pattern.

---

### 4.4 `volatile` — Visibility Đa Thread

**Dùng ở đâu:** `ServerMain.isRunning`, `NetworkClient.isRunning`

```java
private volatile boolean isRunning = true;

// Thread server:
while (isRunning) {
    Socket client = serverSocket.accept();
    // ...
}

// Thread khác (shutdown hook):
isRunning = false;  // Thay đổi này được thấy ngay bởi thread server
                    // Không có volatile → server thread có thể cache isRunning = true mãi
```

**Tại sao `volatile` chứ không phải `AtomicBoolean`?** — `volatile` đủ khi chỉ cần đọc/ghi đơn giản. `AtomicBoolean` cần khi có compound operation như `compareAndSet`.

---

### 4.5 `AtomicInteger` — Counter Thread-Safe

**Dùng ở đâu:** `Seller.totalAuctionsCreated`, `Bidder.totalBidsPlaced`

```java
private final AtomicInteger totalBidsPlaced = new AtomicInteger(0);

// Thread-safe increment — không cần lock
bidder.incrementTotalBidsPlaced();
// Bên trong: totalBidsPlaced.incrementAndGet()
//            = hardware CAS instruction, không block thread
```

---

### 4.6 Virtual Threads (Java 21) — Scale Server

**Dùng ở đâu:** `ServerMain`, `AuctionScheduler`

```java
// ServerMain: mỗi client một virtual thread
ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
virtualExecutor.submit(new ClientHandler(clientSocket));

// AuctionScheduler: scheduler đặt lịch, virtual thread thực thi
scheduler.schedule(
    () -> virtualExecutor.submit(() -> finishSafely(auction)),
    durationSeconds, TimeUnit.SECONDS
);

// NetworkClient: virtual thread riêng để listen message
Thread.ofVirtual().start(this::listenForMessages);
```

**Virtual Thread vs Platform Thread:**

| | Platform Thread | Virtual Thread |
|---|---|---|
| Stack memory | ~1MB/thread | ~1KB/thread |
| Số lượng | ~10,000 tối đa | ~Triệu cùng lúc |
| Thích hợp | CPU-intensive | I/O-intensive (socket, DB) |
| JDK | Java 1+ | Java 21+ |

**Tại sao quan trọng?** — TCP server cần 1 thread/client. Với 10,000 client đồng thời, platform thread tốn 10GB RAM. Virtual thread chỉ tốn ~10MB.

---

### 4.7 `ExclusionStrategy` trong Gson — Kiểm Soát Serialization

**Dùng ở đâu:** `ClientHandler.gson`

```java
this.gson = new GsonBuilder()
    .setExclusionStrategies(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            String fieldName = f.getName();
            // Bỏ qua các field gây crash khi serialize BidResult
            return fieldName.equals("manualTransaction")
                || fieldName.equals("autoBidsPlaced")
                || fieldName.equals("autoBidTransactions");
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            // Bỏ qua mọi class là Transaction hoặc subclass
            // (vì Transaction có circular references)
            return Transaction.class.isAssignableFrom(clazz);
        }
    })
    .create();
```

**Vấn đề giải quyết:** `BidResult` chứa `BidTransaction` chứa `Bidder` chứa `PaymentStrategy` — Gson serialize đệ quy sâu → StackOverflowError. ExclusionStrategy cắt đứt đệ quy này.

---

### 4.8 `Initialization-on-Demand Holder` — Thread-Safe Singleton

**Dùng ở đâu:** `AuctionManager`

```java
public class AuctionManager {
    private AuctionManager() { ... }

    // Inner class chỉ được load khi lần đầu gọi getInstance()
    // JVM đảm bảo class loading là thread-safe → không cần synchronized!
    private static final class Holder {
        static final AuctionManager INSTANCE = new AuctionManager();
    }

    public static AuctionManager getInstance() {
        return Holder.INSTANCE;  // Thread-safe, lazy, không overhead
    }
}
```

**So sánh với double-checked locking:**
```java
// Cách cũ (phức tạp, dễ sai):
private static volatile AuctionManager instance;
public static AuctionManager getInstance() {
    if (instance == null) {
        synchronized (AuctionManager.class) {
            if (instance == null) {
                instance = new AuctionManager();
            }
        }
    }
    return instance;
}

// Cách Holder (đơn giản, đúng, nhanh):
return Holder.INSTANCE;  // Done.
```

---

## 5. LIÊN KẾT BACKEND VỚI CÁC TẦNG KHÁC

### 5.1 Backend ↔ Model Layer

Models là **thực thể dữ liệu thuần túy** — Backend là nơi xử lý logic trên đó.

```
Auction (model)          AuctionService (backend)
├── status               ├── startAuction() → auction.start()
├── currentHighestBid    ├── BidService.placeBid() → auction.updateHighestBid()
└── lock: ReentrantLock  └── AuctionScheduler → auction.finish()

User (model)             AuthService (backend)
├── permissionStrategy   ├── login() → UserFactory.createUser()
└── role                 └── register() → userDao.saveUser(user, password)
```

**Dependency direction:** Backend phụ thuộc vào Model (import), Model KHÔNG phụ thuộc Backend.

---

### 5.2 Backend ↔ Network Layer

```
Network (ClientHandler)          Backend (Services)
        │                               │
        │  CommandContext (DI)          │
        │  ┌─────────────────────────┐  │
        └──► ClientActionHandler     │  │
             └──► Command.execute() ──►─┘
                  (BidCommand,          BidService.placeBid()
                   LoginCommand, ...)   AuthService.login()
```

**Luồng request:**
1. `ClientHandler` nhận JSON → parse `ClientMessage`
2. Gọi `ClientActionHandler.doAction(message)`
3. Factory tìm Command theo `ActionType`
4. Command dùng `CommandContext` lấy service → gọi service method
5. Kết quả trả về → `ClientHandler` đóng gói `ServerMessage` → gửi JSON

**Luồng event (push):**
1. Backend publish `AuctionEvent` → `AuctionEventPublisher`
2. `FrontendNotificationObserver.update(event)` được gọi
3. Serialize event → JSON string
4. Gọi `FrontendNotifier.sendNotification(auctionId, json)`
5. `ClientHandler` (implements FrontendNotifier) gửi tới socket

---

### 5.3 Backend ↔ Database Layer

```
Backend Services                    DAO Layer                  SQLite
     │                                  │                         │
AuctionService ──────────────────► AuctionDao.saveAuction() ──► INSERT INTO auctions
                                   AuctionDao.updateAuction()──► UPDATE auctions SET ...

DataPersistenceObserver (Observer) → Nhận AuctionEvent → Gọi DAO tương ứng
  AUCTION_CREATED  → itemDao.saveItem() + auctionDao.saveAuction()
  AUCTION_STARTED  → auctionDao.updateAuction()
  NEW_BID          → bidDao.saveBid()
  AUCTION_FINISHED → auctionDao.updateAuction()
```

**Tại sao dùng Observer thay vì gọi DAO trực tiếp từ Service?**
→ Service không cần biết về persistence. Thêm/bỏ persistence chỉ cần add/remove observer. Tuân thủ **Single Responsibility Principle**.

---

### 5.4 Backend ↔ Frontend (JavaFX Controllers)

```
Frontend (JavaFX)                    Network                    Backend
─────────────────                   ─────────                   ───────

LoginController.login()
  → NetworkClient.sendRequest(LOGIN)
    → TCP Socket → JSON →          ServerMain
                                   ClientHandler.run()
                                     → AuthService.login()
                                     → ServerMessage(SUCCESS, user)
                                   → JSON → TCP Socket
  → NetworkClient.listeners notified
  → LoginController.onMessageReceived()
  → Navigate to AuctionScreen
```

**Real-time update:**
```
BidCommand.execute() → BidService.placeBid() → AuctionEventPublisher
                                                    │
                                     ┌──────────────┘
                                     ▼
                          FrontendNotificationObserver.update()
                                     │
                                     ▼
                          ClientHandler.sendNotification()
                                     │
                                     ▼ TCP Push
                          NetworkClient.onMessageReceived()
                                     │
                                     ▼
                          AuctionController.updateUI()   (Platform.runLater)
```

---

### 5.5 Sơ Đồ Tổng Thể Liên Kết

```
┌──────────────────────────────────────────────────────────────────┐
│  FRONTEND (JavaFX)                                               │
│  Controllers ←──── NetworkClient ←──── MessageListener          │
│       │                  │ (TCP)             ▲                   │
└───────┼──────────────────┼─────────────────────────────────────┘
        │ sendRequest()    │ sendNotification()│
┌───────▼──────────────────▼─────────────────────────────────────┐
│  NETWORK                                                         │
│  ServerMain → ClientHandler (implements FrontendNotifier)        │
│               └── ClientActionHandler                            │
│                   └── Command → CommandContext                   │
└────────────────────────────┬────────────────────────────────────┘
                             │ uses services
┌────────────────────────────▼────────────────────────────────────┐
│  BACKEND (Services)                                              │
│  AuthService  AuctionService  BidService  AutoBidService         │
│  AuctionScheduler  AuctionManager                                │
│       │                  │                                       │
│       │ events           │ manages                               │
│  AuctionEventPublisher   AuctionManager (RAM cache)              │
│       │                                                          │
│  ┌────▼──────────────────────────────────────────────────────┐  │
│  │  Observers                                                 │  │
│  │  DataPersistenceObserver → DAO Layer → SQLite              │  │
│  │  FrontendNotificationObserver → FrontendNotifier → Client  │  │
│  │  AdminObserver, SellerObserver, BidderObserver → Logging   │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 6. LUỒNG HOẠT ĐỘNG CỦA HỆ THỐNG

### 6.1 Khởi Động Server

```
Main.main()
  └─► DatabaseManager.initialize()
        ├── Tạo bảng users, items, auctions, bid_transactions
        ├── PRAGMA foreign_keys = ON
        └── Insert admin mặc định (nếu chưa có)
  └─► ServerMain.start()
        ├── new ServerSocket(8080)
        ├── CommandContext() — wiring tất cả dependency
        └── while(isRunning):
              Socket client = serverSocket.accept()
              virtualExecutor.submit(new ClientHandler(client))
```

### 6.2 Luồng Đăng Nhập

```
User nhập email/password → LoginController
  → NetworkClient.sendRequest({action:LOGIN, email, password})
    → JSON qua TCP → ClientHandler.handleIncomingRequest()
      → ClientActionHandler.doAction() → LoginCommand.execute()
        → AuthService.login()
          → normalizeLoginIdentifier() (ADMIN → admin@auction.local)
          → UserDao.findByEmail()      (SELECT FROM users)
          → UserDao.checkPassword()    (PBKDF2 verify)
          → return User object
      → ServerMessage(SUCCESS, user)
    → JSON qua TCP → NetworkClient.listeners.onMessageReceived()
  → LoginController hiển thị màn hình chính
```

### 6.3 Luồng Đặt Giá (Bid)

```
Bidder nhấn "Bid" → BidController
  → NetworkClient.sendRequest({action:BID, auctionId, amount, userId})
    → TCP → ClientHandler → BidCommand.execute()
      → AuctionManager.findAuction(id)  (O(1) từ RAM)
      → UserDao.findById(userId)        (load fresh từ DB)
      → BidService.placeBid(auction, bidder, amount)
          ┌─ [Lock auction]
          │  BidValidator.validateBid()
          │  auction.updateHighestBid()
          │  createTransaction()
          │  bidHistoryManager.addTransaction()
          └─ [Unlock]
          ─ publishBidEvent(NEW_BID) ──► AuctionEventPublisher
          │                              ├─ DataPersistenceObserver → BidDao.saveBid()
          │                              ├─ FrontendNotificationObserver → ClientHandler.sendNotification()
          │                              └─ BidderObserver → notify outbid bidder
          ─ checkAndApplyAntiSnipe()
          │  └─ nếu ≤30s: AuctionScheduler.rescheduleAuctionFinish(+60s)
          └─ processAutoBids() → cascade auto-bid loop

      → BidResult → ServerMessage(SUCCESS, result)
    → TCP → NetworkClient → BidController.onSuccess()
```

### 6.4 Luồng Kết Thúc Auction Tự Động

```
AuctionScheduler (sau N giây):
  scheduler.schedule() fires
    → virtualExecutor.submit(finishSafely)
      → auction.finish()          [ACTIVE → FINISHED]
      → publishAuctionEvent(AUCTION_FINISHED)
          ├─ DataPersistenceObserver → AuctionDao.updateAuction(status=FINISHED)
          └─ FrontendNotificationObserver → broadcast tới tất cả watcher
               → ClientHandler.sendNotification("auctionId", json)
               → TCP push → NetworkClient → AuctionController.updateUI()
                            Platform.runLater(() → UI update)
```

---

## 7. CÁC MẪU THIẾT KẾ — TẠI SAO & DÙNG Ở ĐÂU

### 7.1 Singleton Pattern

**Dùng ở đâu:** `AuctionManager`, `NetworkClient`

**Tại sao cần?**
- `AuctionManager` phải là **một nguồn sự thật duy nhất** cho danh sách auction đang hoạt động. Nếu có 2 instance, ClientHandler A và B sẽ nhìn thấy 2 danh sách khác nhau → mất đồng bộ.
- `NetworkClient` (phía client): chỉ có 1 kết nối TCP duy nhất đến server.

**Cài đặt:** Initialization-on-demand Holder (thread-safe, lazy, không overhead):
```java
private static final class Holder {
    static final AuctionManager INSTANCE = new AuctionManager();
}
public static AuctionManager getInstance() { return Holder.INSTANCE; }
```

---

### 7.2 Command Pattern

**Dùng ở đâu:** Package `network/command/`

**Tại sao cần?**
- Server nhận **13+ loại action** khác nhau từ client. Không dùng Command → `ClientActionHandler` sẽ là 1 hàm khổng lồ với 13 `if-else` → vi phạm Open/Closed Principle.
- Dễ thêm action mới: chỉ thêm 1 class Command + 1 entry trong Factory.
- Mỗi Command tự quản lý logic của mình.

**Cấu trúc:**
```
ClientCommand (interface)
  └── execute(CommandContext, ClientMessage) → Object

BaseClientCommand (abstract)
  ├── LoginCommand
  ├── BidCommand
  ├── CreateAuctionCommand
  └── ... (13 commands)

ClientCommandFactory.createDefaultCommands()
  → HashMap<ActionType, ClientCommand>

ClientActionHandler.doAction(message)
  → map.get(message.getAction()).execute(context, message)
```

---

### 7.3 Observer Pattern

**Dùng ở đâu:** Toàn bộ package `backend/observer/`

**Tại sao cần?**
Khi có bid mới, hệ thống phải làm **5 việc đồng thời**:
1. Lưu vào DB
2. Thông báo cho người mua
3. Thông báo cho người bán
4. Cập nhật UI tất cả người xem
5. Log cho admin

Nếu `BidService` gọi trực tiếp từng việc → BidService phụ thuộc vào DAO, network socket, logger → **không thể test, không thể bỏ bớt**.

Observer Pattern tách rời hoàn toàn:
- `BidService` chỉ biết `AuctionEventPublisher.publishEvent()`
- Ai muốn nhận event thì đăng ký → thêm/xóa observer không ảnh hưởng BidService

**Luồng:**
```
Service.publish(event)
    → AuctionEventPublisher
        → DataPersistenceObserver.update()   → DB
        → FrontendNotificationObserver.update() → network push
        → SellerObserver.update()            → notify seller
        → BidderObserver.update()            → notify outbid
        → AdminObserver.update()             → log
```

---

### 7.4 Factory Pattern

**Dùng ở đâu:** `UserFactory`, `ItemFactory`, `DefaultPaymentFactory`, `ClientCommandFactory`

**Tại sao cần?**

`UserFactory`:
```java
// Thay vì: new Admin(id, name, email) hoặc new Seller(id, ...) hoặc new Bidder(id, ...)
// → caller phải biết loại cụ thể

// Factory che giấu chi tiết tạo:
User user = UserFactory.createUser(role, name, email);
// role=ADMIN  → return new Admin(...)
// role=SELLER → return new Seller(...)
// role=BIDDER → return new Bidder(...)
```

`ClientCommandFactory`:
```java
// Đăng ký tất cả commands một lần duy nhất khi khởi động
Map<ActionType, ClientCommand> commands = new HashMap<>();
commands.put(ActionType.LOGIN, new LoginCommand());
commands.put(ActionType.BID, new BidCommand());
// ...
return commands;
```

---

### 7.5 Strategy Pattern

**Dùng ở đâu:**
- `PermissionStrategy` (AdminPermission, SellerPermission, BidderPermission)
- `PaymentStrategy` (MomoPayment, BankPayment, VnPayPayment)

**Tại sao cần?**

Permission:
```java
// Thay vì:
if (user.getRole() == ADMIN) return true;
else if (user.getRole() == SELLER) return role == SELLER;
// ... lặp lại ở mọi nơi check permission

// Strategy: logic permission đóng gói trong class riêng
user.canCreateAuction()
  → permissionStrategy.canCreateAuction()
  → AdminPermission: return true
  → SellerPermission: return true
  → BidderPermission: return false
```

Payment:
```java
// Dễ thêm phương thức thanh toán mới (ví dụ: ZaloPay)
// mà không sửa PaymentProcessor
class ZaloPayPayment implements PaymentStrategy {
    public boolean pay(double amount) { /* ZaloPay API */ }
}
bidder.setPaymentStrategy(new ZaloPayPayment());
```

---

### 7.6 DAO Pattern (Data Access Object)

**Dùng ở đâu:** `UserDao`, `AuctionDao`, `ItemDao`, `BidDao`

**Tại sao cần?**
- Tách logic business khỏi SQL
- Dễ thay đổi database (SQLite → PostgreSQL): chỉ sửa DAO
- Dễ mock DAO trong unit test

```
BidService        →    BidDao          →   SQLite
"lưu transaction"      "INSERT INTO bid_transactions..."
```

---

### 7.7 Builder Pattern

**Dùng ở đâu:** `ClientMessage.Builder`, `ServerMessage.Builder`

**Tại sao cần?**
`ServerMessage` có 6+ optional fields. Constructor với 6 tham số khó đọc, dễ nhầm vị trí:

```java
// Không dùng Builder (khó đọc):
new ServerMessage("BID", "SUCCESS", null, data, null, null);

// Dùng Builder (rõ ràng, an toàn):
ServerMessage.builder()
    .action("BID")
    .status(ServerMessage.STATUS_SUCCESS)
    .data(result)
    .build();
```

---

### 7.8 Template Method Pattern (ngầm)

**Dùng ở đâu:** `BaseClientCommand`

```java
// BaseClientCommand định nghĩa khung xử lý chung:
public abstract class BaseClientCommand implements ClientCommand {
    @Override
    public Object execute(CommandContext ctx, ClientMessage msg) {
        validate(msg);         // hook — subclass có thể override
        return doExecute(ctx, msg); // abstract — subclass phải implement
    }
    protected abstract Object doExecute(CommandContext ctx, ClientMessage msg);
    protected void validate(ClientMessage msg) { /* default: nothing */ }
}
```

---

### Tổng Hợp Mẫu Thiết Kế

| Mẫu | Dùng ở đâu | Lợi ích chính |
|-----|-----------|--------------|
| **Singleton** | AuctionManager, NetworkClient | 1 nguồn sự thật, tiết kiệm tài nguyên |
| **Command** | 13 network commands | Mở rộng không sửa cũ, tách logic |
| **Observer** | Event system (5 observers) | Decoupling service ↔ side effects |
| **Factory** | UserFactory, ItemFactory, CommandFactory | Che giấu logic tạo object |
| **Strategy** | Permission, Payment | Thay đổi hành vi runtime, dễ mở rộng |
| **DAO** | 4 DAO classes | Tách persistence, dễ test |
| **Builder** | ClientMessage, ServerMessage | Tạo object phức tạp an toàn |
| **Template Method** | BaseClientCommand | Tái dùng code khung, subclass điền logic |

---

## KẾT LUẬN

Backend của hệ thống đấu giá được xây dựng theo các nguyên tắc:

1. **Phân tầng rõ ràng** — Network / Service / Model / Database tách biệt hoàn toàn
2. **Thread-safety** — ReentrantLock, ConcurrentHashMap, CopyOnWriteArrayList, volatile đúng chỗ
3. **Scale bằng Virtual Thread** — Hàng nghìn kết nối đồng thời với chi phí RAM tối thiểu
4. **Decoupling qua Observer** — Services không biết về persistence hay network I/O
5. **Mở rộng qua Command & Strategy** — Thêm action/payment/permission không sửa code cũ
6. **Bảo mật** — PBKDF2 với 310,000 iterations, block admin register, ExclusionStrategy chống reflection leak
