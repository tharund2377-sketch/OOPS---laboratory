package model;

public class OnlinePayment extends Payment {

    private final String paymentGateway; // UPI or Card

    public OnlinePayment(int id, int bookingId, double amount, String paymentGateway, String paymentStatus) {
        super(id, bookingId, amount, paymentStatus);
        this.paymentGateway = paymentGateway;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    @Override
    public String getPaymentType() {
        return "ONLINE (" + paymentGateway + ")";
    }
}
