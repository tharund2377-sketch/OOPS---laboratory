package model;

import java.util.List;

public interface Bookable {
    boolean reserveSeats(List<String> seats);
    boolean releaseSeats(List<String> seats);
}
