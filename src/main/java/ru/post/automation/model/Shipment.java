package ru.post.automation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shipment {
    private final String trackNumber;
    private final String senderName;
    private final String recipientName;
    private final String recipientPhone;
    private final double weightKg;
    private final int zone;
    private final double cost;
    private final String cellCode;
    private ShipmentStatus status;
    private final List<String> history = new ArrayList<>();

    public Shipment(String trackNumber, String senderName, String recipientName, String recipientPhone,
                    double weightKg, int zone, double cost, String cellCode) {
        this.trackNumber = trackNumber;
        this.senderName = senderName;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.weightKg = weightKg;
        this.zone = zone;
        this.cost = cost;
        this.cellCode = cellCode;
        this.status = ShipmentStatus.ACCEPTED;
        this.history.add(ShipmentStatus.ACCEPTED.getTitle());
    }

    public String getTrackNumber() { return trackNumber; }
    public String getSenderName() { return senderName; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public double getWeightKg() { return weightKg; }
    public int getZone() { return zone; }
    public double getCost() { return cost; }
    public String getCellCode() { return cellCode; }
    public ShipmentStatus getStatus() { return status; }
    public List<String> getHistory() { return Collections.unmodifiableList(history); }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
        this.history.add(status.getTitle());
    }
}
