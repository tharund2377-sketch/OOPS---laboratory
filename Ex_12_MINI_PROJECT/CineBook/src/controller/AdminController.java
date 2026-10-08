package controller;

import dao.*;
import model.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {

    private final MovieDAO movieDAO = new MovieDAO();
    private final ShowDAO showDAO = new ShowDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TheatreDAO theatreDAO = new TheatreDAO();

    public void addMovie(Movie movie) throws SQLException {
        movieDAO.add(movie);
    }

    public void updateMovie(Movie movie) throws SQLException {
        movieDAO.update(movie);
    }

    public void deleteMovie(int id) throws SQLException {
        movieDAO.delete(id);
    }

    public List<Show> allShows() throws SQLException {
        return showDAO.findAll();
    }

    public void addShow(Show show) throws SQLException {
        showDAO.add(show);
    }

    public void deleteShow(int id) throws SQLException {
        showDAO.delete(id);
    }

    public List<Theatre> allTheatres() throws SQLException {
        return theatreDAO.findAll();
    }

    public void addTheatre(Theatre theatre) throws SQLException {
        theatreDAO.add(theatre);
    }

    public void deleteTheatre(int id) throws SQLException {
        theatreDAO.delete(id);
    }

    public List<User> allUsers() throws SQLException {
        return userDAO.findAllUsers();
    }

    public void setUserBlocked(int userId, boolean blocked) throws SQLException {
        userDAO.setBlockedStatus(userId, blocked);
    }

    public List<Booking> allBookings() throws SQLException {
        return bookingDAO.findAll();
    }

    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMovies", movieDAO.count());
        stats.put("totalShows", showDAO.count());
        stats.put("totalBookings", bookingDAO.getConfirmedCount());
        stats.put("todayBookings", bookingDAO.getTodayBookingsCount());
        stats.put("totalRevenue", bookingDAO.getTotalRevenue());
        stats.put("todayRevenue", bookingDAO.getTodayRevenue());
        stats.put("cancelledBookings", bookingDAO.getCancelledCount());
        stats.put("popularMovie", bookingDAO.getMostPopularMovie());
        stats.put("totalUsers", userDAO.findAllUsers().size());
        return stats;
    }
}