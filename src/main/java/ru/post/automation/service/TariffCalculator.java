package ru.post.automation.service;

public class TariffCalculator {
    private static final double[] ZONE_BASE = {120, 180, 240, 310, 380};
    private static final double STEP_PRICE = 35;
    private static final double STEP_WEIGHT = 0.5;
    private static final double MAX_WEIGHT = 20;

    public double calculate(double weightKg, int zone) {
        if (weightKg <= 0 || weightKg > MAX_WEIGHT) {
            throw new IllegalArgumentException("Вес должен быть больше 0 и не более 20 кг");
        }
        if (zone < 1 || zone > ZONE_BASE.length) {
            throw new IllegalArgumentException("Неверная зона доставки: " + zone);
        }
        int steps = Math.max(0, (int) Math.ceil((weightKg - STEP_WEIGHT) / STEP_WEIGHT));
        return ZONE_BASE[zone - 1] + steps * STEP_PRICE;
    }
}
