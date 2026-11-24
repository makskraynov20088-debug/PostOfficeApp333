package ru.post.automation;

import org.junit.jupiter.api.Test;
import ru.post.automation.service.TariffCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TariffCalculatorTest {
    private final TariffCalculator calculator = new TariffCalculator();

    @Test
    void minimalWeightCostsBasePrice() {
        assertEquals(120.0, calculator.calculate(0.5, 1));
    }

    @Test
    void weightIsRoundedUpToNextStep() {
        assertEquals(320.0, calculator.calculate(2.2, 2));
    }

    @Test
    void heavyParcelInFarZone() {
        assertEquals(625.0, calculator.calculate(4.0, 5));
    }

    @Test
    void zeroWeightIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(0, 1));
    }

    @Test
    void unknownZoneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(1.0, 6));
    }
}
