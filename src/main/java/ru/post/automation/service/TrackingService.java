package ru.post.automation.service;

import ru.post.automation.model.Shipment;
import ru.post.automation.model.ShipmentStatus;
import ru.post.automation.util.AppLogger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TrackingService {
    private static final Logger LOG = AppLogger.get(TrackingService.class);
    private final Map<String, Shipment> storage = new LinkedHashMap<>();

    public void save(Shipment shipment) {
        storage.put(shipment.getTrackNumber(), shipment);
    }

    public Shipment find(String trackNumber) {
        if (trackNumber == null || !trackNumber.matches("\\d{14}")) {
            throw new IllegalArgumentException("Трек-номер должен состоять из 14 цифр");
        }
        Shipment shipment = storage.get(trackNumber);
        if (shipment == null) {
            LOG.warning("Поиск несуществующего отправления " + trackNumber);
            throw new ShipmentNotFoundException(trackNumber);
        }
        return shipment;
    }

    public Shipment changeStatus(String trackNumber, ShipmentStatus next) {
        Shipment shipment = find(trackNumber);
        if (!shipment.getStatus().canMoveTo(next)) {
            throw new IllegalStateException("Нельзя перевести статус из «" + shipment.getStatus().getTitle()
                    + "» в «" + next.getTitle() + "»");
        }
        shipment.setStatus(next);
        LOG.info("Отправление " + trackNumber + ": статус «" + next.getTitle() + "»");
        return shipment;
    }

    public int count() {
        return storage.size();
    }
}
