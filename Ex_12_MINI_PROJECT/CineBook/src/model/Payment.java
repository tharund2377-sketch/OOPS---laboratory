package model;

public abstract class Payment implements Payable {

    private final int id;
    private final int bookingId;
    private final double amount;
    private final String paymentStatus;

    public Payment(int id, int bookingId, double amount, String paymentStatus) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    public int getId() {
        return id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    @Override
    public boolean processPayment() {
        return "SUCCESS".equalsIgnoreCase(paymentStatus);
    }
}