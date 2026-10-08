package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import model.Booking;

public class BookingDAO {

    public List<String> bookedSeats(int showId) throws SQLException {
        List<String> seats = new ArrayList<>();
        String sql = "SELECT seats FROM bookings WHERE show_id = ? AND status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, showId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String seatStr = rs.getString(1);
                    if (seatStr != null && !seatStr.isBlank()) {
                        seats.addAll(Arrays.asList(seatStr.split(",")));
                    }
                }
            }
        }
        return seats;
    }

    public Booking create(Booking booking, String paymentMethod) throws SQLException {
        String sqlBooking = "INSERT INTO bookings(user_id, movie_id, show_id, seats, total_amount, status) VALUES(?, ?, ?, ?, ?, 'CONFIRMED')";
        String sqlPayment = "INSERT INTO payments(booking_id, amount, payment_method, payment_status) VALUES(?, ?, ?, 'SUCCESS')";
        
        try (Connection conn = DatabaseConnection.open()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, booking.getUserId());
                pstmt.setInt(2, booking.getMovieId());
                pstmt.setInt(3, booking.getShowId());
                pstmt.setString(4, String.join(",", booking.getSeatNumbers()));
                pstmt.setDouble(5, booking.getTotalAmount());
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int bookingId = keys.getInt(1);
                        try (PreparedStatement payStmt = conn.prepareStatement(sqlPayment)) {
                            payStmt.setInt(1, bookingId);
                            payStmt.setDouble(2, booking.getTotalAmount());
                            payStmt.setString(3, paymentMethod);
                            payStmt.executeUpdate();
                        }
                        conn.commit();
                        return new Booking(
                            bookingId,
                            booking.getUserId(),
                            booking.getMovieId(),
                            booking.getShowId(),
                            booking.getSeatNumbers(),
                            booking.getTotalAmount(),
                            "CONFIRMED",
                            LocalDateTime.now()
                        );
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        throw new SQLException("Failed to record booking.");
    }

    public List<Booking> findByUser(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(makeBooking(rs));
                }
            }
        }
        return list;
    }

    public List<Booking> findAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(makeBooking(rs));
            }
        }
        return list;
    }

    public boolean cancel(int bookingId) throws SQLException {
        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ? AND status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean restoreBooking(int bookingId) throws SQLException {
        String sql = "UPDATE bookings SET status = 'CONFIRMED' WHERE id = ? AND status = 'CANCELLED'";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return makeBooking(rs);
                }
            }
        }
        return null;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM bookings WHERE status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public double getTodayRevenue() throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM bookings WHERE status = 'CONFIRMED' AND DATE(created_at) = DATE('now')";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public int getConfirmedCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getTodayBookingsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE DATE(created_at) = DATE('now')";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getCancelledCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = 'CANCELLED'";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public String getMostPopularMovie() throws SQLException {
        String sql = """
            SELECT m.title, COUNT(b.id) as ticket_count 
            FROM bookings b 
            JOIN movies m ON b.movie_id = m.id 
            WHERE b.status = 'CONFIRMED' 
            GROUP BY m.id 
            ORDER BY ticket_count DESC 
            LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) + " (" + rs.getInt(2) + " bookings)" : "N/A";
        }
    }

    private Booking makeBooking(ResultSet rs) throws SQLException {
        String seatsStr = rs.getString("seats");
        List<String> seatList = (seatsStr != null && !seatsStr.isBlank())
            ? Arrays.asList(seatsStr.split(","))
            : Collections.emptyList();

        return new Booking(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("movie_id"),
            rs.getInt("show_id"),
            seatList,
            rs.getDouble("total_amount"),
            rs.getString("status"),
            LocalDateTime.now()
        );
    }
}