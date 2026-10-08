package dsa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Booking;

public class BookingHashMap {

    private final Map<Integer, Booking> bookingMap = new HashMap<>();

    public void buildIndex(List<Booking> bookings) {
        bookingMap.clear();
        if (bookings != null) {
            for (Booking b : bookings) {
                bookingMap.put(b.getId(), b);
            }
        }
    }

    public Booking findById(int bookingId) {
        return bookingMap.get(bookingId);
    }
}
