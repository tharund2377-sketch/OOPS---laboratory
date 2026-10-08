package dsa;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoStack {

    private final Deque<String> seats = new ArrayDeque<>();

    public void push(String seat) {
        seats.push(seat);
    }

    public String undo() {
        return seats.poll();
    }

    public void clear() {
        seats.clear();
    }

    public int size() {
        return seats.size();
    }
}