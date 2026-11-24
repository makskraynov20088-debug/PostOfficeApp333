package ru.post.automation;

import ru.post.automation.service.StorageFeeCalculator;

public class DebugDemo {
    public static void main(String[] args) {
        StorageFeeCalculator calculator = new StorageFeeCalculator();
        int[] days = {3, 6, 40};
        double[] expected = {0, 10, 250};
        for (int i = 0; i < days.length; i++) {
            double actual = calculator.calculate(days[i]);
            System.out.println("Дней: " + days[i] + ", ожидалось: " + expected[i] + ", получено: " + actual);
        }
    }
}
