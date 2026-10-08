package model;

public class Show {

    private final int id;
    private final int movieId;
    private final String showTime;
    private final String theatre;
    private final int screenNumber;

    public Show(int id, int movieId, String showTime, String theatre, int screenNumber) {
        this.id = id;
        this.movieId = movieId;
        this.showTime = showTime;
        this.theatre = theatre;
        this.screenNumber = screenNumber;
    }

    public int getId() {
        return id;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getShowTime() {
        return showTime;
    }

    public String getTheatre() {
        return theatre;
    }

    public String getTheatreName() {
        return theatre;
    }

    public int getScreenNumber() {
        return screenNumber;
    }

    @Override
    public String toString() {
        return showTime + "  |  " + theatre + " - Screen " + screenNumber;
    }
}