package com.auction.models.payment;

/**
 * Payment strategy cho ngân hàng.
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

  private void validateAmount(double amount) {

    if (amount <= 0) {
      throw new IllegalArgumentException(
          "Số tiền phải lớn hơn 0.");
    }
  }

  private void validateBankName(String name) {

    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException(
          "Tên ngân hàng không hợp lệ.");
    }
  }

  private void validateAccountNumber(String accountNumber) {

    if (accountNumber == null
        || accountNumber.isBlank()) {

      throw new IllegalArgumentException(
          "Số tài khoản không hợp lệ.");
    }
  }

  private void validateHolderName(String holderName) {

    if (holderName == null
        || holderName.isBlank()) {

      throw new IllegalArgumentException(
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