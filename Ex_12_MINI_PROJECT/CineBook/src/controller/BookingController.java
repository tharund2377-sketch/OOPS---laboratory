package controller;

import dao.BookingDAO;
import dao.ShowDAO;
import dsa.CancellationStack;
import model.Booking;
import model.Movie;
import model.Show;
import model.User;
import service.BookingService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingController {

    private final ShowDAO showDAO = new ShowDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final BookingService bookingService = new BookingService();
    private final CancellationStack cancellationStack = new CancellationStack();

    public List<Show> shows(Movie movie) throws SQLException {
        return showDAO.findByMovie(movie.getId());
    }

    public Booking book(User user, Movie movie, Show show, List<String> seats, String paymentMethod) throws SQLException {
        return bookingService.book(user, movie, show, seats, paymentMethod);
    }

    public boolean cancelBooking(int bookingId) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking != null && bookingDAO.cancel(bookingId)) {
            cancellationStack.push(booking);
            return true;
        }
        return false;
    }

    public boolean undoLastCancellation() throws SQLException {
        if (!cancellationStack.isEmpty()) {
            Booking lastCancelled = cancellationStack.pop();
            return bookingDAO.restoreBooking(lastCancelled.getId());
        }
        return false;
    }

    public boolean hasCancelledStack() {
        return !cancellationStack.isEmpty();
    }

    public List<Booking> userBookings(int userId) throws SQLException {
        return bookingDAO.findByUser(userId);
    }

    public Map<String, Object> getUserProfileStats(int userId) throws SQLException {
        List<Booking> list = bookingDAO.findByUser(userId);
        int totalTickets = 0;
        double totalSpent = 0.0;
        int activeCount = 0;

        for (Booking b : list) {
            if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                totalTickets += b.getSeatNumbers().size();
                totalSpent += b.getTotalAmount();
                activeCount++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", list.size());
        stats.put("activeBookings", activeCount);
        stats.put("totalTickets", totalTickets);
        stats.put("totalSpent", totalSpent);
        return stats;
    }
}