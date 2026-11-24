package ru.post.automation.service;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(String trackNumber) {
        super("Отправление " + trackNumber + " не найдено");
    }
}
