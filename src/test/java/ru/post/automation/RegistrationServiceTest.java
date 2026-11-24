package ru.post.automation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.post.automation.model.Shipment;
import ru.post.automation.model.ShipmentStatus;
import ru.post.automation.service.RegistrationService;
import ru.post.automation.service.SmsNotifier;
import ru.post.automation.service.TariffCalculator;
import ru.post.automation.service.TrackingService;
import ru.post.automation.service.WarehouseService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationServiceTest {
    private WarehouseService warehouse;
    private TrackingService tracking;
    private SmsNotifier notifier;
    private RegistrationService registration;

    @BeforeEach
    void setUp() {
        warehouse = new WarehouseService(5);
        tracking = new TrackingService();
        notifier = new SmsNotifier();
        registration = new RegistrationService(new TariffCalculator(), warehouse, tracking, notifier);
    }

    @Test
    void registrationConnectsAllModules() {
        Shipment s = registration.register("Крайнов", "Попов", "+79052223344", 2.2, 2);
        assertEquals(320.0, s.getCost());
        assertEquals(4, warehouse.freeCount());
        assertEquals(1, tracking.count());
        assertEquals(1, notifier.getSent().size());
    }

    @Test
    void invalidPhoneDoesNotReserveCell() {
        assertThrows(IllegalArgumentException.class,
                () -> registration.register("Крайнов", "Попов", "89052223344", 1.0, 1));
        assertEquals(5, warehouse.freeCount());
        assertEquals(0, tracking.count());
    }

    @Test
    void arrivalSendsSmsToRecipient() {
        Shipment s = registration.register("Крайнов", "Попов", "+79052223344", 1.0, 1);
        registration.moveStatus(s.getTrackNumber(), ShipmentStatus.IN_TRANSIT);
        registration.moveStatus(s.getTrackNumber(), ShipmentStatus.ARRIVED);
        assertEquals(2, notifier.getSent().size());
        assertTrue(notifier.getSent().get(1).contains("ожидает вас"));
    }

    @Test
    void deliveryReleasesCell() {
        Shipment s = registration.register("Крайнов", "Попов", "+79052223344", 1.0, 1);
        registration.moveStatus(s.getTrackNumber(), ShipmentStatus.IN_TRANSIT);
        registration.moveStatus(s.getTrackNumber(), ShipmentStatus.ARRIVED);
        registration.moveStatus(s.getTrackNumber(), ShipmentStatus.DELIVERED);
        assertEquals(5, warehouse.freeCount());
    }
}
