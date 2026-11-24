package ru.post.automation.service;

import ru.post.automation.model.Shipment;
import ru.post.automation.model.ShipmentStatus;
import ru.post.automation.util.AppLogger;

import java.util.logging.Logger;

public class RegistrationService {
    private static final Logger LOG = AppLogger.get(RegistrationService.class);
    private final TariffCalculator tariff;
    private final WarehouseService warehouse;
    private final TrackingService tracking;
    private final Notifier notifier;
    private long counter = 12345;

    public RegistrationService(TariffCalculator tariff, WarehouseService warehouse,
                               TrackingService tracking, Notifier notifier) {
        this.tariff = tariff;
        this.warehouse = warehouse;
        this.tracking = tracking;
        this.notifier = notifier;
    }

    public Shipment register(String sender, String recipient, String phone, double weightKg, int zone) {
        if (sender == null || sender.isBlank() || recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Укажите отправителя и получателя");
        }
        if (phone == null || !phone.matches("\\+7\\d{10}")) {
            throw new IllegalArgumentException("Телефон должен быть в формате +7XXXXXXXXXX");
        }
        double cost = tariff.calculate(weightKg, zone);
        String cell;
        try {
            cell = warehouse.reserve();
        } catch (NoFreeCellException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Не удалось принять отправление для " + recipient, e);
            throw e;
        }
        String track = String.format("80080%09d", ++counter);
        Shipment shipment = new Shipment(track, sender, recipient, phone, weightKg, zone, cost, cell);
        tracking.save(shipment);
        notifier.send(phone, "Отправление " + track + " принято, стоимость " + cost + " руб.");
        LOG.info("Зарегистрировано отправление " + track + ", ячейка " + cell);
        return shipment;
    }

    public Shipment moveStatus(String trackNumber, ShipmentStatus next) {
        Shipment shipment = tracking.changeStatus(trackNumber, next);
        if (next == ShipmentStatus.ARRIVED) {
            notifier.send(shipment.getRecipientPhone(), "Отправление " + trackNumber + " ожидает вас в отделении");
        }
        if (next == ShipmentStatus.DELIVERED) {
            warehouse.release(shipment.getCellCode());
        }
        return shipment;
    }
}
