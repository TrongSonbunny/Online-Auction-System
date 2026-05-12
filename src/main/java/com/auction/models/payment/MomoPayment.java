package com.auction.models.payment;

import com.auction.exceptions.PaymentException;

/**
 * Payment strategy cho ví Momo.
 */
public class MomoPayment implements PaymentStrategy {

  private final String phoneNumber;

  private final String walletOwnerName;

  /**
   * Constructor momo payment.
   *
   * @param phoneNumber số điện thoại momo
   * @param walletOwnerName tên chủ ví
   */
  public MomoPayment(
      String phoneNumber,
      String walletOwnerName) {

    validatePhoneNumber(phoneNumber);
    validateWalletOwnerName(walletOwnerName);

    this.phoneNumber = phoneNumber;
    this.walletOwnerName = walletOwnerName;
  }

  @Override
  public boolean pay(double amount) {

    validateAmount(amount);

    System.out.println(
        "Thanh toán "
            + amount
            + " bằng ví Momo.");

    return true;
  }

  @Override
  public boolean refund(double amount) {

    validateAmount(amount);

    System.out.println(
        "Hoàn tiền "
            + amount
            + " về ví Momo.");

    return true;
  }

  @Override
  public String getPaymentMethodName() {
    return "Momo Payment";
  }

  private void validateAmount(double amount) {

    if (amount <= 0) {
      throw new PaymentException(
          "Số tiền phải lớn hơn 0.");
    }
  }

  private void validatePhoneNumber(String number) {

    if (number == null
        || number.isBlank()
        || number.length() < 10) {

      throw new PaymentException(
          "Số điện thoại không hợp lệ.");
    }
  }

  private void validateWalletOwnerName(String name) {

    if (name == null || name.isBlank()) {
      throw new PaymentException(
          "Tên chủ ví không hợp lệ.");
    }
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getWalletOwnerName() {
    return walletOwnerName;
  }

  @Override
  public String toString() {

    return "MomoPayment{"
        + "phoneNumber='"
        + phoneNumber
        + '\''
        + ", walletOwnerName='"
        + walletOwnerName
        + '\''
        + '}';
  }
}