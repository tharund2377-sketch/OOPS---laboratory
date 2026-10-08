package model;

public class Theatre {

    private final int id;
    private final String name;
    private final String city;
    private final int totalRows;
    private final int seatsPerRow;

    public Theatre(int id, String name, String city, int totalRows, int seatsPerRow) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    @Override
    public String toString() {
        return name + " (" + city + ")";
    }
}
