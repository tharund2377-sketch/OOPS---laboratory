package model;

import java.time.LocalDateTime;
import java.util.List;

public class Booking {

    private final int id;
    private final int userId;
    private final int movieId;
    private final int showId;
    private final List<String> seatNumbers;
    private final double totalAmount;
    private final String status;
    private final LocalDateTime bookingDate;

    public Booking(int id, int userId, int movieId, int showId, List<String> seatNumbers, double totalAmount, String status, LocalDateTime bookingDate) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.showId = showId;
        this.seatNumbers = seatNumbers;
        this.totalAmount = totalAmount;
        this.status = status;
        this.bookingDate = bookingDate;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public int getShowId() {
        return showId;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }
}