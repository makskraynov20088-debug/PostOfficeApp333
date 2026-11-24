package ru.post.automation.service;

public class StorageFeeCalculator {
    private static final int FREE_DAYS = 5;
    private static final double RATE = 10;
    private static final double MAX_FEE = 250;

    public double calculate(int storageDays) {
        if (storageDays < 0) {
            throw new IllegalArgumentException("Срок хранения не может быть отрицательным");
        }
        if (storageDays <= FREE_DAYS) {
            return 0;
        }
        return Math.min((storageDays - FREE_DAYS) * RATE, MAX_FEE);
    }
}