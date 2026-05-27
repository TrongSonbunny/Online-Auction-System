package com.auction.models.payment;

import com.auction.exceptions.PaymentException;
import java.util.logging.Logger;

/**
 * Payment strategy thanh toán qua ví điện tử Momo.
 *
 * <p>Yêu cầu số điện thoại tối thiểu 10 ký tự và tên chủ ví.
 * {@code pay()} và {@code refund()} hiện tại ghi log ra console.
 */
public class MomoPayment implements PaymentStrategy {

  private static final Logger logger =
      Logger.getLogger(
          MomoPayment.class.getName());

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

  /**
   * Thực hiện thanh toán qua ví Momo.
   *
   * @param amount số tiền thanh toán (phải > 0)
   * @return true nếu thành công
   */
  @Override
  public boolean pay(double amount) {

    validateAmount(amount);

    logger.info(
        "Thanh toán "
            + amount
            + " bằng ví Momo.");

    return true;
  }

  /**
   * Hoàn tiền về ví Momo.
   *
   * @param amount số tiền hoàn (phải > 0)
   * @return true nếu thành công
   */
  @Override
  public boolean refund(double amount) {

    validateAmount(amount);

    logger.info(
        "Hoàn tiền "
            + amount
            + " về ví Momo.");

    return true;
  }

  @Override
  public String getPaymentMethodName() {
    return "Momo Payment";
  }

  /**
   * Validate số tiền phải lớn hơn 0.
   *
   * @param amount số tiền
   */
  private void validateAmount(double amount) {

    if (amount <= 0) {
      throw new PaymentException(
          "Số tiền phải lớn hơn 0.");
    }
  }

  /**
   * Validate số điện thoại không null, không blank và tối thiểu 10 ký tự.
   *
   * @param number số điện thoại
   */
  private void validatePhoneNumber(String number) {

    if (number == null
        || number.isBlank()
        || number.length() < 10) {

      throw new PaymentException(
          "Số điện thoại không hợp lệ.");
    }
  }

  /**
   * Validate tên chủ ví không được null hoặc blank.
   *
   * @param name tên chủ ví
   */
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