package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Theatre;

public class TheatreDAO {

    public List<Theatre> findAll() throws SQLException {
        List<Theatre> list = new ArrayList<>();
        String sql = "SELECT * FROM theatres ORDER BY id";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(makeTheatre(rs));
            }
        }
        return list;
    }

    public void add(Theatre theatre) throws SQLException {
        String sql = "INSERT INTO theatres(name, city, total_rows, seats_per_row) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, theatre.getName());
            pstmt.setString(2, theatre.getCity());
            pstmt.setInt(3, theatre.getTotalRows());
            pstmt.setInt(4, theatre.getSeatsPerRow());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM theatres WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Theatre makeTheatre(ResultSet rs) throws SQLException {
        return new Theatre(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("city"),
            rs.getInt("total_rows"),
            rs.getInt("seats_per_row")
        );
    }
}
