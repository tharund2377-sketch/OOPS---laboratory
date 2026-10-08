package dsa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Movie;

public final class BookingSearch {

    private BookingSearch() { }

    /**
     * Performs Binary Search on an alphabetically sorted list of movies by title.
     * Returns the matching Movie object, or null if not found.
     */
    public static Movie findByTitle(List<Movie> movies, String targetTitle) {
        if (movies == null || targetTitle == null || targetTitle.isBlank()) {
            return null;
        }

        List<Movie> sorted = new ArrayList<>(movies);
        sorted.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        int low = 0;
        int high = sorted.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Movie midMovie = sorted.get(mid);
            int comp = midMovie.getTitle().compareToIgnoreCase(targetTitle);

            if (comp == 0) {
                return midMovie;
            } else if (comp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return null;
    }

    /**
     * Uses Binary Search to find matching titles or genre filters.
     */
    public static List<Movie> filterByTitleOrGenre(List<Movie> movies, String query) {
        if (movies == null || query == null || query.isBlank()) {
            return movies == null ? List.of() : movies;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Movie> sorted = new ArrayList<>(movies);
        sorted.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        List<Movie> results = new ArrayList<>();
        
        // 1. Binary Search for exact or prefix title match
        Movie exactMatch = findByTitle(sorted, query);
        if (exactMatch != null) {
            results.add(exactMatch);
        }

        // 2. Add remaining partial/genre matches avoiding duplicates
        for (Movie m : sorted) {
            if (results.contains(m)) continue;
            if (m.getTitle().toLowerCase().contains(lowerQuery) ||
                m.getGenre().toLowerCase().contains(lowerQuery)) {
                results.add(m);
            }
        }

        return results;
    }
}