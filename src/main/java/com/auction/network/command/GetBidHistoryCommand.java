package com.auction.network.command;

import com.auction.models.bid.BidTransaction;
import com.auction.network.ClientMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command xử lý việc lấy lịch sử trả giá của một phiên đấu giá cụ thể.
 * Tuân thủ Google Checkstyle và đồng bộ hóa cấu trúc dữ liệu phẳng (DTO).
 */
public class GetBidHistoryCommand extends BaseClientCommand {

  /**
   * Constructor.
   *
   * @param context command context
   */
  public GetBidHistoryCommand(CommandContext context) {
    super(context);
  }

  /**
   * Trả về danh sách dữ liệu lịch sử tinh giản thuộc về auctionId được yêu cầu.
   * Tránh lỗi circular reference và cấu trúc lồng phức tạp của đối tượng liên kết.
   *
   * @param message dữ liệu client gửi lên chứa auctionId
   * @return JsonArray chứa chuỗi dữ liệu lịch sử thô đã làm phẳng
   */
  @Override
  public Object execute(ClientMessage message) {
    String targetAuctionId = message.getAuctionId();
    if (targetAuctionId == null || targetAuctionId.isBlank()) {
      return new JsonArray();
    }

    // Lọc lấy các giao dịch đúng của phiên đấu giá này từ RAM
    List<BidTransaction> history = context.getBidHistoryManager().getBidHistory().stream()
        .filter(t -> targetAuctionId.equals(t.getAuctionId()))
        .collect(Collectors.toList());

    JsonArray resultArray = new JsonArray();

    // Tạo cấu trúc phẳng (Flat DTO) để truyền tải an toàn qua Socket mạng TCP
    for (BidTransaction tx : history) {
      if (tx == null) {
        continue;
      }
      JsonObject jsonTx = new JsonObject();
      jsonTx.addProperty("transactionId", tx.getTransactionId());
      jsonTx.addProperty("bidAmount", tx.getBidAmount());
      jsonTx.addProperty("bidderName",
          tx.getBidder() != null ? tx.getBidder().getName() : "Ẩn danh");

      // Định dạng ngày giờ thành String để Gson không bao giờ lỗi phân tích cú pháp LocalDateTime
      if (tx.getCreatedAt() != null) {
        jsonTx.addProperty("createdAt",
            tx.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      }

      resultArray.add(jsonTx);
    }

    return resultArray;
  }
}