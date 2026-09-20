# HỆ THỐNG OBSERVER — PHÂN TÍCH CHI TIẾT

---

## MỤC LỤC

1. [Vấn Đề Cần Giải Quyết](#1-vấn-đề-cần-giải-quyết)
2. [Observer Pattern Là Gì](#2-observer-pattern-là-gì)
3. [Sơ Đồ Toàn Bộ Hệ Thống Observer](#3-sơ-đồ-toàn-bộ-hệ-thống-observer)
4. [Từng Class — Code Thực Tế + Giải Thích](#4-từng-class--code-thực-tế--giải-thích)
5. [Luồng Sự Kiện Từ Đầu Đến Cuối](#5-luồng-sự-kiện-từ-đầu-đến-cuối)
6. [Payload: Tại Sao Cần 2 Loại Khác Nhau](#6-payload-tại-sao-cần-2-loại-khác-nhau)
7. [FrontendNotifier — Cầu Nối Backend với Network](#7-frontendnotifier--cầu-nối-backend-với-network)
8. [Thread-Safety Trong Observer](#8-thread-safety-trong-observer)
9. [Tại Sao Thiết Kế Như Vậy — So Sánh Phương Án](#9-tại-sao-thiết-kế-như-vậy--so-sánh-phương-án)

---

## 1. VẤN ĐỀ CẦN GIẢI QUYẾT

Hãy tưởng tượng khi có một **bid mới** xảy ra trong hệ thống. Những việc cần làm ngay lập tức:

| Việc cần làm | Ai cần làm |
|---|---|
| Lưu bid transaction xuống SQLite | DataPersistenceObserver |
| Cập nhật giá cao nhất trong bảng auctions | DataPersistenceObserver |
| Gửi cập nhật giá real-time đến tất cả người xem | FrontendNotificationObserver |
| Thông báo cho Seller biết có người vừa bid | SellerObserver |
| Thông báo cho Bidder bị vượt giá | BidderObserver |
| Ghi log cho Admin theo dõi | AdminObserver |

**Cách tệ nhất — gọi thẳng trong BidService:**

```java
// KHÔNG LÀM NHƯ NÀY:
public BidResult placeBid(Auction auction, Bidder bidder, double amount) {
    // ... logic bid ...

    // BidService bây giờ phải biết về:
    bidDao.saveBidTransaction(transaction);       // Database
    auctionDao.updateAuction(auction);            // Database
    clientHandler.sendToClient(response);         // Network socket
    logger.info("New bid by " + bidder.getName()); // Logging
    emailService.sendEmailToSeller(...);          // Email
    // ...
}
```

**Vấn đề:**
- `BidService` phụ thuộc vào 5+ thứ không liên quan đến logic bid
- Thêm tính năng mới (ví dụ: push notification) → phải sửa BidService
- Muốn bỏ logging → phải sửa BidService
- Unit test BidService → phải mock tất cả 5 dependency trên
- Vi phạm **Single Responsibility Principle**

**Giải pháp: Observer Pattern** — BidService chỉ cần biết `publishEvent()`. Ai muốn nhận thì tự đăng ký.

---

## 2. OBSERVER PATTERN LÀ GÌ

Observer Pattern định nghĩa mối quan hệ **1 publisher → nhiều subscriber**:

```
Publisher (Subject)          Observers (Subscribers)
─────────────────           ──────────────────────
Biết có gì xảy ra           Biết phải làm gì khi nhận tin
Không cần biết ai nghe      Không cần biết có bao nhiêu observer khác
publishEvent(event)    ───► observer1.update(event)
                       ───► observer2.update(event)
                       ───► observer3.update(event)
```

**Hai phía hoàn toàn độc lập với nhau.**

---

## 3. SƠ ĐỒ TOÀN BỘ HỆ THỐNG OBSERVER

```
«interface»
AuctionObserver
└── update(AuctionEvent)
         ▲
         │ implements
    ┌────┴────────────────────────────────────────────────┐
    │           │              │           │              │
DataPersist  Frontend      Seller      Bidder          Admin
enceObserver Notification  Observer   Observer       Observer
             Observer
    │           │              │           │              │
    ▼           ▼              ▼           ▼              ▼
 DAO Layer  FrontendNotifier  FrontendNotifier      java.util
 (SQLite)   (ClientHandler)   (ClientHandler)        .logging
                │                   │
                └──── TCP Socket ────┘
                      → JavaFX Client


AuctionEventPublisher
├── observers: CopyOnWriteArrayList<AuctionObserver>
├── addObserver(observer)
├── removeObserver(observer)
└── publishEvent(event)
         │
         └──► for each observer → observer.update(event)


AuctionEvent                        BidEventPayload
├── eventType: AuctionEventType     ├── auction: Auction
├── auctionId: String               └── transaction: BidTransaction
├── message: String
├── createdAt: LocalDateTime
└── payload: Object
            ├── Auction (cho auction events)
            └── BidEventPayload (cho bid events)


AuctionEventType (enum)
├── AUCTION_CREATED
├── AUCTION_STARTED
├── AUCTION_FINISHED
├── AUCTION_CANCELLED
├── AUCTION_EXTENDED
├── AUCTION_UPDATED
├── NEW_BID
├── AUTO_BID_PLACED
├── AUTO_BID_REGISTERED
└── AUTO_BID_CANCELLED


AuctionEventSerializer (Utility)
└── toJson(event) → String JSON
                    ├── buildBidPayload(BidTransaction)    → {transactionId, bidderName, bidAmount, createdAt}
                    └── buildAuctionPayload(Auction)       → {currentHighestBid, status, scheduledEndTime, ...}


«interface»
FrontendNotifier
└── sendNotification(auctionId, jsonPayload)
         ▲
         │ implements
    ClientHandler (trong network layer)
    └── sendToClient(ServerMessage) — synchronized, ghi vào TCP socket
```

---

## 4. TỪNG CLASS — CODE THỰC TẾ + GIẢI THÍCH

### 4.1 `AuctionObserver` — Interface Gốc

```java
public interface AuctionObserver {
    void update(AuctionEvent event);
}
```

**Chỉ có 1 phương thức duy nhất.** Mọi observer đều triển khai phương thức này. Publisher không cần biết observer cụ thể là gì — chỉ cần biết nó có `update()`.

---

### 4.2 `AuctionEvent` — Gói Thông Tin Sự Kiện

```java
public class AuctionEvent {

    private final AuctionEventType eventType;  // Loại sự kiện (enum)
    private final String auctionId;            // Auction nào bị ảnh hưởng
    private final String message;              // Mô tả ngắn gọn
    private final LocalDateTime createdAt;     // Thời điểm xảy ra (auto-set)
    private final Object payload;              // Dữ liệu đính kèm (2 loại)
}
```

**Quy ước payload:**
- Nếu `eventType` là `AUCTION_*` → payload là đối tượng `Auction`
- Nếu `eventType` là `NEW_BID` hoặc `AUTO_BID_PLACED` → payload là `BidEventPayload`
- Các event khác → payload là `null`

**Tại sao `payload` là `Object` thay vì dùng generics `AuctionEvent<T>`?**

Dùng `Object` đơn giản hơn khi publish event. Nếu dùng generic thì phải khai báo:
```java
// Phức tạp khi có nhiều loại event:
AuctionEvent<Auction> auctionEvent;
AuctionEvent<BidEventPayload> bidEvent;
List<AuctionEvent<?>> events;  // wildcard khó xử lý

// Dùng Object đơn giản hơn:
AuctionEvent event = new AuctionEvent(type, id, msg, payload);
```

---

### 4.3 `AuctionEventPublisher` — Trái Tim Của Observer Pattern

```java
public class AuctionEventPublisher {

    // CopyOnWriteArrayList: thread-safe khi publish event đồng thời add/remove observer
    private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();

    public void addObserver(AuctionObserver observer) {
        if (observer == null) throw new AuctionException("Observer không được null.");
        observers.add(observer);
    }

    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    public void publishEvent(AuctionEvent event) {
        if (event == null) throw new AuctionException("Event không được null.");

        // Duyệt và gọi update() trên từng observer
        // CopyOnWriteArrayList đảm bảo không ConcurrentModificationException
        for (AuctionObserver observer : observers) {
            observer.update(event);
        }
    }

    public int getObserverCount() { return observers.size(); }
}
```

**Điểm quan trọng:** Publisher gọi observer theo thứ tự đăng ký, **tuần tự** (không phải song song). Nếu một observer ném exception → các observer sau không được gọi. Đây là trade-off chấp nhận được vì:
- Exception trong observer là lỗi lập trình, không phải runtime lỗi thông thường
- Tuần tự dễ debug hơn song song

---

### 4.4 `DataPersistenceObserver` — Lưu Xuống SQLite

```java
public class DataPersistenceObserver implements AuctionObserver {

    private final ItemDao itemDao;
    private final AuctionDao auctionDao;
    private final BidDao bidDao;

    @Override
    public void update(AuctionEvent event) {
        if (event == null) return;

        switch (event.getEventType()) {

            case AUCTION_CREATED:
                // Tạo mới: phải lưu cả item (trước) rồi mới lưu auction
                // (vì auction có FK → item)
                saveCreatedAuction(event);
                break;

            case AUCTION_FINISHED:
            case AUCTION_CANCELLED:
            case AUCTION_EXTENDED:
                // Trạng thái thay đổi → UPDATE bản ghi auction có sẵn
                updateAuction(event);
                break;

            case NEW_BID:
            case AUTO_BID_PLACED:
                // Bid mới: lưu transaction + cập nhật giá cao nhất trong auction
                saveBidAndUpdateAuction(event);
                break;

            // Những event sau không cần lưu DB:
            case AUTO_BID_REGISTERED:  // Lưu trong RAM (AutoBidManager)
            case AUTO_BID_CANCELLED:   // Xóa trong RAM
            case AUCTION_STARTED:      // Không có gì thay đổi cần persist
            default:
                break;
        }
    }

    private void saveCreatedAuction(AuctionEvent event) {
        Auction auction = extractAuction(event);
        itemDao.saveItem(auction.getItem());   // INSERT INTO items
        auctionDao.saveAuction(auction);       // INSERT INTO auctions
    }

    private void saveBidAndUpdateAuction(AuctionEvent event) {
        BidEventPayload payload = extractBidPayload(event);
        bidDao.saveBidTransaction(payload.getTransaction());  // INSERT INTO bid_transactions
        auctionDao.updateAuction(payload.getAuction());       // UPDATE auctions SET current_highest_bid...
    }
}
```

**Tại sao AUCTION_STARTED không cần persist?**
Vì khi tạo auction (`AUCTION_CREATED`), dữ liệu đã được lưu với `status = PENDING`. Khi bắt đầu, chỉ thay đổi `status` trong RAM (`Auction.start()`), nhưng DB vẫn đọc được trạng thái cuối cùng khi có event kết thúc. Hệ thống này ưu tiên hiệu năng: không ghi DB với mỗi trạng thái nhỏ.

---

### 4.5 `FrontendNotificationObserver` — Đẩy Cập Nhật Real-time

```java
public class FrontendNotificationObserver implements AuctionObserver {

    private final FrontendNotifier frontendNotifier;  // Cầu nối sang Network layer

    @Override
    public void update(AuctionEvent event) {
        if (event == null) return;

        // Bước 1: Chuyển AuctionEvent thành JSON string
        String json = AuctionEventSerializer.toJson(event);

        // Bước 2: Gửi qua FrontendNotifier (thực ra là ClientHandler ghi vào TCP socket)
        frontendNotifier.sendNotification(event.getAuctionId(), json);
    }
}
```

**Luồng đầy đủ của một lần update real-time:**
```
BidService.placeBid()
  → publishEvent(NEW_BID)
    → AuctionEventPublisher.publishEvent()
      → FrontendNotificationObserver.update(event)
        → AuctionEventSerializer.toJson(event)
             // Tạo JSON thủ công (không dùng Gson reflection để tránh circular ref):
             // {"eventType":"NEW_BID","auctionId":"A-xxx","payload":{"bidderName":"Alice","bidAmount":500}}
        → ClientHandler.sendNotification("A-xxx", json)
          → ServerMessage.builder().action("EVENT").message(json).build()
          → sendToClient(serverMessage)     ← synchronized!
            → writer.println(gson.toJson(serverMessage))
              → TCP Socket
                → NetworkClient.onMessageReceived()
                  → AuctionController.updateLabel()   ← Platform.runLater()
```

---

### 4.6 `SellerObserver` và `BidderObserver` — Thông Báo Cá Nhân

```java
public class SellerObserver implements AuctionObserver {

    private final String sellerName;           // Tên seller cụ thể
    private final FrontendNotifier frontendNotifier;  // Socket của seller đó

    @Override
    public void update(AuctionEvent event) {
        // Đóng gói thủ công thành JSON — không cần serialize toàn bộ Auction
        JsonObject json = new JsonObject();
        json.addProperty("recipient", sellerName);
        json.addProperty("eventType", event.getEventType().name());
        json.addProperty("auctionId", event.getAuctionId());
        json.addProperty("message", event.getMessage());
        json.addProperty("createdAt", event.getCreatedAt().toString());

        // Gửi chỉ đến ĐÚNG socket của seller này
        frontendNotifier.sendNotification(event.getAuctionId(), json.toString());
    }
}
```

**Điểm quan trọng:** Mỗi `SellerObserver` gắn với 1 người dùng cụ thể và 1 `FrontendNotifier` cụ thể (tức là 1 TCP socket cụ thể). Khi seller mở auction, hệ thống đăng ký observer này cho auction đó. Khi seller đóng app → `ClientHandler.cleanup()` xóa observer → không còn "ghost observer" chiếm bộ nhớ.

`BidderObserver` hoàn toàn tương tự `SellerObserver` — chỉ khác tên class và loại exception ném trong constructor.

---

### 4.7 `AdminObserver` — Logging Toàn Bộ

```java
public class AdminObserver implements AuctionObserver {

    private static final Logger logger = Logger.getLogger(AdminObserver.class.getName());
    private final String adminName;

    @Override
    public void update(AuctionEvent event) {
        // Ghi LOG mọi event không phân biệt loại
        logger.info("[ADMIN LOG] " + adminName + " ghi nhận event: " + event);
        // AuctionEvent.toString() → "AuctionEvent{eventType=NEW_BID, auctionId='A-xxx', ...}"
    }
}
```

**Đơn giản nhất trong 5 observer.** Không cần biết payload là gì, không cần phân loại event — chỉ log tất cả.

---

### 4.8 `AuctionEventSerializer` — Tránh Circular Reference

```java
public final class AuctionEventSerializer {

    // Utility class → private constructor, không instantiate
    private AuctionEventSerializer() {}

    public static String toJson(AuctionEvent event) {
        JsonObject root = new JsonObject();
        root.addProperty("eventType", event.getEventType().name());
        root.addProperty("auctionId", event.getAuctionId());
        root.addProperty("message", event.getMessage());
        root.addProperty("createdAt", event.getCreatedAt().toString());

        JsonObject payloadJson = buildPayload(event);
        root.add("payload", payloadJson != null ? payloadJson : JsonNull.INSTANCE);

        return root.toString();
    }

    // Serialize BidTransaction — CHỈ lấy các field cần thiết cho UI
    private static JsonObject buildBidPayload(BidTransaction tx) {
        JsonObject obj = new JsonObject();
        obj.addProperty("transactionId", tx.getTransactionId());
        obj.addProperty("bidderName", tx.getBidder().getName());  // Chỉ lấy tên, không serialize toàn bộ Bidder
        obj.addProperty("bidAmount", tx.getBidAmount());
        obj.addProperty("createdAt", tx.getCreatedAt().toString());
        return obj;
    }

    // Serialize Auction — CHỈ lấy các field trạng thái cần hiển thị
    private static JsonObject buildAuctionPayload(Auction auction) {
        JsonObject obj = new JsonObject();
        obj.addProperty("currentHighestBid", auction.getCurrentHighestBid());
        // Chỉ lấy tên bidder, không serialize toàn bộ Bidder object
        obj.addProperty("currentHighestBidder",
            auction.getCurrentHighestBidder() != null
                ? auction.getCurrentHighestBidder().getName()
                : null);
        obj.addProperty("status", auction.getStatus().name());
        if (auction.getScheduledEndTime() != null)
            obj.addProperty("scheduledEndTime", auction.getScheduledEndTime().toString());
        if (auction.getEndTime() != null)
            obj.addProperty("endTime", auction.getEndTime().toString());
        return obj;
    }
}
```

**Tại sao KHÔNG dùng `Gson.toJson(auction)` trực tiếp?**

```
Auction
├── seller: Seller
│   └── permissionStrategy: SellerPermission
│       └── (không serialize được)
├── item: AuctionItem
│   └── category: ItemCategory (enum)
├── lock: ReentrantLock   ← Gson crash ở đây (transient nhưng vẫn rủi ro)
└── currentHighestBidder: Bidder
    └── paymentStrategy: PaymentStrategy
        └── (có thể circular reference)
```

Serialize toàn bộ `Auction` qua Gson → có thể gây crash hoặc JSON rất lớn không cần thiết. `AuctionEventSerializer` **chọn lựa thủ công** chỉ những field UI cần — an toàn và hiệu quả hơn.

---

### 4.9 `FrontendNotifier` — Interface Tách Backend Khỏi Network

```java
public interface FrontendNotifier {
    void sendNotification(String auctionId, String jsonPayload);
}
```

**Đây là interface trong package `backend/observer/`**, nhưng được implement bởi `ClientHandler` trong package `network/`. Đây là kỹ thuật **Dependency Inversion**:

```
backend/observer/ (biết về interface)       network/ (biết về TCP)
────────────────────────────────────        ─────────────────────
FrontendNotifier (interface)                ClientHandler
                                              implements FrontendNotifier
                                              → sendNotification() ghi vào TCP
```

Backend (FrontendNotificationObserver) phụ thuộc vào **interface**, không phụ thuộc vào `ClientHandler` cụ thể. Điều này có nghĩa:
- Có thể test backend với `FrontendNotifier` giả (mock)
- Có thể thay TCP socket bằng WebSocket mà không sửa backend

---

## 5. LUỒNG SỰ KIỆN TỪ ĐẦU ĐẾN CUỐI

### Kịch bản: Alice đặt giá 500 vào auction "A-001"

```
[1] BidCommand.execute(ctx, message)
    │   auctionId="A-001", amount=500, userId="alice-id"
    │
    ▼
[2] BidService.placeBid(auction, alice, 500)
    │   [Lock auction]
    │   validate → OK
    │   previousHighestBidder = Bob (người đang giữ 450)
    │   auction.updateHighestBid(alice, 500)
    │   createTransaction(alice, "A-001", 500) → Transaction T-001
    │   bidHistoryManager.addTransaction(T-001)
    │   [Unlock]
    │
    ▼
[3] publishEvent(NEW_BID, "A-001", "Có bid mới.", BidEventPayload(auction, T-001))
    │
    ▼
[4] AuctionEventPublisher.publishEvent(event)
    │
    ├──[4a]── DataPersistenceObserver.update(event)
    │          extractBidPayload(event) → BidEventPayload
    │          bidDao.saveBidTransaction(T-001)         → INSERT INTO bid_transactions
    │          auctionDao.updateAuction(auction)         → UPDATE auctions SET current_highest_bid=500
    │
    ├──[4b]── FrontendNotificationObserver.update(event)
    │          AuctionEventSerializer.toJson(event)
    │            → {"eventType":"NEW_BID","auctionId":"A-001",
    │               "payload":{"bidderName":"Alice","bidAmount":500,...}}
    │          ClientHandler.sendNotification("A-001", json)
    │            → ServerMessage{action:"EVENT", auctionId:"A-001", message:json}
    │            → writer.println(gson.toJson(serverMessage))  ← TCP push đến TẤT CẢ người xem
    │
    ├──[4c]── SellerObserver.update(event)              ← Observer của người bán
    │          json = {recipient:"Charlie", eventType:"NEW_BID", ...}
    │          ClientHandler_Charlie.sendNotification("A-001", json) ← TCP push đến Charlie
    │
    ├──[4d]── BidderObserver.update(event)              ← Observer của Bob (bị vượt giá)
    │          json = {recipient:"Bob", eventType:"NEW_BID", message:"Bạn vừa bị vượt giá", ...}
    │          ClientHandler_Bob.sendNotification("A-001", json) ← TCP push đến Bob
    │
    └──[4e]── AdminObserver.update(event)
               logger.info("[ADMIN LOG] Admin ghi nhận event: AuctionEvent{...}")
    │
    ▼
[5] checkAndApplyAntiSnipe(auction)
    │   còn 25 giây → reschedule thêm 60 giây
    │   → publishEvent(AUCTION_EXTENDED, ...)
    │   → DataPersistenceObserver cập nhật DB
    │   → FrontendNotificationObserver push AUCTION_EXTENDED đến tất cả
    │
    ▼
[6] processAutoBids(auction, Bob)
    │   Bob có auto-bid: maxBid=600, increment=30
    │   → Bob auto-bid 530
    │   → publishEvent(AUTO_BID_PLACED, ...) × 1 lần
    │   → Alice bị vượt, Alice có auto-bid: maxBid=550, increment=10
    │   → Alice auto-bid 540
    │   → publishEvent(AUTO_BID_PLACED, ...) × 1 lần
    │   → Bob bị vượt, Bob auto-bid 570
    │   → Alice bị vượt, maxBid=550 < 570 → DỪNG
    │   → Bob thắng với 570
    │
    ▼
[7] return BidResult(T-001, [auto-bid transactions])
    → ClientHandler wrap vào ServerMessage → gửi về Alice
```

---

## 6. PAYLOAD: TẠI SAO CẦN 2 LOẠI KHÁC NHAU

### Loại 1: Payload = `Auction` (cho auction events)

Khi auction bắt đầu, kết thúc, gia hạn → observer cần **toàn bộ trạng thái auction** để cập nhật DB và thông báo UI.

```java
// Trong AuctionService.startAuction():
eventPublisher.publishEvent(
    new AuctionEvent(
        AuctionEventType.AUCTION_STARTED,
        auction.getAuctionId(),
        "Auction đã bắt đầu.",
        auction   // ← payload là Auction
    )
);

// DataPersistenceObserver nhận và làm gì?
case AUCTION_FINISHED:
    Auction auction = (Auction) event.getPayload();
    auctionDao.updateAuction(auction);
    // → UPDATE auctions SET status='FINISHED', end_time=... WHERE auction_id=...
```

### Loại 2: Payload = `BidEventPayload` (cho bid events)

Khi có bid → observer cần **cả 2**: auction (để cập nhật giá cao nhất) VÀ transaction (để lưu vào bid_transactions).

```java
// Tại sao cần BidEventPayload thay vì chỉ truyền Auction?
// Nếu chỉ truyền Auction → DataPersistenceObserver không biết transaction nào cần lưu!

// Trong BidService.placeBid():
eventPublisher.publishEvent(
    new AuctionEvent(
        AuctionEventType.NEW_BID,
        auction.getAuctionId(),
        "Có bid mới.",
        new BidEventPayload(auction, manualTransaction)  // ← cả 2 trong 1 gói
    )
);

// DataPersistenceObserver nhận và làm gì?
case NEW_BID:
    BidEventPayload payload = (BidEventPayload) event.getPayload();
    bidDao.saveBidTransaction(payload.getTransaction());  // Lưu transaction
    auctionDao.updateAuction(payload.getAuction());       // Cập nhật giá
```

**Tại sao `BidEventPayload` là class riêng, không dùng Map<String, Object>?**

```java
// Cách tệ:
Map<String, Object> payload = new HashMap<>();
payload.put("auction", auction);
payload.put("transaction", transaction);
event.setPayload(payload);
// Observer phải cast thủ công, không có type check tại compile time

// Cách tốt (đang dùng):
BidEventPayload payload = new BidEventPayload(auction, transaction);
// Có getter rõ ràng: payload.getAuction(), payload.getTransaction()
// Compile-time type safety
```

---

## 7. FRONTENDNOTIFIER — CẦU NỐI BACKEND VỚI NETWORK

Đây là thiết kế thú vị nhất trong hệ thống. Hãy xem tại sao cần `FrontendNotifier`:

### Vấn đề: Backend không được biết về Network

```
Package backend/observer/ ──────────────► Package network/
                              import?        ClientHandler
                           KHÔNG ĐƯỢC!
```

Backend là **lõi nghiệp vụ** — không được phụ thuộc vào cách gửi dữ liệu (TCP, WebSocket, REST, v.v.). Nếu phụ thuộc thẳng vào `ClientHandler` → backend và network bị kết dính chặt với nhau.

### Giải pháp: Interface trong backend, implementation trong network

```
backend/observer/                          network/
─────────────────                          ───────
FrontendNotifier (interface)               ClientHandler
void sendNotification(id, json)              implements FrontendNotifier
                                             @Override
                                             void sendNotification(id, json) {
                                                 // Ghi vào TCP socket
                                                 writer.println(json);
                                             }
```

```java
// Cách đăng ký trong CommandContext:
FrontendNotificationObserver observer =
    new FrontendNotificationObserver(clientHandler);  // clientHandler là FrontendNotifier
eventPublisher.addObserver(observer);

// Khi có event → observer.update() → clientHandler.sendNotification() → TCP socket
```

**Tại sao `sendToClient()` trong ClientHandler là `synchronized`?**

```java
private synchronized void sendToClient(ServerMessage message) {
    if (writer != null && !clientSocket.isClosed()) {
        writer.println(gson.toJson(message));
    }
}
```

`PrintWriter` không thread-safe. Nhiều observer có thể gọi `sendNotification()` đồng thời (ví dụ: cùng lúc `FrontendNotificationObserver` và `SellerObserver` đều push đến cùng 1 client). Nếu không synchronized → 2 JSON string lẫn vào nhau trên socket → client parse lỗi.

---

## 8. THREAD-SAFETY TRONG OBSERVER

### Vấn đề: Publish event đồng thời add/remove observer

```
Thread A (BidService):                Thread B (ClientHandler):
publishEvent(NEW_BID)                 addObserver(newObserver)
  → for (observer : observers)        observers.add(newObserver)
      observer.update(event)
```

Nếu `observers` là `ArrayList` thông thường → `ConcurrentModificationException`.

### Giải pháp: `CopyOnWriteArrayList`

```java
private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();
```

**Cơ chế hoạt động:**

```
Trước khi write (add/remove):
  CopyOnWriteArrayList tạo một bản SAO của mảng hiện tại
  → Thread đang iterate vẫn thấy mảng CŨ (không bị ảnh hưởng)
  → Thread ghi vào mảng MỚI
  → Sau khi ghi xong, swap mảng mới vào
```

```
observers (trước):  [Observer1, Observer2, Observer3]
                           ↑
                    Thread A đang iterate ở đây

Thread B: add(Observer4)
  → tạo copy: [Observer1, Observer2, Observer3, Observer4]
  → Thread A vẫn thấy [Observer1, Observer2, Observer3]
  → Thread A finish iterate → OK
  → Lần iterate sau: thấy [Observer1, Observer2, Observer3, Observer4]
```

**Trade-off:**
- Write tốn kém: copy toàn bộ mảng → O(n)
- Read/iterate rất nhanh: không lock → O(1)
- Phù hợp: observers ít khi thay đổi, nhưng publish event liên tục

---

## 9. TẠI SAO THIẾT KẾ NHƯ VẬY — SO SÁNH PHƯƠNG ÁN

### So sánh 3 cách xử lý "khi có bid mới"

**Phương án 1: Gọi trực tiếp (KHÔNG dùng Observer)**
```java
// BidService:
public BidResult placeBid(...) {
    // ... logic ...
    bidDao.saveBid(tx);
    auctionDao.update(auction);
    clientHandler.send(json);
    logger.info(...);
    emailService.send(...);
    return result;
}
```
❌ BidService biết về DAO, network, logger, email  
❌ Thêm tính năng → sửa BidService  
❌ Unit test phải mock 5+ dependency  
❌ Vi phạm Single Responsibility, Open/Closed Principle

---

**Phương án 2: Event Queue bất đồng bộ (Kafka/RabbitMQ style)**
```java
// BidService:
queue.publish(new BidEvent(tx, auction));  // không chờ

// Workers chạy song song xử lý:
Worker1: lưu DB
Worker2: gửi notification
Worker3: gửi email
```
✅ BidService hoàn toàn tách biệt  
❌ Quá phức tạp cho bài toán này  
❌ Ordering không đảm bảo (DB lưu trước hay notification đến trước?)  
❌ Khó debug  
❌ Cần infrastructure thêm (queue server)

---

**Phương án 3: Observer Pattern đồng bộ (đang dùng)**
```java
// BidService:
eventPublisher.publishEvent(new AuctionEvent(NEW_BID, ...));
// Chờ tất cả observer xử lý xong mới return
```
✅ BidService chỉ biết về Publisher  
✅ Thêm tính năng: chỉ thêm Observer mới  
✅ Đảm bảo thứ tự: DB lưu xong rồi mới notification  
✅ Đơn giản, dễ debug  
✅ Unit test: mock Publisher hoặc kiểm tra observer riêng lẻ  
⚠️ Đồng bộ: observer nặng (I/O) làm chậm luồng chính  
⚠️ Exception trong 1 observer → các observer sau không chạy  

→ **Phù hợp cho hệ thống đấu giá** quy mô vừa, nơi tính nhất quán (consistency) quan trọng hơn throughput tuyệt đối.

---

### Tóm Tắt Hệ Thống Observer Trong Dự Án

| Class | Vai trò | Nhận event nào | Làm gì |
|---|---|---|---|
| `AuctionEventPublisher` | Điều phối | — | Gọi `update()` trên từng observer |
| `DataPersistenceObserver` | Persistence | CREATED, FINISHED, CANCELLED, EXTENDED, NEW_BID, AUTO_BID | INSERT/UPDATE SQLite |
| `FrontendNotificationObserver` | Real-time UI | Tất cả | Serialize JSON → push qua TCP |
| `SellerObserver` | Cá nhân hóa | Tất cả | Thông báo đến socket của Seller cụ thể |
| `BidderObserver` | Cá nhân hóa | Tất cả | Thông báo đến socket của Bidder cụ thể |
| `AdminObserver` | Monitoring | Tất cả | Ghi log ra java.util.logging |
| `AuctionEventSerializer` | Utility | — | `AuctionEvent` → JSON string an toàn |
| `FrontendNotifier` | Contract | — | Interface tách backend khỏi network |
| `BidEventPayload` | Data carrier | — | Gói auction + transaction lại cho bid events |
