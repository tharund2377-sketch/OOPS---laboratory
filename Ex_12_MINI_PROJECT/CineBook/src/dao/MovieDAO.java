package dao;

import java.sql.*;
import java.util.*;
import model.Movie;

public class MovieDAO {

    public List<Movie> findAll() throws SQLException {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY title";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(makeMovie(rs));
            }
        }
        return list;
    }

    public Movie findById(int id) throws SQLException {
        String sql = "SELECT * FROM movies WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return makeMovie(rs);
                }
            }
        }
        return null;
    }

    public void add(Movie movie) throws SQLException {
        String sql = "INSERT INTO movies(title, genre, duration, rating, description, poster_path) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            populateStatement(pstmt, movie);
            pstmt.executeUpdate();
        }
    }

    public void update(Movie movie) throws SQLException {
        String sql = "UPDATE movies SET title = ?, genre = ?, duration = ?, rating = ?, description = ?, poster_path = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            populateStatement(pstmt, movie);
            pstmt.setInt(7, movie.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM movies WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM movies";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void populateStatement(PreparedStatement pstmt, Movie movie) throws SQLException {
        pstmt.setString(1, movie.getTitle());
        pstmt.setString(2, movie.getGenre());
        pstmt.setString(3, movie.getDuration());
        pstmt.setString(4, movie.getRating());
        pstmt.setString(5, movie.getDescription());
        pstmt.setString(6, movie.getPosterPath());
    }

    private Movie makeMovie(ResultSet rs) throws SQLException {
        return new Movie(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("genre"),
            rs.getString("duration"),
            rs.getString("rating"),
            rs.getString("description"),
            rs.getString("poster_path")
        );
    }
}