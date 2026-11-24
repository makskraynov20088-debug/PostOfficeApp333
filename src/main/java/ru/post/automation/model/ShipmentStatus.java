package ru.post.automation.model;

public enum ShipmentStatus {
    ACCEPTED("Принято в отделении"),
    IN_TRANSIT("В пути"),
    ARRIVED("Прибыло в место вручения"),
    DELIVERED("Вручено");

    private final String title;

    ShipmentStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean canMoveTo(ShipmentStatus next) {
        return next.ordinal() == ordinal() + 1;
    }
}
