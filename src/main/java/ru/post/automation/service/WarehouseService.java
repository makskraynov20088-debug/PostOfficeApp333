package ru.post.automation.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class WarehouseService {
    private final Deque<String> freeCells = new ArrayDeque<>();
    private final Set<String> busyCells = new HashSet<>();

    public WarehouseService(int cellCount) {
        for (int i = 1; i <= cellCount; i++) {
            freeCells.add(String.format("Я-%02d", i));
        }
    }

    public String reserve() {
        String cell = freeCells.pollFirst();
        if (cell == null) {
            throw new NoFreeCellException("Свободных ячеек на складе нет");
        }
        busyCells.add(cell);
        return cell;
    }

    public void release(String cell) {
        if (busyCells.remove(cell)) {
            freeCells.addFirst(cell);
        }
    }

    public int freeCount() {
        return freeCells.size();
    }
}
