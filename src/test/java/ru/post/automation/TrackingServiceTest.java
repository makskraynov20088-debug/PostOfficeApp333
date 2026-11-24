package ru.post.automation;

import org.junit.jupiter.api.Test;
import ru.post.automation.model.Shipment;
import ru.post.automation.model.ShipmentStatus;
import ru.post.automation.service.ShipmentNotFoundException;
import ru.post.automation.service.TrackingService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrackingServiceTest {
    private final TrackingService tracking = new TrackingService();

    private Shipment sample() {
        Shipment s = new Shipment("80080000012346", "Крайнов", "Попов", "+79052223344", 1.0, 1, 155.0, "Я-01");
        tracking.save(s);
        return s;
    }

    @Test
    void savedShipmentIsFound() {
        sample();
        assertEquals("Попов", tracking.find("80080000012346").getRecipientName());
    }

    @Test
    void unknownShipmentThrowsNotFound() {
        assertThrows(ShipmentNotFoundException.class, () -> tracking.find("00000000000000"));
    }

    @Test
    void badTrackNumberIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> tracking.find("12345"));
    }

    @Test
    void statusCannotSkipSteps() {
        sample();
        assertThrows(IllegalStateException.class,
                () -> tracking.changeStatus("80080000012346", ShipmentStatus.DELIVERED));
    }
}
