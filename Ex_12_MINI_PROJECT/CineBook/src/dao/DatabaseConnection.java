package dao;

import java.sql.*;

public final class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:cinebook.db";

    private DatabaseConnection() { }

    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() throws SQLException {
        try (Connection conn = open();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    is_blocked INTEGER DEFAULT 0
                )
            """);

            // Migration for pre-existing database files
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN is_blocked INTEGER DEFAULT 0");
            } catch (SQLException ignored) {
                // Column already exists
            }

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS theatres (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    city TEXT NOT NULL,
                    total_rows INTEGER DEFAULT 5,
                    seats_per_row INTEGER DEFAULT 8
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS movies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    genre TEXT,
                    duration TEXT,
                    rating TEXT,
                    description TEXT,
                    poster_path TEXT
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS shows (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    movie_id INTEGER REFERENCES movies(id) ON DELETE CASCADE,
                    show_time TEXT NOT NULL,
                    theatre TEXT,
                    screen_number INTEGER
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bookings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER REFERENCES users(id),
                    movie_id INTEGER REFERENCES movies(id),
                    show_id INTEGER REFERENCES shows(id),
                    seats TEXT NOT NULL,
                    total_amount REAL,
                    status TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    booking_id INTEGER REFERENCES bookings(id),
                    amount REAL,
                    payment_method TEXT,
                    payment_status TEXT
                )
            """);

            seed(conn);
        }
    }

    private static void seed(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO users(name, email, password, role, is_blocked) VALUES(?, ?, ?, ?, 0)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    addUser(pstmt, "Aarav Mehta", "admin@cinebook.com", "admin123", "ADMIN");
                    addUser(pstmt, "Demo Customer", "user@cinebook.com", "user123", "CUSTOMER");
                }
            }
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM theatres")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO theatres(name, city, total_rows, seats_per_row) VALUES(?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    addTheatre(pstmt, "CineBook Central", "Mumbai", 5, 8);
                    addTheatre(pstmt, "CineBook IMAX Grand", "Delhi", 5, 8);
                    addTheatre(pstmt, "CineBook Dolby Cinema", "Bengaluru", 5, 8);
                }
            }
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM movies")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO movies(title, genre, duration, rating, description, poster_path) VALUES(?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    int dune = addMovie(pstmt, "Dune: Part Two", "Sci-Fi / Adventure", "2h 46m", "8.7", 
                        "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.", "#D32F2F");
                    int past = addMovie(pstmt, "Past Lives", "Drama / Romance", "1h 46m", "7.9", 
                        "Two childhood friends reconnect in New York and reckon with the lives they might have shared.", "#8E24AA");
                    int spider = addMovie(pstmt, "Spider-Man: Across the Spider-Verse", "Animation / Action", "2h 20m", "8.6", 
                        "Miles Morales travels across the multiverse and meets a team of Spider-People.", "#C2185B");
                    
                    addShows(conn, dune);
                    addShows(conn, past);
                    addShows(conn, spider);
                }
            }
        }
    }

    private static void addUser(PreparedStatement pstmt, String name, String email, String password, String role) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setString(2, email);
        pstmt.setString(3, password);
        pstmt.setString(4, role);
        pstmt.executeUpdate();
    }

    private static void addTheatre(PreparedStatement pstmt, String name, String city, int rows, int cols) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setString(2, city);
        pstmt.setInt(3, rows);
        pstmt.setInt(4, cols);
        pstmt.executeUpdate();
    }

    private static int addMovie(PreparedStatement pstmt, String title, String genre, String duration, String rating, String description, String poster) throws SQLException {
        pstmt.setString(1, title);
        pstmt.setString(2, genre);
        pstmt.setString(3, duration);
        pstmt.setString(4, rating);
        pstmt.setString(5, description);
        pstmt.setString(6, poster);
        pstmt.executeUpdate();
        try (ResultSet keys = pstmt.getGeneratedKeys()) {
            keys.next();
            return keys.getInt(1);
        }
    }

    private static void addShows(Connection conn, int movieId) throws SQLException {
        String sql = "INSERT INTO shows(movie_id, show_time, theatre, screen_number) VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[] times = {"10:30 AM", "02:00 PM", "07:30 PM"};
            for (String time : times) {
                pstmt.setInt(1, movieId);
                pstmt.setString(2, time);
                pstmt.setString(3, "CineBook Central");
                pstmt.setInt(4, (movieId % 3) + 1);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}