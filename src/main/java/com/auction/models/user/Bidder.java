package com.auction.models.user;

import com.auction.models.payment.PaymentStrategy;
import com.auction.models.user.permission.BidderPermission;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User đặt giá (bidder) trong hệ thống đấu giá.
 *
 * <p>
 * Có thêm {@link PaymentStrategy} để xử lý thanh toán và bộ đếm
 * {@link java.util.concurrent.atomic.AtomicInteger} đảm bảo thread-safe
 * khi nhiều thread cùng ghi nhận bid thành công.
 */
public class Bidder extends User implements Serializable {

  private static final long serialVersionUID = 1L;

  // FIX LỖI: Thêm transient vì PaymentStrategy là logic, không thể gửi qua Socket
  private transient PaymentStrategy paymentStrategy;

  private final AtomicInteger totalBidsPlaced;

  /**
   * Constructor bidder.
   *
   * @param userId          mã bidder
   * @param name            tên bidder
   * @param email           email bidder
   * @param paymentStrategy chiến lược thanh toán
   */
  public Bidder(
      String userId,
      String name,
      String email,
      PaymentStrategy paymentStrategy) {

    super(
        userId,
        name,
        email,
        UserRole.BIDDER,
        new BidderPermission());

    this.paymentStrategy = Objects.requireNonNull(
        paymentStrategy,
        "Payment strategy không được null.");

    this.totalBidsPlaced = new AtomicInteger(0);
  }

  /**
   * Tăng số lần bid.
   */
  public void incrementTotalBidsPlaced() {
    totalBidsPlaced.incrementAndGet();
  }

  public PaymentStrategy getPaymentStrategy() {
    return paymentStrategy;
  }

  /**
   * Cập nhật payment strategy.
   *
   * @param newStrategy strategy mới
   */
  public void setPaymentStrategy(
      PaymentStrategy newStrategy) {

    this.paymentStrategy = Objects.requireNonNull(
        newStrategy,
        "Payment strategy không được null.");
  }

  public int getTotalBidsPlaced() {
    return totalBidsPlaced.get();
  }

  @Override
  public String toString() {

    return "Bidder{"
        + "totalBidsPlaced="
        + totalBidsPlaced
        + "} "
        + super.toString();
  }
}