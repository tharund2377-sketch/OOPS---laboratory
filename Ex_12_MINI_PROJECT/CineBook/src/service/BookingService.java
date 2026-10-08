package service;

import dao.BookingDAO;
import dsa.BookingQueue;
import model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingQueue bookingQueue = new BookingQueue();
    private final PaymentService paymentService = new PaymentService();

    public Booking book(User user, Movie movie, Show show, List<String> seats, String paymentMethod) throws SQLException {
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one seat.");
        }

        List<String> takenSeats = bookingDAO.bookedSeats(show.getId());
        for (String seat : seats) {
            if (takenSeats.contains(seat)) {
                throw new IllegalArgumentException("Seat " + seat + " is already booked for this show.");
            }
        }

        double totalAmount = seats.size() * 220.0;

        Payment payment = paymentService.process(0, totalAmount, paymentMethod);
        if (!"SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new IllegalStateException("Payment failed.");
        }

        Booking bookingRequest = new Booking(
            0,
            user.getId(),
            movie.getId(),
            show.getId(),
            seats,
            totalAmount,
            "PENDING",
            LocalDateTime.now()
        );

        bookingQueue.enqueue(bookingRequest);
        Booking nextInQueue = bookingQueue.processNext();

        return bookingDAO.create(nextInQueue, paymentMethod);
    }
}