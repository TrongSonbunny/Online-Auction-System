package com.auction.models.payment;

import com.auction.exceptions.PaymentException;

/**
 * Payment strategy thanh toán qua tài khoản ngân hàng.
 *
 * <p>Implement {@link PaymentStrategy} với thông tin: tên ngân hàng,
 * số tài khoản và tên chủ tài khoản. Thực hiện {@code pay()} và {@code refund()}
 * bằng cách ghi log ra console (integration thực tế do team khác implement).
 */
public class BankPayment implements PaymentStrategy {

  private final String bankName;

  private final String bankAccountNumber;

  private final String accountHolderName;

  /**
   * Constructor bank payment.
   *
   * @param bankName tên ngân hàng
   * @param bankAccountNumber số tài khoản
   * @param accountHolderName tên chủ tài khoản
   */
  public BankPayment(
      String bankName,
      String bankAccountNumber,
      String accountHolderName) {

    validateBankName(bankName);
    validateAccountNumber(bankAccountNumber);
    validateHolderName(accountHolderName);

    this.bankName = bankName;
    this.bankAccountNumber = bankAccountNumber;
    this.accountHolderName = accountHolderName;
  }

  /**
   * Thực hiện thanh toán qua tài khoản ngân hàng.
   *
   * @param amount số tiền thanh toán (phải > 0)
   * @return true nếu thành công
   */
  @Override
  public boolean pay(double amount) {

    validateAmount(amount);

    System.out.println(
        "Thanh toán "
            + amount
            + " bằng ngân hàng "
            + bankName);

    return true;
  }

  /**
   * Hoàn tiền về tài khoản ngân hàng.
   *
   * @param amount số tiền hoàn (phải > 0)
   * @return true nếu thành công
   */
  @Override
  public boolean refund(double amount) {

    validateAmount(amount);

    System.out.println(
        "Hoàn tiền "
            + amount
            + " về tài khoản ngân hàng.");

    return true;
  }

  @Override
  public String getPaymentMethodName() {
    return "Bank Payment";
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
   * Validate tên ngân hàng không được null hoặc blank.
   *
   * @param name tên ngân hàng
   */
  private void validateBankName(String name) {

    if (name == null || name.isBlank()) {
      throw new PaymentException(
          "Tên ngân hàng không hợp lệ.");
    }
  }

  /**
   * Validate số tài khoản không được null hoặc blank.
   *
   * @param accountNumber số tài khoản
   */
  private void validateAccountNumber(String accountNumber) {

    if (accountNumber == null
        || accountNumber.isBlank()) {

      throw new PaymentException(
          "Số tài khoản không hợp lệ.");
    }
  }

  /**
   * Validate tên chủ tài khoản không được null hoặc blank.
   *
   * @param holderName tên chủ tài khoản
   */
  private void validateHolderName(String holderName) {

    if (holderName == null
        || holderName.isBlank()) {

      throw new PaymentException(
          "Tên chủ tài khoản không hợp lệ.");
    }
  }

  public String getBankName() {
    return bankName;
  }

  public String getBankAccountNumber() {
    return bankAccountNumber;
  }

  public String getAccountHolderName() {
    return accountHolderName;
  }

  @Override
  public String toString() {

    return "BankPayment{"
        + "bankName='"
        + bankName
        + '\''
        + ", bankAccountNumber='"
        + bankAccountNumber
        + '\''
        + ", accountHolderName='"
        + accountHolderName
        + '\''
        + '}';
  }
}