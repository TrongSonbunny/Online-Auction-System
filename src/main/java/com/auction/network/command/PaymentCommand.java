package com.auction.network.command;

import com.auction.backend.payment.PaymentLogger;
import com.auction.backend.payment.PaymentProcessor;
import com.auction.backend.payment.PaymentValidator;
import com.auction.exceptions.PaymentException;
import com.auction.exceptions.UnauthorizedException;
import com.auction.models.auction.Auction;
import com.auction.models.auction.AuctionStatus;
import com.auction.models.payment.BankPayment;
import com.auction.models.payment.MomoPayment;
import com.auction.models.payment.PaymentStrategy;
import com.auction.models.payment.VnPayPayment;
import com.auction.models.user.Bidder;
import com.auction.models.user.User;
import com.auction.network.ClientMessage;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Command xử lý thanh toán cho người chiến thắng một phiên đấu giá.
 *
 * <p>Tích hợp toàn bộ tầng payment đã được code sẵn:
 * {@link PaymentValidator} (kiểm tra số tiền), {@link PaymentProcessor}
 * (facade gọi {@link PaymentStrategy}: Momo/Bank/VnPay theo lựa chọn của người
 * dùng) và {@link PaymentLogger} (ghi log kết quả).
 *
 * <p>Số tiền KHÔNG lấy từ client mà tính lại từ giá chốt của phiên trên server
 * để tránh gian lận. Chỉ người thắng của phiên đã {@code FINISHED} mới được trả.
 */
public class PaymentCommand extends BaseClientCommand {

  /**
   * Constructor payment command.
   *
   * @param context command context
   */
  public PaymentCommand(CommandContext context) {
    super(context);
  }

  @Override
  public Object execute(ClientMessage message) {

    User user = getRequiredUser(message.getUserId());

    if (!(user instanceof Bidder)) {
      throw new UnauthorizedException("Chỉ người mua (Bidder) mới được thanh toán.");
    }

    Auction auction = getRequiredAuction(message.getAuctionId());

    if (auction.getStatus() != AuctionStatus.FINISHED) {
      throw new PaymentException("Chỉ thanh toán được khi phiên đã kết thúc.");
    }

    Bidder winner = auction.getCurrentHighestBidder();
    if (winner == null) {
      throw new PaymentException("Phiên này không có người thắng để thanh toán.");
    }

    if (!winner.getUserId().equals(user.getUserId())) {
      throw new UnauthorizedException("Bạn không phải người thắng phiên này.");
    }

    double amount = auction.getCurrentHighestBid();
    PaymentStrategy strategy = resolveStrategy(message.getPaymentMethod(), (Bidder) user);

    PaymentValidator validator = context.getPaymentValidator();
    PaymentProcessor processor = context.getPaymentProcessor();
    PaymentLogger paymentLogger = context.getPaymentLogger();

    validator.validatePaymentAmount(amount);

    boolean success;
    try {
      success = processor.processPayment(strategy, amount);
    } catch (RuntimeException ex) {
      paymentLogger.logPaymentFailure(strategy.getPaymentMethodName(), ex.getMessage());
      throw ex;
    }

    if (success) {
      paymentLogger.logPaymentSuccess(strategy.getPaymentMethodName(), amount);
    } else {
      paymentLogger.logPaymentFailure(strategy.getPaymentMethodName(), "Cổng thanh toán từ chối.");
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("success", success);
    result.put("auctionId", auction.getAuctionId());
    result.put("itemName", auction.getItem() != null ? auction.getItem().getName() : "");
    result.put("method", strategy.getPaymentMethodName());
    result.put("amount", amount);
    result.put(
        "message",
        success
            ? "Thanh toán thành công " + String.format(Locale.US, "%,.0f VNĐ", amount)
                + " qua " + strategy.getPaymentMethodName() + "."
            : "Thanh toán thất bại. Vui lòng thử lại.");
    return result;
  }

  /**
   * Chọn {@link PaymentStrategy} theo lựa chọn của người dùng.
   * Thông tin tài khoản dựng từ hồ sơ người thắng để mọi strategy validate hợp lệ.
   *
   * @param method  phương thức (MOMO/BANK/VNPAY); null hoặc lạ → mặc định Momo
   * @param winner  người thắng (để lấy tên/email dựng thông tin tài khoản)
   * @return strategy tương ứng
   */
  private PaymentStrategy resolveStrategy(String method, Bidder winner) {
    String normalized = method == null ? "MOMO" : method.trim().toUpperCase(Locale.ROOT);
    return switch (normalized) {
      case "BANK" -> new BankPayment("Auction Bank", "ACC-" + winner.getUserId(), winner.getName());
      case "VNPAY" -> new VnPayPayment(winner.getEmail());
      default -> new MomoPayment("0000000000", winner.getName());
    };
  }
}
