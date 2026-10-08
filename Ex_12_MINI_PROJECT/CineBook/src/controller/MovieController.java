package controller;

import dao.MovieDAO;
import dsa.BookingSearch;
import model.Movie;

import java.sql.SQLException;
import java.util.List;

public class MovieController {

    private final MovieDAO movieDAO = new MovieDAO();

    public List<Movie> all() throws SQLException {
        return movieDAO.findAll();
    }

    public List<Movie> search(List<Movie> list, String query) {
        if (query == null || query.isBlank()) {
            return list;
        }
        return BookingSearch.filterByTitleOrGenre(list, query);
    }

    public Movie findExactByTitle(List<Movie> list, String title) {
        return BookingSearch.findByTitle(list, title);
    }

    public void add(Movie movie) throws SQLException {
        movieDAO.add(movie);
    }

    public void update(Movie movie) throws SQLException {
        movieDAO.update(movie);
    }

    public void delete(int id) throws SQLException {
        movieDAO.delete(id);
    }
}