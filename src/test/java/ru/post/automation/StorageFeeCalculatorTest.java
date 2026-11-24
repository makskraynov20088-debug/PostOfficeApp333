package ru.post.automation;

import org.junit.jupiter.api.Test;
import ru.post.automation.service.StorageFeeCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageFeeCalculatorTest {
    private final StorageFeeCalculator calculator = new StorageFeeCalculator();

    @Test
    void firstFiveDaysAreFree() {
        assertEquals(0.0, calculator.calculate(3));
        assertEquals(0.0, calculator.calculate(5));
    }

    @Test
    void sixthDayCostsOneRate() {
        assertEquals(10.0, calculator.calculate(6));
    }

    @Test
    void feeIsLimitedByMaximum() {
        assertEquals(250.0, calculator.calculate(40));
    }

    @Test
    void negativeDaysAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(-1));
    }
}
