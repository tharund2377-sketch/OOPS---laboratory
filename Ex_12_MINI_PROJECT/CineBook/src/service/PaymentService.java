package service;

import model.CashPayment;
import model.OnlinePayment;
import model.Payment;

public class PaymentService {

    public Payment process(int bookingId, double amount, String method) {
        if (method != null && method.toLowerCase().contains("cash")) {
            return new CashPayment(0, bookingId, amount, "SUCCESS");
        } else {
            return new OnlinePayment(0, bookingId, amount, method == null ? "UPI" : method, "SUCCESS");
        }
    }
}