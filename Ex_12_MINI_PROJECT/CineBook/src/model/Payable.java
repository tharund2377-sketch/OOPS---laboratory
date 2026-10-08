package model;

public interface Payable {
    boolean processPayment();
    String getPaymentType();
}
