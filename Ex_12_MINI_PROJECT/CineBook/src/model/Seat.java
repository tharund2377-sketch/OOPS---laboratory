package model;

public class Seat {

    private final String seatNumber;
    private final char row;
    private final int col;
    private final String category; // VIP, PREMIUM, REGULAR
    private final double price;
    private boolean booked;

    public Seat(String seatNumber, char row, int col, String category, double price, boolean booked) {
        this.seatNumber = seatNumber;
        this.row = row;
        this.col = col;
        this.category = category;
        this.price = price;
        this.booked = booked;
    }

    public static double getCategoryPrice(char rowChar) {
        return switch (rowChar) {
            case 'A' -> 350.0; // VIP
            case 'B', 'C' -> 250.0; // Premium
            default -> 150.0; // Regular (D, E)
        };
    }

    public static String getCategoryName(char rowChar) {
        return switch (rowChar) {
            case 'A' -> "VIP";
            case 'B', 'C' -> "PREMIUM";
            default -> "REGULAR";
        };
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public char getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}