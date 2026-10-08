package dsa;

import java.util.ArrayDeque;
import java.util.Deque;
import model.Booking;

public class CancellationStack {

    private final Deque<Booking> stack = new ArrayDeque<>();

    public void push(Booking booking) {
        if (booking != null) {
            stack.push(booking);
        }
    }

    public Booking pop() {
        return stack.poll();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }
}
