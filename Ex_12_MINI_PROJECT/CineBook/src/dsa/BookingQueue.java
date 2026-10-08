package dsa;

import java.util.ArrayDeque;
import java.util.Queue;
import model.Booking;

public class BookingQueue {

    private final Queue<Booking> requests = new ArrayDeque<>();

    public void enqueue(Booking booking) {
        requests.offer(booking);
    }

    public Booking processNext() {
        return requests.poll();
    }

    public int size() {
        return requests.size();
    }

    public boolean isEmpty() {
        return requests.isEmpty();
    }
}