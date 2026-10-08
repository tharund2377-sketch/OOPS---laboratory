package model;

public class CashPayment extends Payment {

    public CashPayment(int id, int bookingId, double amount, String paymentStatus) {
        super(id, bookingId, amount, paymentStatus);
    }

    @Override
    public String getPaymentType() {
        return "CASH AT COUNTER";
    }
}
