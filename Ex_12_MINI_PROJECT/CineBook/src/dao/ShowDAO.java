package dao;

import java.sql.*;
import java.util.*;
import model.Show;

public class ShowDAO {

    public List<Show> findByMovie(int movieId) throws SQLException {
        List<Show> list = new ArrayList<>();
        String sql = "SELECT * FROM shows WHERE movie_id = ? ORDER BY id";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(makeShow(rs));
                }
            }
        }
        return list;
    }

    public List<Show> findAll() throws SQLException {
        List<Show> list = new ArrayList<>();
        String sql = "SELECT * FROM shows ORDER BY movie_id, id";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(makeShow(rs));
            }
        }
        return list;
    }

    public void add(Show show) throws SQLException {
        String sql = "INSERT INTO shows(movie_id, show_time, theatre, screen_number) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, show.getMovieId());
            pstmt.setString(2, show.getShowTime());
            pstmt.setString(3, show.getTheatreName());
            pstmt.setInt(4, show.getScreenNumber());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM shows WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM shows";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Show makeShow(ResultSet rs) throws SQLException {
        return new Show(
            rs.getInt("id"),
            rs.getInt("movie_id"),
            rs.getString("show_time"),
            rs.getString("theatre"),
            rs.getInt("screen_number")
        );
    }
}