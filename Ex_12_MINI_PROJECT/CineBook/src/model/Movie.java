package model;

public class Movie {

    private final int id;
    private final String title;
    private final String genre;
    private final String duration;
    private final String rating;
    private final String description;
    private final String posterPath;

    public Movie(int id, String title, String genre, String duration, String rating, String description, String posterPath) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.description = description;
        this.posterPath = posterPath;
    }

    public Movie(String title, String genre, String duration, String rating, String description, String posterPath) {
        this(0, title, genre, duration, rating, description, posterPath);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getDuration() {
        return duration;
    }

    public String getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public String getPosterPath() {
        return posterPath;
    }

    @Override
    public String toString() {
        return title + " (" + genre + ")";
    }
}