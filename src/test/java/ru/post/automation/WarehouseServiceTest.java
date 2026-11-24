package ru.post.automation;

import org.junit.jupiter.api.Test;
import ru.post.automation.service.NoFreeCellException;
import ru.post.automation.service.WarehouseService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarehouseServiceTest {
    @Test
    void reserveReducesFreeCells() {
        WarehouseService warehouse = new WarehouseService(3);
        warehouse.reserve();
        assertEquals(2, warehouse.freeCount());
    }

    @Test
    void reserveFailsWhenWarehouseIsFull() {
        WarehouseService warehouse = new WarehouseService(1);
        warehouse.reserve();
        assertThrows(NoFreeCellException.class, warehouse::reserve);
    }

    @Test
    void releasedCellCanBeReused() {
        WarehouseService warehouse = new WarehouseService(1);
        String cell = warehouse.reserve();
        warehouse.release(cell);
        assertEquals(cell, warehouse.reserve());
    }
}
